(ns babashka.http-client
  (:refer-clojure :exclude [send get])
  (:require [babashka.http-client.internal :as i]))

(def default-client-opts
  "Options used to create the (implicit) default client."
  i/default-client-opts)

(defn ->SSLContext
  "Constructs a `javax.net.ssl.SSLContext`.

  Options:

  * `:key-store` is a file, URI or URL or anything else that is compatible with `io/input-stream`, e.g. (io/resource somepath.p12)
  * `:key-store-pass` is the password for the keystore
  * `:key-store-type` is the type of keystore to create [note: not the type of the file] (default: pkcs12)
  * `:trust-store` is a file, URI or URL or anything else that is compatible with `io/input-stream`, e.g. (io/resource somepath.p12)
  * `:trust-store-pass` is the password for the trust store
  * `:trust-store-type` is the type of trust store to create [note: not the type of the file] (default: pkcs12)
  * `:insecure` if `true`, an insecure trust manager accepting all server certificates will be configured.

  Note that `:keystore` and `:truststore` can be set using the
  `javax.net.ssl.keyStore` and `javax.net.ssl.trustStore` System
  properties globally."
  [opts]
  (i/->SSLContext opts))

(defn client
  "Construct a custom client. To get the same behavior as the (implicit) default client, pass `default-client-opts`.

  Options:
  * `:follow-redirects` - `:never`, `:always` or `:normal`
  * `:connect-timeout` - connection timeout in milliseconds.
  * `:request` - default request options which will be used in requests made with this client.
  * `:executor` - a `java.util.concurrent.Executor`
  * `:ssl-context`: a `javax.net.ssl.SSLContext` or a map of options, see docstring of `->SSLContext`.

  Returns map with:

  * `:client`, a `java.net.http.HttpClient`.

  The map can be passed to `request` via the `:client` key.
  "
  ([opts]
   (i/client opts)))

(defn request
  "Perform request. Returns map with at least `:body`, `:status`

  Options:

  * `:uri` - the uri to request (required).
     May be a string or map of `:scheme` (required), `:host` (required), `:port`, `:path` and `:query`
  * `:headers` - a map of headers
  * `:method` - the request method: `:get`, `:post`, `:head`, `:delete`, `:patch` or `:put`
  * `:interceptors` - custom interceptor chain
  * `:client` - a client as produced by `client`. If not provided a default client will be used.
  * `:query-params` - a map of query params. The values can be a list to send multiple params with the same key.
  * `:form-params` - a map of form params to send in the request body.
  * `:body` - a file, inputstream or string to send as the request body.
  * `:basic-auth` - a sequence of `user` `password` or map with `:user` `:pass` used for basic auth.
  * `:async` - perform request asynchronously. The response will be a `CompletableFuture` of the response map.
  * `:async-then` - a function that is called on the async result if successful
  * `:async-catch` - a function that is called on the async result if exceptional
  * `:timeout` - request timeout in milliseconds.
  * `:version` - the HTTP version: `:http1.1` or `:http2`.
  "
  [opts]
  (i/request opts))

(defn get
  "Convenience wrapper for `request` with method `:get`"
  ([uri] (get uri nil))
  ([uri opts]
   (let [opts (assoc opts :uri uri :method :get)]
     (request opts))))

(defn delete
  "Convenience wrapper for `request` with method `:delete`"
  ([uri] (delete uri nil))
  ([uri opts]
   (let [opts (assoc opts :uri uri :method :delete)]
     (request opts))))

(defn head
  "Convenience wrapper for `request` with method `:head`"
  ([uri] (head uri nil))
  ([uri opts]
   (let [opts (assoc opts :uri uri :method :head)]
     (request opts))))

(defn post
  "Convenience wrapper for `request` with method `:post`"
  ([uri] (post uri nil))
  ([uri opts]
   (let [opts (assoc opts :uri uri :method :post)]
     (request opts))))

(defn patch
  "Convenience wrapper for `request` with method `:patch`"
  ([url] (patch url nil))
  ([url opts]
   (let [opts (assoc opts
                     :uri url
                     :method :patch)]
     (request opts))))

(defn put
  "Convenience wrapper for `request` with method `:put`"
  ([url] (put url nil))
  ([url opts]
   (let [opts (assoc opts
                     :uri url
                     :method :put)]
     (request opts))))
