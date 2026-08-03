(ns babashka.http-client.internal.socks
  {:no-doc true}
  (:require
   [clojure.string :as str])
  (:import
   [java.io InputStream OutputStream]
   [java.net InetAddress InetSocketAddress ServerSocket Socket URI]
   [java.nio.charset StandardCharsets]))

(set! *warn-on-reflection* true)

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
    (.write out (->byte-array [1 (alength u)]))
    (.write out u)
    (.write out (->byte-array [(alength p)]))
    (.write out p)
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

(defn- request-connect [^InputStream in ^OutputStream out ^String host ^long port]
  (let [h (.getBytes host StandardCharsets/UTF_8)]
    (.write out (->byte-array [5 1 0 3 (alength h)]))
    (.write out h)
    (.write out (->byte-array [(bit-shift-right port 8) (bit-and port 0xFF)]))
    (.flush out))
  (let [reply (read-n in 4)
        status (ub reply 1)]
    (when-not (zero? status)
      (throw (ex-info (str "SOCKS proxy refused to connect to " host ":" port ": "
                           (get reply-messages status (str "reply code " status)))
                      {:host host :port port :reply status})))
    (skip-bound-address in (ub reply 3))))

(defn- socks-connect
  "Connects to target-host:target-port through the SOCKS5 proxy described by opts."
  ^Socket [{:keys [host port user pass]} ^String target-host ^long target-port]
  (let [socket (Socket.)]
    (try
      (.connect socket (InetSocketAddress. ^String host ^long port))
      (let [in (.getInputStream socket)
            out (.getOutputStream socket)]
        (greet in out user pass)
        (request-connect in out target-host target-port))
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

(defn- drain-headers [^InputStream in]
  (loop []
    (when-let [line (read-header-line in)]
      (when-not (= "" line)
        (recur)))))

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

(defn- relay [^Socket client ^Socket origin downstream]
  (let [upstream (Thread. ^Runnable (fn []
                                      (quietly #(pump (.getInputStream client)
                                                      (.getOutputStream origin)))))]
    (.setDaemon upstream true)
    (.start upstream)
    (quietly downstream)
    (quietly #(.shutdownInput client))
    (.join upstream 1000)))

(def ^:private dropped-headers
  ["proxy-connection:" "connection:" "keep-alive:" "upgrade:" "http2-settings:"])

(defn- append-headers
  "Copies headers into sb, dropping the hop by hop ones, and closes the block
  with `Connection: close`."
  [^StringBuilder sb ^InputStream in]
  (loop []
    (when-let [line (read-header-line in)]
      (when-not (= "" line)
        (let [lower (str/lower-case line)]
          (when-not (some (fn [h] (str/starts-with? lower ^String h)) dropped-headers)
            (.append sb line)
            (.append sb "\r\n")))
        (recur))))
  ;; One request per connection avoids parsing bodies to find the next one.
  (.append sb "Connection: close\r\n\r\n"))

(defn- forward-request-head
  "Rewrites an absolute form request line to origin form and forwards the headers."
  [^InputStream in ^OutputStream out ^String method ^URI uri ^String version]
  (let [path (.getRawPath uri)
        query (.getRawQuery uri)
        sb (StringBuilder.)]
    (.append sb (str method " " (if (str/blank? path) "/" path)
                     (when query (str "?" query)) " " version "\r\n"))
    (append-headers sb in)
    (write-ascii out (str sb))))

(defn- forward-response-head
  "Forwards the response head, telling the client the connection ends with it.
  Returns false at end of stream."
  [^InputStream in ^OutputStream out]
  (if-let [status-line (read-header-line in)]
    (let [sb (StringBuilder.)]
      (.append sb status-line)
      (.append sb "\r\n")
      (append-headers sb in)
      (write-ascii out (str sb))
      true)
    false))

(defn- connect-or-fail [socks-opts ^Socket client ^String host ^long port]
  (try
    (socks-connect socks-opts host port)
    (catch Exception e
      (quietly #(write-ascii (.getOutputStream client) "HTTP/1.1 502 Bad Gateway\r\n\r\n"))
      (throw e))))

(defn- handle-connect [socks-opts ^Socket client ^String target]
  (let [[host port] (split-host-port target 443)]
    (drain-headers (.getInputStream client))
    (with-open [^Socket origin (connect-or-fail socks-opts client host port)]
      (write-ascii (.getOutputStream client) "HTTP/1.1 200 Connection Established\r\n\r\n")
      (relay client origin
             #(pump (.getInputStream origin) (.getOutputStream client))))))

(defn- handle-absolute [socks-opts ^Socket client ^String method ^String target ^String version]
  (let [uri (URI. target)
        host (strip-brackets (.getHost uri))
        port (if (pos? (.getPort uri)) (.getPort uri) 80)]
    (with-open [^Socket origin (connect-or-fail socks-opts client host port)]
      (forward-request-head (.getInputStream client) (.getOutputStream origin)
                            method uri version)
      (relay client origin
             #(when (forward-response-head (.getInputStream origin) (.getOutputStream client))
                (pump (.getInputStream origin) (.getOutputStream client)))))))

(defn- handle [socks-opts ^Socket client]
  (with-open [^Socket client client]
    (when-let [request-line (read-header-line (.getInputStream client))]
      (let [[method target version] (str/split request-line #" " 3)]
        (if (= "CONNECT" (str/upper-case (str method)))
          (handle-connect socks-opts client target)
          (handle-absolute socks-opts client method target (or version "HTTP/1.1")))))))

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
  started per distinct set of options and shared from then on."
  [socks-opts]
  (let [k (select-keys socks-opts [:host :port :user :pass])]
    @(get (swap! bridges update k (fn [d] (or d (delay (start-bridge k))))) k)))
