(ns babashka.http-client.interceptors
  (:refer-clojure :exclude [send get])
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str])
  (:import
   [java.net URLEncoder]
   [java.util Base64]
   [java.util.zip
    GZIPInputStream
    Inflater
    InflaterInputStream
    ZipException]))

(set! *warn-on-reflection* true)

(defn- coerce-key
  "Coerces a key to str"
  [k]
  (if (keyword? k)
    (-> k str (subs 1))
    (str k)))

(defn- url-encode
  "Returns an UTF-8 URL encoded version of the given string."
  [^String unencoded]
  (URLEncoder/encode unencoded "UTF-8"))

(defn- map->query-params [query-params-map]
  (loop [params* (transient [])
         kvs (seq query-params-map)]
    (if kvs
      (let [[k v] (first kvs)]
        (if (and (coll? v)
                 (seqable? v))
          (recur params* (concat
                          (map (fn [v]
                                 [k v]) v)
                          (rest kvs)))
          (recur (conj! params* (str (url-encode (coerce-key k)) "=" (url-encode (str v)))) (next kvs))))
      (str/join "&" (persistent! params*)))))

(defn- map->form-params [form-params-map]
  (loop [params* (transient [])
         kvs (seq form-params-map)]
    (if kvs
      (let [[k v] (first kvs)
            v (url-encode (str v))
            param (str (url-encode (coerce-key k)) "=" v)]
        (recur (conj! params* param) (next kvs)))
      (str/join "&" (persistent! params*)))))

(defn- basic-auth-value [x]
  (let [[user pass] (if (sequential? x) x [(clojure.core/get x :user) (clojure.core/get x :pass)])
        basic-auth (str user ":" pass)]
    (str "Basic " (.encodeToString (Base64/getEncoder) (.getBytes basic-auth "UTF-8")))))

(def basic-auth
  "Request: adds `:authorization` header based on `:basic-auth` (a map
  of `:user` and `:pass`) in request."
  {:name ::basic-auth
   :request (fn [opts]
              (if-let [basic-auth (:basic-auth opts)]
                (let [headers (:headers opts)
                      auth (basic-auth-value basic-auth)
                      headers (assoc headers :authorization auth)
                      opts (assoc opts :headers headers)]
                  opts)
                opts))})

(def accept-header
  "Request: adds `:accept` header. Only supported value is `:json`."
  {:name ::accept-header
   :request
   (fn [opts]
     (if-let [accept (:accept opts)]
       (let [headers (:headers opts)
             accept-header (case accept
                             :json "application/json")
             headers (assoc headers :accept accept-header)
             opts (assoc opts :headers headers)]
         opts)
       opts))})

(def query-params
  "Request: encodes `:query-params` map and appends to `:uri`."
  {:name ::query-params
   :request (fn [opts]
              (if-let [qp (:query-params opts)]
                (assoc opts :uri (str (:uri opts) "?" (map->query-params qp)))
                opts))})

(def form-params
  "Request: encodes `:form-params` map and adds `:body`."
  {:name ::form-params
   :request (fn [opts]
              (if-let [fp (:form-params opts)]
                (let [opts (assoc opts :body (map->form-params fp))
                      ct (get-in opts [:headers :content-type])]
                  (if ct
                    opts
                    (assoc-in opts [:headers :content-type] "application/x-www-form-urlencoded")))
                opts))})

(defmulti ^:private do-decompress-body
  (fn [resp]
    (when-let [encoding (get-in resp [:headers "content-encoding"])]
      (str/lower-case encoding))))

(defn- gunzip
  "Returns a gunzip'd version of the given byte array or input stream."
  [b]
  (when b
    (when (instance? java.io.InputStream b)
      (GZIPInputStream. b))))

(defmethod do-decompress-body "gzip"
  [resp]
  (update resp :body gunzip))

(defn- inflate
  "Returns a zlib inflate'd version of the given byte array or InputStream. Taken from hato."
  [b]
  (when b
    ;; This weirdness is because HTTP servers lie about what kind of deflation
    ;; they're using, so we try one way, then if that doesn't work, reset and
    ;; try the other way
    (let [stream (java.io.BufferedInputStream. b)
          _ (.mark stream 512)
          iis (InflaterInputStream. stream)
          readable? (try (.read iis) true
                         (catch ZipException _ false))
          _ (.reset stream)
          iis' (if readable?
                 (InflaterInputStream. stream)
                 (InflaterInputStream. stream (Inflater. true)))]

      iis')))

(defmethod do-decompress-body "deflate"
  [resp]
  (update resp :body inflate))

(defmethod do-decompress-body :default [resp]
  resp)

(def decompress-body
  "Response: decompresses body based on  \"content-encoding\" header. Valid values: `gzip` and `deflate`."
  {:name ::decompress
   :response (fn [resp]
               (if (false? (:decompress-body (:request resp)))
                 resp
                 (do-decompress-body resp)))})

(defn- stream-bytes [is]
  (let [baos (java.io.ByteArrayOutputStream.)]
    (io/copy is baos)
    (.toByteArray baos)))

(def decode-body
  "Response: based on the value of `:as` in request, decodes as `:string`, `:stream` or `:bytes`. Defaults to `:string`."
  {:name ::decode-body
   :response (fn [resp]
               (let [as (or (-> resp :request :as) :string)
                     body (:body resp)
                     body (case as
                            :string (slurp body)
                            :stream body
                            :bytes (stream-bytes body))]
                 (assoc resp :body body)))})

(def construct-uri
  "Request: construct uri from map"
  {:name ::construct-uri
   :request (fn [req]
              (let [uri (:uri req)
                    uri (cond (string? uri) uri
                              (map? uri)
                              (str (java.net.URI. ^String (:scheme uri)
                                                  ^String (:user uri)
                                                  ^String (:host uri)
                                                  ^Integer (:port uri)
                                                  ^String (:path uri)
                                                  ^String (:query uri)
                                                  ^String (:fragment uri)))
                              :else uri)]
                (assoc req :uri uri)))})

(def unexceptional-statuses
  #{200 201 202 203 204 205 206 207 300 301 302 303 304 307})

(def throw-on-exceptional-status-code
  "Response: throw on exceptional status codes"
  {:name ::throw-on-exceptional-status-code
   :response (fn [resp]
               (let [status (:status resp)]
                 (if (or (false? (some-> resp :request :throw))
                         (contains? unexceptional-statuses status))
                   resp
                   (throw (ex-info (str "Exceptional status code: " status) resp)))))})

(def default-interceptors
  "Default interceptor chain. Interceptors are called in order for request and in reverse order for response."
  [throw-on-exceptional-status-code
   construct-uri
   accept-header
   basic-auth
   query-params
   form-params
   decode-body
   decompress-body])

#_(defn insert-interceptors-before [chain before & interceptors]
    (let [[pre _ post] (partition-by #(= before %) chain)]
      (reduce into [] [pre (conj (vec interceptors) before) post])))

#_(defn insert-interceptors-after [chain after & interceptors]
    (let [[pre _ post] (partition-by #(= after %) chain)]
      (reduce into [] [pre (list* after interceptors) post])))
