(ns babashka.http-client.internal
  (:refer-clojure :exclude [send get])
  (:require
   [babashka.http-client.interceptors :as interceptors]
   [babashka.http-client.internal.version :as iv]
   [clojure.java.io :as io]
   [clojure.string :as str])
  (:import
   [java.net URI URLEncoder Authenticator PasswordAuthentication]
   [java.net.http
    HttpClient
    HttpClient$Builder
    HttpClient$Redirect
    HttpClient$Version
    HttpRequest
    HttpRequest$BodyPublishers
    HttpRequest$Builder
    HttpResponse
    HttpResponse$BodyHandlers]
   [java.security KeyStore SecureRandom]
   [java.security.cert X509Certificate]
   [java.time Duration]
   [java.util.concurrent CompletableFuture]
   [java.util.function Function Supplier]
   [javax.net.ssl KeyManagerFactory TrustManagerFactory SSLContext TrustManager]))

(set! *warn-on-reflection* true)

(defn- ->follow-redirect [redirect]
  (case redirect
    :always HttpClient$Redirect/ALWAYS
    :never HttpClient$Redirect/NEVER
    :normal HttpClient$Redirect/NORMAL))

(defn- version-keyword->version-enum [version]
  (case version
    :http1.1 HttpClient$Version/HTTP_1_1
    :http2 HttpClient$Version/HTTP_2))

(defn ->timeout [t]
  (if (integer? t)
    (Duration/ofMillis t)
    t))

(defn- load-keystore
  ^KeyStore [store store-type store-pass]
  (when store
    (with-open [kss (io/input-stream store)]
      (doto (KeyStore/getInstance store-type)
        (.load kss (char-array store-pass))))))

(defmacro if-bb [then else]
  (if (System/getProperty "babashka.version")
    then else))

(def insecure-tm
  (delay
    (if-bb
     (reify javax.net.ssl.X509TrustManager
       (checkClientTrusted [_ _ _])
       (checkServerTrusted [_ _ _])
       (getAcceptedIssuers [_] (into-array X509Certificate [])))
     (proxy [javax.net.ssl.X509ExtendedTrustManager] []
       (checkClientTrusted
         ([_ _])
         ([_ _ _]))
       (checkServerTrusted
         ([_ _])
         ([_ _ _]))
       (getAcceptedIssuers [] (into-array X509Certificate []))))))

(defn ->SSLContext
  [v]
  (if (instance? SSLContext v)
    v
    (let [{:keys [key-store key-store-type key-store-pass trust-store trust-store-type trust-store-pass insecure]} v
          ;; compatibility with hato
          key-store-type (or key-store-type (:keystore-type v) "pkcs12")
          trust-store-type (or trust-store-type "pkcs12")
          key-managers (when-let [ks (load-keystore key-store key-store-type key-store-pass)]
                         (.getKeyManagers (doto (KeyManagerFactory/getInstance (KeyManagerFactory/getDefaultAlgorithm))
                                            (.init ks (char-array key-store-pass)))))

          trust-managers (if insecure
                           (into-array TrustManager [@insecure-tm])
                           (when-let [ts (load-keystore trust-store trust-store-type trust-store-pass)]
                             (.getTrustManagers (doto (TrustManagerFactory/getInstance (TrustManagerFactory/getDefaultAlgorithm))
                                                  (.init ts)))))]

      (doto (SSLContext/getInstance "TLS")
        (.init key-managers
               trust-managers
               (SecureRandom.))))))

(defn ->ProxySelector
  [opts]
  (if (instance? java.net.ProxySelector opts)
    opts
    (let [{:keys [host port]} opts]
      (cond (and host port)
            (java.net.ProxySelector/of (java.net.InetSocketAddress. ^String host ^long port))))))

(defn ->Authenticator
  [v]
  (if (instance? Authenticator v)
    v
    (let [{:keys [user pass]} v]
      (when (and user pass)
        (proxy [Authenticator] []
          (getPasswordAuthentication []
            (PasswordAuthentication. user (char-array pass))))))))

(defn client-builder
  (^HttpClient$Builder []
   (client-builder {}))
  (^HttpClient$Builder [opts]
   (let [{:keys [connect-timeout
                 cookie-handler
                 executor
                 follow-redirects
                 priority
                 proxy
                 authenticator
                 ssl-context
                 ssl-parameters
                 version]} opts]
     (cond-> (HttpClient/newBuilder)
       connect-timeout (.connectTimeout (->timeout connect-timeout))
       cookie-handler (.cookieHandler cookie-handler)
       executor (.executor executor)
       follow-redirects (.followRedirects (->follow-redirect follow-redirects))
       priority (.priority priority)
       authenticator (.authenticator (->Authenticator authenticator))
       proxy (.proxy (->ProxySelector proxy))
       ssl-context (.sslContext (->SSLContext ssl-context))
       ssl-parameters (.sslParameters ssl-parameters)
       version (.version (version-keyword->version-enum version))))))

(defn client
  ([opts]
   {:client (.build (client-builder opts))
    :request (:request opts)
    :type :babashka.http-client/client}))

(def default-client-opts
  {:follow-redirects :normal
   :request {:headers {:accept "*/*"
                       :accept-encoding ["gzip" "deflate"]
                       :user-agent (str "babashka.http-client/" iv/version)}}})

