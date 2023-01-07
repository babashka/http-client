(ns babashka.http-client
  (:refer-clojure :exclude [send get])
  (:require [babashka.http-client.internal :as i]))

(defn default-client
  "Get the default client, intended for customization"
  []
  (i/client {:follow-redirects :always}))

(defn request
  "Perform request. Returns map with at least `:body`, `:status`

  Options:

  * `:uri` - the uri to request (required).
     May be a string or map of `:schema` (required), `:host` (required), `:port`, `:path` and `:query`
  * `:method` - the request method: `:get`, `:post`, `:head`, `:delete` or `:patch`
  * `:interceptors` - custom interceptor chain
  * `:client` - a client as produced by `client`. If not provided a default client will be used.
  * `:async` - perform request asynchronously. The response will be a `CompletableFuture` of the response map.
  * `:timeout` - request timeout in milliseconds.
  * `:version` - the HTTP version: `:http1.1` or `:http2`.
  "
  [opts]
  (let [opts (if-not (contains? opts :client)
               (assoc opts :client (default-client))
               opts)]
    (i/request opts)))

(defn client
  "Construct a custom client.

  Options:
  * `:follow-redirects` - `:never`, `:always` or `:normal`
  * `:connect-timeout` - connection timeout in milliseconds.
  "
  [opts]
  (i/client opts))

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
