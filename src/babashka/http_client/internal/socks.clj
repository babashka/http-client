(ns babashka.http-client.internal.socks
  {:no-doc true}
  (:require
   [clojure.string :as str])
  (:import
   [java.io InputStream OutputStream]
   [java.net InetAddress InetSocketAddress ServerSocket Socket URI]
   [java.nio.charset StandardCharsets]
   [java.security MessageDigest SecureRandom]
   [java.util Base64]))

(set! *warn-on-reflection* true)

;; The bridge listens on loopback, where any local process can reach it. Only
;; requests carrying this token are served. `Basic` would be stripped from the
;; CONNECT by jdk.http.auth.tunneling.disabledSchemes, so the scheme is our own.
(def ^:private auth-scheme "Bbsocks")

(defonce ^:private token
  (delay (let [b (byte-array 32)]
           (.nextBytes (SecureRandom.) b)
           (.encodeToString (Base64/getUrlEncoder) b))))

(defn authorization
  "Value of the `Proxy-Authorization` header the bridge demands."
  []
  (str auth-scheme " " @token))

(def ^:private reply-messages
  {1 "general SOCKS server failure"
   2 "connection not allowed by ruleset"
   3 "network unreachable"
   4 "host unreachable"
   5 "connection refused"
   6 "TTL expired"
   7 "command not supported"
   8 "address type not supported"})

(defn- ->byte-array ^bytes [coll]
  (byte-array (map unchecked-byte coll)))

(defn- read-n ^bytes [^InputStream in ^long n]
  (let [buf (byte-array n)]
    (loop [off 0]
      (if (< off n)
        (let [r (.read in buf off (- n off))]
          (when (neg? r)
            (throw (ex-info "SOCKS proxy closed the connection." {})))
          (recur (+ off r)))
        buf))))

(defn- ub ^long [^bytes buf ^long i]
  (bit-and (aget buf i) 0xFF))

(defn- authenticate [^InputStream in ^OutputStream out ^String user ^String pass]
  (let [u (.getBytes user StandardCharsets/UTF_8)
        p (.getBytes pass StandardCharsets/UTF_8)]
    ;; One write per message. The stream is unbuffered, so writing in parts puts
    ;; the message on the wire in parts, and a proxy that reads it in one call
    ;; sees a truncated message and hangs up.
    (.write out (->byte-array (concat [1 (alength u)] (seq u) [(alength p)] (seq p))))
    (.flush out)
    (when-not (zero? (ub (read-n in 2) 1))
      (throw (ex-info "SOCKS proxy rejected the user and password." {})))))

(defn- greet [^InputStream in ^OutputStream out user pass]
  (let [auth? (boolean (and user pass))]
    (.write out (->byte-array (if auth? [5 2 0 2] [5 1 0])))
    (.flush out)
    (case (ub (read-n in 2) 1)
      0 nil
      2 (if auth?
          (authenticate in out user pass)
          (throw (ex-info "SOCKS proxy requires a user and password." {})))
      (throw (ex-info "SOCKS proxy offered no acceptable authentication method." {})))))

(defn- skip-bound-address [^InputStream in ^long address-type]
  (case address-type
    1 (read-n in 4)
    3 (read-n in (ub (read-n in 1) 0))
    4 (read-n in 16)
    (throw (ex-info "SOCKS proxy returned an unknown address type." {})))
  (read-n in 2))