(def ^HttpClient default-client
  (delay (client default-client-opts)))

(defn- method-keyword->str [method]
  (str/upper-case (name method)))

(defn- coerce-key
  "Coerces a key to str"
  [k]
  (if (keyword? k)
    (-> k str (subs 1))
    (str k)))

(defn ^:private coerce-headers
  [headers]
  (mapcat
   (fn [[k v]]
     (if (sequential? v)
       (interleave (repeat (coerce-key k)) v)
       [(coerce-key k) v]))
   headers))

(defn- input-stream-supplier [s]
  (reify Supplier
    (get [_this] s)))

(defn- ->body-publisher [body]
  (cond
    (nil? body)
    (HttpRequest$BodyPublishers/noBody)

    (string? body)
    (HttpRequest$BodyPublishers/ofString body)

    (instance? java.io.InputStream body)
    (HttpRequest$BodyPublishers/ofInputStream (input-stream-supplier body))

    (bytes? body)
    (HttpRequest$BodyPublishers/ofByteArray body)

    (instance? java.io.File body)
    (let [^java.nio.file.Path path (.toPath (io/file body))]
      (HttpRequest$BodyPublishers/ofFile path))

    (instance? java.nio.file.Path body)
    (let [^java.nio.file.Path path body]
      (HttpRequest$BodyPublishers/ofFile path))

    :else
    (throw (ex-info (str "Don't know how to convert " (type body) "to body")
                    {:body body}))))

(defn- url-encode
  "Returns an UTF-8 URL encoded version of the given string."
  [^String unencoded]
  (URLEncoder/encode unencoded "UTF-8"))

(defn map->form-params [form-params-map]
  (loop [params* (transient [])
         kvs (seq form-params-map)]
    (if kvs
      (let [[k v] (first kvs)
            v (url-encode (str v))
            param (str (url-encode (coerce-key k)) "=" v)]
        (recur (conj! params* param) (next kvs)))
      (str/join "&" (persistent! params*)))))

(defn ->request-builder ^HttpRequest$Builder [opts]
  (let [{:keys [expect-continue
                headers
                method
                timeout
                uri
                version
                body]} opts]
    (cond-> (HttpRequest/newBuilder)
      (some? expect-continue) (.expectContinue expect-continue)
      (seq headers) (.headers (into-array String (coerce-headers headers)))
      method (.method (method-keyword->str method) (->body-publisher body))
      timeout (.timeout (->timeout timeout))
      uri (.uri ^URI uri)
      version (.version (version-keyword->version-enum version)))))

(defn- apply-interceptors [init interceptors k]
  (reduce (fn [acc i]
            (if-let [f (clojure.core/get i k)]
              (f acc)
              acc))
          init interceptors))

(defn ring->HttpRequest
  (^HttpRequest [req-map]
   (.build (->request-builder req-map))))

(defn- version-enum->version-keyword [^HttpClient$Version version]
  (case (.name version)
    "HTTP_1_1" :http1.1
    "HTTP_2" :http2))

(defn response->map [^HttpResponse resp]
  {:status (.statusCode resp)
   :body (.body resp)
   :version (-> resp .version version-enum->version-keyword)
   :headers (into {}
                  (map (fn [[k v]] [k (if (= 1 (count v))
                                        (first v)
                                        (vec v))]))
                  (.map (.headers resp)))})

(defn then [x f]
  (if (instance? java.util.concurrent.CompletableFuture x)
    (.thenApply ^java.util.concurrent.CompletableFuture x
                ^java.util.function.Function
                (reify java.util.function.Function
                  (apply [_ args]
                    (f args))))
    (f x)))

(defn merge-opts [x y]
  (if (and (map? x) (map? y))
    (merge x y)
    y))

(defn request
  [{:keys [client raw] :as req}]
  (let [client (or client @default-client)
        request-defaults (:request client)
        ^HttpClient client (or (:client client) client)
        req (merge-with merge-opts request-defaults req)
        request-interceptors (or (:interceptors req)
                                 interceptors/default-interceptors)
        req (apply-interceptors req request-interceptors :request)
        req' (ring->HttpRequest req)
        async (:async req)
        resp (if async
               (.sendAsync client req' (HttpResponse$BodyHandlers/ofInputStream))
               (.send client req' (HttpResponse$BodyHandlers/ofInputStream)))]
    (if raw resp
        (let [resp (then resp response->map)
              resp (then resp (fn [resp]
                                (assoc resp :request req)))
              resp (reduce (fn [resp interceptor]
                             (if-let [f (:response interceptor)]
                               (then resp f)
                               resp))
                           resp (reverse (or (:interceptors req)
                                             interceptors/default-interceptors)))]
          (if async
            (-> ^CompletableFuture resp
                (.thenApply
                 (reify Function
                   (apply [_ resp]
                     (if-let [then-fn (:async-then req)]
                       (then-fn resp)
                       resp))))
                (.exceptionally
                 (reify Function
                   (apply [_ e]
                     (let [^Throwable e e]
                       (if-let [catch-fn (:async-catch req)]
                         (catch-fn (let [cause (ex-cause e)]
                                     {:ex e
                                      :ex-cause cause
                                      :ex-data (ex-data (or cause e))
                                      :ex-message (ex-message (or cause e))
                                      :request req}))
                         resp))))))
            resp)))))
