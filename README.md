# Http-client

An HTTP client for Clojure and Babashka built on `java.net.http`.

## API

See [API.md](API.md).

## Status

This library is in flux. Feedback is welcome. It can be used in production, but
expect breaking changes. When this library is considered stable (API-wise) it
will be built into babashka.

The APIs in this library are mostly compatible with
[babashka.curl](https://github.com/babashka/babashka.curl), which is in turn
inspired by libraries like [clj-http](https://github.com/dakrone/clj-http).

``` clojure
(require '[babashka.http-client :as client])
(require '[clojure.java.io :as io]) ;; optional
(require '[cheshire.core :as json]) ;; optional
```

### GET

Simple `GET` request:

``` clojure
(client/get "https://httpstat.us/200")
;;=> {:status 200, :body "200 OK", :headers { ... }}
```

### Headers

Passing headers:

``` clojure
(def resp (client/get "https://httpstat.us/200" {:headers {"Accept" "application/json"}}))
(json/parse-string (:body resp)) ;;=> {"code" 200, "description" "OK"}
```

### Query parameters

Query parameters:

``` clojure
(->
  (client/get "https://postman-echo.com/get" {:query-params {"q" "clojure"}})
  :body
  (json/parse-string true)
  :args)
;;=> {:q "clojure"}
```

To send multiple params to the same key:
```clojure
;; https://postman-echo.com/get?q=clojure&q=curl

(-> (client/get "https://postman-echo.com/get" {:query-params [[:q "clojure"] [:q "curl"]]}) :body (json/parse-string true) :args)
;;=> {:q ["clojure" "curl"]}
```

### POST

A `POST` request with a `:body`:

``` clojure
(def resp (client/post "https://postman-echo.com/post" {:body "From Clojure"}))
(json/parse-string (:body resp)) ;;=> {"args" {}, "data" "", ...}
```

Posting a file as a `POST` body:

``` clojure
(:status (client/post "https://postman-echo.com/post" {:body (io/file "README.md")}))
;; => 200
```

Posting a stream as a `POST` body:

``` clojure
(:status (client/post "https://postman-echo.com/post" {:body (io/input-stream "README.md")}))
;; => 200
```

Posting form params:

``` clojure
(:status (client/post "https://postman-echo.com/post" {:form-params {"name" "Michiel"}}))
;; => 200
```

### Basic auth

Basic auth:

``` clojure
(:body (client/get "https://postman-echo.com/basic-auth" {:basic-auth ["postman" "password"]}))
;; => "{\"authenticated\":true}"
```

### Download binary

Download a binary file:

``` clojure
(io/copy
  (:body (client/get "https://github.com/babashka/babashka/raw/master/logo/icon.png"
    {:as :bytes}))
  (io/file "icon.png"))
(.length (io/file "icon.png"))
;;=> 7748
```

### Streaming

With `:as :stream`

``` clojure
(:body (client/get "https://github.com/babashka/babashka/raw/master/logo/icon.png"
    {:as :stream}))
```

will return the raw input stream.

### URL construction

Using the verbose `:uri` API for fine grained (and safer) URI construction:

``` clojure
(-> (client/request {:uri {:scheme "https"
                         :host   "httpbin.org"
                         :port   443
                         :path   "/get"
                         :query  "q=test"}})
    :body
    (json/parse-string true))
;;=>
{:args {:q "test"},
 :headers
 {:Accept "*/*",
  :Host "httpbin.org",
  :User-Agent "curl/7.64.1",
  :X-Amzn-Trace-Id
  "Root=1-5e63989e-7bd5b1dba75e951a84d61b6a"},
 :origin "46.114.35.45",
 :url "https://httpbin.org/get?q=test"}
```

### Redirects

The default client is configured to always follow redirects. To opt out of this behaviour, construct a custom client:

```clojure
(:status (client/get "https://httpstat.us/302" {:client (client/client {:follow-redirects :never})}))
;; => 302
(:status (client/get "https://httpstat.us/302" {:client (client/client {:follow-redirects :always})}))
;; => 200
```

### Exceptions

An `ExceptionInfo` will be thrown for all HTTP response status codes other than `#{200 201 202 203 204 205 206 207 300 301 302 303 304 307}`.

```clojure
(client/get "https://httpstat.us/404")
;;=> Execution error (ExceptionInfo) at babashka.client/request (curl.clj:228).
     status 404
(:status (ex-data *e))
;;=> 404
 ```

To opt out of an exception being thrown, set `:throw` to false.

```clojure
(:status (client/get "https://httpstat.us/404" {:throw false}))
;;=> 404
```

If the body is being returned as a stream then exceptions are never thrown and the `:exit` value is wrapped in a `Delay`.

```clojure
(:exit (client/get "https://httpstat.us/404" {:as :stream}))
;;=> #object[clojure.lang.Delay 0x75769ab0 {:status :pending, :val nil}]
(force *1)
;;=> 0
```

### Compression

To accept gzipped or zipped responses, use:

```
(client/get "https://api.stackexchange.com/2.2/sites" {:headers {"Accept-Encoding" ["gzip" "deflate"]}})
```

The above server only serves compressed responses, so if you remove the header, the request will fail.
Accepting compressed responses may become the default in a later version of this library.

### Interceptors

Babashka http-client interceptors are similar to Pedestal interceptors. They are maps of `:name` (a string), `:request` (a function), `:response` (a function).
An example is shown in this test:

``` clojure
(deftest interceptor-test
  (let [json-interceptor
        {:name ::json
         :description
         "A request with `:as :json` will automatically get the
         \"application/json\" accept header and the response is decoded as JSON."
         :request (fn [request]
                    (if (= :json (:as request))
                      (-> (assoc-in request [:headers :accept] "application/json")
                          ;; Read body as :string
                          ;; Mark request as amenable to json decoding
                          (assoc :as :string ::json true))
                      request))
         :response (fn [response]
                     (if (get-in response [:request ::json])
                       (update response :body #(json/parse-string % true))
                       response))}
        ;; Add json interceptor add beginning of chain
        ;; It will be the first to see the request and the last to see the response
        interceptors (cons json-interceptor interceptors/default-interceptors)
        ]
    (testing "interceptors on request"
      (let [resp (client/get "https://httpstat.us/200"
                             {:interceptors interceptors
                              :as :json})]
        (is (= 200 (-> resp :body
                       ;; response as JSON
                       :code)))))))
```

A `:request` function is executed when the request is built and the `:response`
function is executed on the response. Default interceptors are in
`babashka.http-client.interceptors/default-interceptors`.  Interceptors can be
configured on the level of requests by passing a modified `:interceptors`
chain.

### Async

To execute request asynchronously, use `:async true`. The response will be a
`CompletableFuture` with the response map.

``` clojure
(-> (client/get "https://clojure.org" {:async true}) .get :status)
;;=> 200
```

## Test

``` clojure
$ bb test:clj
```

## Credits

This library has borrowed liberally from [java-http-clj](https://github.com/schmee/java-http-clj) and [hato](https://github.com/gnarroway/hato), both available under the MIT license.

## License

Copyright © 2022 - 2023 Michiel Borkent

Distributed under the MIT License. See LICENSE.