(def ^:private ipv4-address #"\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}")

;; Literals are parsed here rather than through InetAddress, whose getAddress is
;; not available to babashka when this namespace is interpreted.

(defn- ipv4-octets
  "The four octets of a dotted quad, or nil when it is not one."
  [^String host]
  (when (re-matches ipv4-address host)
    (let [octets (map (fn [^String s] (Integer/parseInt s)) (str/split host #"\."))]
      (when (every? (fn [o] (<= 0 o 255)) octets)
        octets))))

(defn- group-octets
  "Octets of colon separated IPv6 groups, where the last may be a dotted quad.
  Returns nil on anything malformed."
  [groups]
  (reduce (fn [acc ^String g]
            (cond
              (nil? acc) (reduced nil)
              (str/includes? g ".") (if-let [v4 (ipv4-octets g)]
                                      (into acc v4)
                                      (reduced nil))
              :else (let [v (when (re-matches #"[0-9a-fA-F]{1,4}" g)
                              (Integer/parseInt g 16))]
                      (if v
                        (conj acc (bit-shift-right v 8) (bit-and v 0xFF))
                        (reduced nil)))))
          []
          groups))

(defn- split-groups [^String s]
  (if (str/blank? s) [] (str/split s #":")))

(defn- ipv6-octets
  "The sixteen octets of an IPv6 literal, or nil when it is not one."
  [^String host]
  (when (str/includes? host ":")
    (let [without-zone (first (str/split host #"%" 2))
          halves (str/split without-zone #"::" -1)]
      (case (count halves)
        1 (let [octets (group-octets (split-groups without-zone))]
            (when (= 16 (count octets)) octets))
        2 (let [head (group-octets (split-groups (first halves)))
                tail (group-octets (split-groups (second halves)))]
            ;; `::` stands for at least one zero group.
            (when (and head tail (<= (+ (count head) (count tail)) 14))
              (concat head (repeat (- 16 (count head) (count tail)) 0) tail)))
        nil))))

(defn- address-bytes
  "SOCKS5 address type and encoded address for a target host. A name is sent as
  is, so the proxy resolves it rather than this process."
  [^String host]
  (if-let [v4 (ipv4-octets host)]
    [1 (->byte-array v4)]
    (if-let [v6 (ipv6-octets host)]
      [4 (->byte-array v6)]
      (let [b (.getBytes host StandardCharsets/UTF_8)]
        [3 (->byte-array (cons (alength b) (seq b)))]))))

(defn- request-connect [^InputStream in ^OutputStream out ^String host ^long port]
  (let [[atyp ^bytes addr] (address-bytes host)]
    (.write out (->byte-array (concat [5 1 0 atyp]
                                      (seq addr)
                                      [(bit-shift-right port 8) (bit-and port 0xFF)])))
    (.flush out))
  (let [reply (read-n in 4)
        status (ub reply 1)]
    (when-not (zero? status)
      (throw (ex-info (str "SOCKS proxy refused to connect to " host ":" port ": "
                           (get reply-messages status (str "reply code " status)))
                      {:host host :port port :reply status})))
    (skip-bound-address in (ub reply 3))))

(def default-connect-timeout 10000)

(defn- socks-connect
  "Connects to target-host:target-port through the SOCKS5 proxy described by opts."
  ^Socket [{:keys [host port user pass connect-timeout]
            :or {connect-timeout default-connect-timeout}}
           ^String target-host ^long target-port]
  (let [socket (Socket.)]
    (try
      (.connect socket (InetSocketAddress. ^String host ^long port) (int connect-timeout))
      (.setSoTimeout socket (int connect-timeout))
      (let [in (.getInputStream socket)
            out (.getOutputStream socket)]
        (greet in out user pass)
        (request-connect in out target-host target-port))
      ;; A relay may idle for a long time, so the handshake timeout must not
      ;; outlive the handshake.
      (.setSoTimeout socket 0)
      socket
      (catch Exception e
        (.close socket)
        (throw e)))))

(def ^:private max-header-line 8192)

(defn- read-header-line
  "Reads one CRLF terminated line. Returns nil at end of stream."
  [^InputStream in]
  (let [sb (StringBuilder.)]
    (loop []
      (let [b (.read in)]
        (cond
          (neg? b) (when (pos? (.length sb)) (str sb))
          (= 10 b) (str sb)
          (= 13 b) (recur)
          (> (.length sb) max-header-line)
          (throw (ex-info "Header line is too long." {}))
          :else (do (.append sb (char b)) (recur)))))))

(def ^:private max-headers 64)

(defn- read-headers [^InputStream in]
  (loop [acc []]
    (let [line (read-header-line in)]
      (cond
        (or (nil? line) (= "" line)) acc
        (>= (count acc) max-headers) (throw (ex-info "Too many header lines." {}))
        :else (recur (conj acc line))))))

(defn- header-value ^String [^String line]
  (str/trim (subs line (inc (.indexOf line ":")))))

(defn- authorized? [lines]
  (let [expected (.getBytes ^String (authorization) StandardCharsets/ISO_8859_1)]
    (boolean (some (fn [^String line]
                     (and (str/starts-with? (str/lower-case line) "proxy-authorization:")
                          (MessageDigest/isEqual
                           (.getBytes (header-value line) StandardCharsets/ISO_8859_1)
                           expected)))
                   lines))))

(defn- strip-brackets ^String [^String host]
  (if (and (str/starts-with? host "[") (str/ends-with? host "]"))
    (subs host 1 (dec (count host)))
    host))

(defn- split-host-port [^String s ^long default-port]
  (let [i (.lastIndexOf s ":")]
    (if (and (pos? i) (not (str/ends-with? s "]")))
      [(strip-brackets (subs s 0 i)) (Long/parseLong (subs s (inc i)))]
      [(strip-brackets s) default-port])))

(defn- write-ascii [^OutputStream out ^String s]
  (.write out (.getBytes s StandardCharsets/ISO_8859_1))
  (.flush out))

(defn- quietly [f]
  (try (f) (catch Exception _ nil)))

(defn- pump [^InputStream in ^OutputStream out]
  (let [buf (byte-array 16384)]
    (loop []
      (let [r (.read in buf)]
        (when (pos? r)
          (.write out buf 0 r)
          (.flush out)
          (recur))))))

(defn- relay
  "Copies bytes both ways until either direction ends. Whichever ends first
  closes both sockets, so the other never blocks on a peer that is gone."
  [^Socket client ^Socket origin downstream]
  (let [close-both (fn []
                     (quietly #(.close origin))
                     (quietly #(.close client)))
        upstream (Thread. ^Runnable (fn []
                                      (quietly #(pump (.getInputStream client)
                                                      (.getOutputStream origin)))
                                      (close-both)))]
    (.setDaemon upstream true)
    (.start upstream)
    (quietly downstream)
    (close-both)
    (.join upstream 1000)))

(def ^:private dropped-headers
  ["proxy-connection:" "proxy-authorization:" "connection:" "keep-alive:"
   "upgrade:" "http2-settings:"])

(defn- append-headers
  "Copies headers into sb, dropping the hop by hop ones, and closes the block
  with `Connection: close`."
  [^StringBuilder sb lines]
  (doseq [^String line lines]
    (let [lower (str/lower-case line)]
      (when-not (some (fn [h] (str/starts-with? lower ^String h)) dropped-headers)
        (.append sb line)
        (.append sb "\r\n"))))
  ;; One request per connection avoids parsing bodies to find the next one.
  (.append sb "Connection: close\r\n\r\n"))

(defn- forward-request-head
  "Rewrites an absolute form request line to origin form and forwards the headers."
  [^OutputStream out ^String method ^URI uri ^String version lines]
  (let [path (.getRawPath uri)
        query (.getRawQuery uri)
        sb (StringBuilder.)]
    (.append sb (str method " " (if (str/blank? path) "/" path)
                     (when query (str "?" query)) " " version "\r\n"))
    (append-headers sb lines)
    (write-ascii out (str sb))))

(defn- forward-response-head
  "Forwards the response head, telling the client the connection ends with it.
  Returns false at end of stream."
  [^InputStream in ^OutputStream out]
  (if-let [status-line (read-header-line in)]
    (let [sb (StringBuilder.)]
      (.append sb status-line)
      (.append sb "\r\n")
      (append-headers sb (read-headers in))
      (write-ascii out (str sb))
      true)
    false))

(defn- bad-gateway [^Exception e]
  (let [reason (or (.getMessage e) (str e))]
    (str "HTTP/1.1 502 Bad Gateway\r\n"
         "Content-Type: text/plain; charset=utf-8\r\n"
         "Content-Length: " (alength (.getBytes reason StandardCharsets/ISO_8859_1)) "\r\n"
         "Connection: close\r\n\r\n"
         reason)))

(defn- report
  "Reports a failure the client cannot be told about. java.net.http reduces a
  failed CONNECT to the status code alone, so without this the reason is lost."
  [socks-opts ^Exception e]
  (if-let [handler (:on-error socks-opts)]
    (quietly #(handler e))
    (binding [*out* *err*]
      (println "babashka.http-client SOCKS5 bridge:" (or (.getMessage e) (str e))))))

(defn- connect-or-fail [socks-opts ^Socket client ^String host ^long port]
  (try
    (socks-connect socks-opts host port)
    (catch Exception e
      (report socks-opts e)
      (quietly #(write-ascii (.getOutputStream client) (bad-gateway e)))
      (throw e))))

(defn- handle-connect [socks-opts ^Socket client ^String target]
  (let [[host port] (split-host-port target 443)]
    (with-open [^Socket origin (connect-or-fail socks-opts client host port)]
      (write-ascii (.getOutputStream client) "HTTP/1.1 200 Connection Established\r\n\r\n")
      (relay client origin
             #(pump (.getInputStream origin) (.getOutputStream client))))))

(defn- handle-absolute [socks-opts ^Socket client ^String method ^String target
                        ^String version lines]
  (let [uri (URI. target)
        host (strip-brackets (.getHost uri))
        port (if (pos? (.getPort uri)) (.getPort uri) 80)]
    (with-open [^Socket origin (connect-or-fail socks-opts client host port)]
      (forward-request-head (.getOutputStream origin) method uri version lines)
      (relay client origin
             #(when (forward-response-head (.getInputStream origin) (.getOutputStream client))
                (pump (.getInputStream origin) (.getOutputStream client)))))))

(def ^:private unauthorized
  (str "HTTP/1.1 407 Proxy Authentication Required\r\n"
       "Proxy-Authenticate: " auth-scheme "\r\n"
       "Content-Length: 0\r\n"
       "Connection: close\r\n\r\n"))

(defn- handle [socks-opts ^Socket client]
  (with-open [^Socket client client]
    ;; An unauthenticated caller must not be able to hold a thread open by
    ;; trickling headers, so the head is read under a deadline.
    (.setSoTimeout client (int (:connect-timeout socks-opts default-connect-timeout)))
    (when-let [request-line (read-header-line (.getInputStream client))]
      (let [lines (read-headers (.getInputStream client))
            [method target version] (str/split request-line #" " 3)]
        (if-not (authorized? lines)
          (write-ascii (.getOutputStream client) unauthorized)
          (do
            ;; A relay may idle for a long time. The deadline covered the head.
            (.setSoTimeout client 0)
            (if (= "CONNECT" (str/upper-case (str method)))
              (handle-connect socks-opts client target)
              (handle-absolute socks-opts client method target (or version "HTTP/1.1")
                               lines))))))))

(defn- start-bridge
  "Starts a loopback HTTP proxy that tunnels through the given SOCKS5 proxy.
  Returns the address it listens on."
  [{:keys [host port] :as socks-opts}]
  (let [server (ServerSocket. 0 50 (InetAddress/getByName "127.0.0.1"))
        accept-loop (fn []
                      (loop []
                        (when-not (.isClosed server)
                          (when-let [client (try (.accept server) (catch Exception _ nil))]
                            (doto (Thread. ^Runnable (fn [] (quietly #(handle socks-opts client))))
                              (.setDaemon true)
                              (.start)))
                          (recur))))
        thread (Thread. ^Runnable accept-loop)]
    (.setName thread (str "babashka.http-client SOCKS5 bridge for " host ":" port))
    (.setDaemon thread true)
    (.start thread)
    {:host "127.0.0.1" :port (.getLocalPort server)}))

(defonce ^:private bridges (atom {}))

(defn bridge-address
  "Address of the loopback HTTP proxy for the given SOCKS5 options. One bridge is
  started per distinct set of connection options and shared from then on, so the
  `:on-error` of whichever client starts it is the one it keeps."
  [socks-opts]
  (let [k (select-keys socks-opts [:host :port :user :pass :connect-timeout])]
    @(get (swap! bridges update k (fn [d] (or d (delay (start-bridge socks-opts))))) k)))
