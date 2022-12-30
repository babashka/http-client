(ns babashka.http-client
  (:refer-clojure :exclude [send get])
  (:require [clojure.string :as str])
  (:import [java.net URI URLEncoder]
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
           [java.util.concurrent CompletableFuture]
           [java.util.function Function Supplier]
           [java.time Duration]))

(def ^HttpClient default-client
  (delay (HttpClient/newHttpClient)))

(defn- method-keyword->str [method]
  (str/upper-case (name method)))

(def ^:private convert-headers-xf
  (mapcat
   (fn [[k v :as p]]
     (if (sequential? v)
       (interleave (repeat k) v)
       p))))

(defn- input-stream-supplier [s]
  (reify Supplier
    (get [_this] s)))

(defn- convert-body-publisher [body]
  (cond
    (nil? body)
    (HttpRequest$BodyPublishers/noBody)

    (string? body)
    (HttpRequest$BodyPublishers/ofString body)

    (instance? java.io.InputStream body)
    (HttpRequest$BodyPublishers/ofInputStream (input-stream-supplier body))

    (bytes? body)
    (HttpRequest$BodyPublishers/ofByteArray body)))

(defn- version-keyword->version-enum [version]
  (case version
    :http1.1 HttpClient$Version/HTTP_1_1
    :http2   HttpClient$Version/HTTP_2))

(defn- convert-timeout [t]
  (if (integer? t)
    (Duration/ofMillis t)
    t))

(defn- url-encode
  "Returns an UTF-8 URL encoded version of the given string."
  [^String unencoded]
  (URLEncoder/encode unencoded "UTF-8"))

(defn- coerce-key
  "Coreces a key to str"
  [k]
  (if (keyword? k)
    (-> k str (subs 1))
    (str k)))

(defn map->query-params [query-params-map]
  (loop [params* (transient [])
         kvs (seq query-params-map)]
    (if kvs
      (let [[k v] (first kvs)]
        (recur (conj! params* (str (url-encode (coerce-key k)) "=" (url-encode (str v)))) (next kvs)))
      (str/join "&" (persistent! params*)))))

(defn ->request-builder ^HttpRequest$Builder [opts]
  (let [{:keys [expect-continue
                headers
                method
                timeout
                uri
                version
                body]} opts
        uri (if-let [qp (:query-params opts)]
              (str uri "?" (map->query-params qp))
              uri)]
    (cond-> (HttpRequest/newBuilder)
      (some? expect-continue) (.expectContinue expect-continue)
      (seq headers)            (.headers (into-array String (eduction convert-headers-xf headers)))
      method                   (.method (method-keyword->str method) (convert-body-publisher body))
      timeout                  (.timeout (convert-timeout timeout))
      uri                      (.uri (URI/create uri))
      version                  (.version (version-keyword->version-enum version)))))

(defn ->request
  (^HttpRequest [req-map] (.build (->request-builder req-map))))

(def ^:private bh-of-string (HttpResponse$BodyHandlers/ofString))
(def ^:private bh-of-input-stream (HttpResponse$BodyHandlers/ofInputStream))
(def ^:private bh-of-byte-array (HttpResponse$BodyHandlers/ofByteArray))

(defn- ->body-handler [mode]
  (case mode
    nil bh-of-string
    :string bh-of-string
    :stream bh-of-input-stream
    :bytes bh-of-byte-array))

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

(defn request
  [{:keys [client raw as] :as req}]
  (let [^HttpClient client (or client @default-client)
        req' (->request req)
        resp (.send client req' (->body-handler as))]
    (if raw resp (response->map resp))))

(defn get
  ([uri] (get uri nil))
  ([uri opts]
   (let [opts (assoc opts :uri uri :method :get)]
     (request opts))))

(defn delete
  ([uri] (delete uri nil))
  ([uri opts]
   (let [opts (assoc opts :uri uri :method :delete)]
     (request opts))))

(defn head
  ([uri] (head uri nil))
  ([uri opts]
   (let [opts (assoc opts :uri uri :method :head)]
     (request opts))))
