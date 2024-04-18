(ns babashka.http-client
  (:refer-clojure :exclude [send get])
  (:require [babashka.http-client.internal :as i]))

(def default-client-opts
  "Options used to create the (implicit) default client."
  i/default-client-opts)

(defn ->ProxySelector
  "Constructs a `java.net.ProxySelector`.
  Options:
  * `:host` - string
  * `:port` - long"
  [opts]
  (i/->ProxySelector opts))

(defn ->SSLContext
  "Constructs a `javax.net.ssl.SSLContext`.

  Options:

  * `:key-store` - a file, URI or URL or anything else that is compatible with `io/input-stream`, e.g. (io/resource somepath.p12)
  * `:key-store-pass` - the password for the keystore
  * `:key-store-type` - the type of keystore to create [note: not the type of the file] (default: pkcs12)
  * `:trust-store` - a file, URI or URL or anything else that is compatible with `io/input-stream`, e.g. (io/resource somepath.p12)
  * `:trust-store-pass` - the password for the trust store
  * `:trust-store-type` - the type of trust store to create [note: not the type of the file] (default: pkcs12)
  * `:insecure` - if `true`, an insecure trust manager accepting all server certificates will be configured.

  Note that `:keystore` and `:truststore` can be set using the
  `javax.net.ssl.keyStore` and `javax.net.ssl.trustStore` System
  properties globally."
  [opts]
  (i/->SSLContext opts))

(defn ->Authenticator
  "Constructs a `java.net.Authenticator`.

  Options:

  * `:user` - the username
  * `:pass` - the password"
  [opts]
  (i/->Authenticator opts))

(defn ->CookieHandler
  "Constructs a `java.net.CookieHandler` using `java.net.CookieManager`.

    Options:

    * `:store` - an optional `java.net.CookieStore` implementation
    * `:policy` - a `java.net.CookiePolicy` or one of `:accept-all`, `:accept-none`, `:original-server`"
  [opts]
  (i/->CookieHandler opts))

(defn ->SSLParameters
  "Constructs a `javax.net.ssl.SSLParameters`.

   Options:

   * `:ciphers` - a list of cipher suite names
   * `:protocols` - a list of protocol names"
  [opts]
  (i/->SSLParameters opts))

(defn ->Executor
  "Constructs a `java.util.concurrent.Executor`.

   Options:

   * `:threads` - constructs a `ThreadPoolExecutor` with the specified number of threads"
  [opts]
  (i/->Executor opts))

(defn client
  "Construct a custom client. To get the same behavior as the (implicit) default client, pass `default-client-opts`.

  Options:
  * `:follow-redirects` - `:never`, `:always` or `:normal`
  * `:connect-timeout` - connection timeout in milliseconds.
  * `:request` - default request options which will be used in requests made with this client.
  * `:executor` - a `java.util.concurrent.Executor` or a map of options, see docstring of `->Executor`
  * `:ssl-context` - a `javax.net.ssl.SSLContext` or a map of options, see docstring of `->SSLContext`.
  * `:ssl-parameters` - a `javax.net.ssl.SSLParameters' or a map of options, see docstring of `->SSLParameters`.
  * `:proxy` - a `java.net.ProxySelector` or a map of options, see docstring of `->ProxySelector`.
  * `:authenticator` - a `java.net.Authenticator` or a map of options, see docstring of `->Authenticator`.
  * `:cookie-handler` - a `java.net.CookieHandler` or a map of options, see docstring of `->CookieHandler`.
  * `:version` - the HTTP version: `:http1.1` or `:http2`.
  * `:priority` - priority for HTTP2 requests, integer between 1-256 inclusive.

  Returns map with:

  * `:client` - a `java.net.http.HttpClient`.

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
  * `:client` - a client as produced by `client` or a clojure function. If not provided a default client will be used.
                When providing :client with a a clojure function, it will be called with the Clojure representation of
                the request which can be useful for testing.
  * `:query-params` - a map of query params. The values can be a list to send multiple params with the same key.
  * `:form-params` - a map of form params to send in the request body.
  * `:body` - a file, inputstream or string to send as the request body.
  * `:basic-auth` - a sequence of `user` `password` or map with `:user` `:pass` used for basic auth.
  * `:oauth-token` - a string token used for bearer auth.
  * `:async` - perform request asynchronously. The response will be a `CompletableFuture` of the response map.
  * `:async-then` - a function that is called on the async result if successful
  * `:async-catch` - a function that is called on the async result if exceptional
  * `:timeout` - request timeout in milliseconds
  * `:throw` - throw on exceptional status codes, all other than `#{200 201 202 203 204 205 206 207 300 301 302 303 304 307}`
  * `:version` - the HTTP version: `:http1.1` or `:http2`."
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
