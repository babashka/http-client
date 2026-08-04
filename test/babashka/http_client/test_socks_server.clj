(ns babashka.http-client.test-socks-server
  "Minimal SOCKS5 server for tests."
  (:require
   [clojure.string :as str])
  (:import
   [java.io InputStream OutputStream]
   [java.net InetAddress InetSocketAddress ServerSocket Socket]
   [java.nio.charset StandardCharsets]))

(defn- read-n ^bytes [^InputStream in n]
  (let [buf (byte-array n)]
    (loop [off 0]
      (if (< off n)
        (let [r (.read in buf off (- n off))]
          (when (neg? r) (throw (ex-info "client closed the connection" {})))
          (recur (+ off r)))
        buf))))

(defn- ub [^bytes buf i]
  (bit-and (aget buf i) 0xFF))

(defn- write-bytes [^OutputStream out coll]
  (.write out (byte-array (map unchecked-byte coll)))
  (.flush out))

(defn- pump [^InputStream in ^OutputStream out]
  (let [buf (byte-array 16384)]
    (loop []
      (let [r (.read in buf)]
        (when (pos? r)
          (.write out buf 0 r)
          (.flush out)
          (recur))))))

(defn- quietly [f]
  (try (f) (catch Exception _ nil)))

(defn- negotiate-auth [^InputStream in ^OutputStream out user pass]
  (let [nmethods (ub (read-n in 2) 1)
        methods (read-n in nmethods)
        offered (into #{} (map #(ub methods %)) (range nmethods))]
    (if (and user pass)
      (if (contains? offered 2)
        (do (write-bytes out [5 2])
            (read-n in 1)
            (let [u (String. (read-n in (ub (read-n in 1) 0)) StandardCharsets/UTF_8)
                  p (String. (read-n in (ub (read-n in 1) 0)) StandardCharsets/UTF_8)]
              (if (and (= user u) (= pass p))
                (do (write-bytes out [1 0]) true)
                (do (write-bytes out [1 1]) false))))
        (do (write-bytes out [5 0xFF]) false))
      (if (contains? offered 0)
        (do (write-bytes out [5 0]) true)
        (do (write-bytes out [5 0xFF]) false)))))

(defn- read-target
  "Reads the connect request with a single read, the way microsocks does. A
  client that splits the request across writes puts it on the wire in parts and
  must fail here rather than only against a real proxy."
  [^InputStream in]
  (let [^bytes buf (byte-array 262)
        n (.read in buf)
        _ (when (< n 5) (throw (ex-info "short SOCKS5 request" {:read n})))
        [host end] (case (long (ub buf 3))
                     1 [(str/join "." (map #(ub buf (+ 4 %)) (range 4))) 8]
                     3 (let [len (ub buf 4)]
                         [(String. buf 5 ^int len StandardCharsets/UTF_8) (+ 5 len)])
                     ;; Expanded form, so the test does not depend on how the
                     ;; canonical short form compresses zero groups.
                     4 [(str/join ":" (map (fn [i]
                                             (Integer/toHexString
                                              (+ (* 256 (ub buf (+ 4 (* 2 i))))
                                                 (ub buf (+ 5 (* 2 i))))))
                                           (range 8)))
                        20])]
    (when (< n (+ end 2))
      (throw (ex-info "SOCKS5 request arrived in parts" {:read n :expected (+ end 2)})))
    [host (+ (* 256 (ub buf end)) (ub buf (inc end)))]))

(defn- handle [^Socket client {:keys [user pass targets]}]
  (with-open [client client]
    (let [in (.getInputStream client)
          out (.getOutputStream client)]
      (when (negotiate-auth in out user pass)
        (let [[host port] (read-target in)]
          (swap! targets conj [host port])
          (with-open [origin (Socket.)]
            (.connect origin (InetSocketAddress. ^String host ^long (long port)))
            (write-bytes out [5 0 0 1 0 0 0 0 0 0])
            (let [close-both (fn []
                               (quietly #(.close origin))
                               (quietly #(.close client)))
                  t (Thread. ^Runnable (fn []
                                         (quietly #(pump (.getInputStream client)
                                                         (.getOutputStream origin)))
                                         (close-both)))]
              (.setDaemon t true)
              (.start t)
              (quietly #(pump (.getInputStream origin) (.getOutputStream client)))
              (close-both)
              (.join t 1000))))))))

(defn start
  "Starts a SOCKS5 server on an ephemeral loopback port. With `:user` and `:pass`
  it demands user and password authentication. Returns a map with `:port`,
  `:targets` and `:stop`."
  ([] (start {}))
  ([{:keys [user pass]}]
   (let [server (ServerSocket. 0 50 (InetAddress/getByName "127.0.0.1"))
         targets (atom [])
         opts {:user user :pass pass :targets targets}
         accept-loop (fn []
                       (loop []
                         (when-not (.isClosed server)
                           (when-let [client (try (.accept server) (catch Exception _ nil))]
                             (doto (Thread. ^Runnable (fn [] (quietly #(handle client opts))))
                               (.setDaemon true)
                               (.start)))
                           (recur))))
         thread (Thread. ^Runnable accept-loop)]
     (.setDaemon thread true)
     (.start thread)
     {:port (.getLocalPort server)
      :targets targets
      :stop #(.close server)})))
