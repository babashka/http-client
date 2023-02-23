(ns babashka.http-client.internal
  {:no-doc true}
  (:refer-clojure :exclude [send get])
  (:require
   [babashka.http-client.interceptors :as interceptors]
   [babashka.http-client.internal.version :as iv]
   [clojure.string :as str]
   [babashka.http-client.internal.helpers :as aux])
  (:import
   [java.net URI URLEncoder]
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
   [java.time Duration]
   [java.util.function Supplier]))

(set! *warn-on-reflection* true)

(defn- ->follow-redirect [redirect]
  (case redirect
    :always HttpClient$Redirect/ALWAYS
    :never HttpClient$Redirect/NEVER
    :normal HttpClient$Redirect/NORMAL))

(defn- version-keyword->version-enum [version]
  (case version
    :http1.1 HttpClient$Version/HTTP_1_1
    :http2   HttpClient$Version/HTTP_2))

(defn ->timeout [t]
  (if (integer? t)
    (Duration/ofMillis t)
    t))

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
                 ssl-context
                 ssl-parameters
                 version]} opts]
     (cond-> (HttpClient/newBuilder)
       connect-timeout  (.connectTimeout (->timeout connect-timeout))
       cookie-handler   (.cookieHandler cookie-handler)
       executor         (.executor executor)
       follow-redirects (.followRedirects (->follow-redirect follow-redirects))
       priority         (.priority priority)
       proxy            (.proxy proxy)
       ssl-context      (.sslContext ssl-context)
       ssl-parameters   (.sslParameters ssl-parameters)
       version          (.version (version-keyword->version-enum version))))))

(defn client
  ([opts]
   {:client (.build (client-builder opts))
    :request (:request opts)
    :type :babashka.http-client/client}))

(def default-client-opts
  {:follow-redirects :always
   :request {:headers {:accept "*/*"
                       :accept-encoding ["gzip" "deflate"]
                       :user-agent (str "babashka.http-client/" iv/version)}}})

(def ^HttpClient default-client
  (delay (client default-client-opts)))

(defn- method-keyword->str [method]
  (str/upper-case (name method)))

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
    (HttpRequest$BodyPublishers/ofString (slurp body))

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
            param (str (url-encode (aux/coerce-key k)) "=" v)]
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
      (seq headers)            (.headers (into-array String (aux/coerce-headers headers)))
      method                   (.method (method-keyword->str method) (->body-publisher body))
      timeout                  (.timeout (->timeout timeout))
      uri                      (.uri (URI/create uri))
      version                  (.version (version-keyword->version-enum version)))))

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
    "HTTP_2"   :http2))

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
        request-interceptors (or (:interceptors req)
                                 interceptors/default-interceptors)
        req (merge-with merge-opts request-defaults req)
        req (apply-interceptors req request-interceptors :request)
        req' (ring->HttpRequest req)
        resp (if (:async req)
               (.sendAsync client req' (HttpResponse$BodyHandlers/ofInputStream))
               (.send client req' (HttpResponse$BodyHandlers/ofInputStream)))]
    (if raw resp
        (let [resp (then resp response->map)
              resp (then resp (fn [resp]
                                (assoc resp :request req)))]
          (reduce (fn [resp interceptor]
                    (if-let [f (:response interceptor)]
                      (then resp f)
                      resp))
                  resp (reverse (or (:interceptors req)
                                    interceptors/default-interceptors)))))))
