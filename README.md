# http-client

[![Clojars Project](https://img.shields.io/clojars/v/org.babashka/http-client.svg)](https://clojars.org/org.babashka/http-client)
[![bb built-in](https://raw.githubusercontent.com/babashka/babashka/master/logo/built-in-badge.svg)](https://babashka.org)

An HTTP client for Clojure and Babashka built on `java.net.http`.

## API

See [API.md](API.md).

> NOTE: The `babashka.http-client` library is built-in as of babashka version 1.1.171.

## Stability

The `babashka.http-client` namespace can be considered stable. The
`babashka.http-client.interceptors` namespace may still undergo some breaking
changes.

## Installation

Use as a dependency in `deps.edn` or `bb.edn`:

``` clojure
org.babashka/http-client {:mvn/version "0.3.11"}
```

## Rationale

Babashka has several built-in options for making HTTP requests, including:

- [babashka.curl](https://github.com/babashka/babashka.curl)
- [http-kit](https://github.com/http-kit/http-kit)
- [java.net.http](https://docs.oracle.com/en/java/javase/17/docs/api/java.net.http/java/net/http/package-summary.html)

In addition, it allows to use several libraries to be used as a dependency:

- [java-http-clj](https://github.com/schmee/java-http-clj)
- [hato](https://github.com/gnarroway/hato)
- [clj-http-lite](https://github.com/clj-commons/clj-http-lite)

The built-in clients come with their own trade-offs. E.g. babashka.curl shells
out to `curl` which on Windows requires your local `curl` to be
updated. Http-kit buffers the entire response in memory. Using `java.net.http`
directly can be a bit verbose.

Babashka's http-client aims to be a good default for most scripting use cases
and is built on top of `java.net.http` and can be used as a dependency-free JVM
library as well. The API is mostly compatible with babashka.curl so it can be
used as a drop-in replacement. The other built-in solutions will not be removed
any time soon.

## Usage

The APIs in this library are mostly compatible with
[babashka.curl](https://github.com/babashka/babashka.curl), which is in turn
inspired by libraries like [clj-http](https://github.com/dakrone/clj-http).

``` clojure
(require '[babashka.http-client :as http])
(require '[clojure.java.io :as io]) ;; optional
(require '[cheshire.core :as json]) ;; optional
```

### GET

Simple `GET` request:

``` clojure
(http/get "https://httpstat.us/200")
;;=> {:status 200, :body "200 OK", :headers { ... }}
```

### Headers

Passing headers:

``` clojure
(def resp (http/get "https://httpstat.us/200" {:headers {"Accept" "application/json"}}))
(json/parse-string (:body resp)) ;;=> {"code" 200, "description" "OK"}
```

Headers may be provided as keywords as well:

``` clojure
{:headers {:content-type "application/json"}}
```

### Query parameters

Query parameters:

``` clojure
(->
  (http/get "https://postman-echo.com/get" {:query-params {"q" "clojure"}})
  :body
  (json/parse-string true)
  :args)
;;=> {:q "clojure"}
```

To send multiple params to the same key:
```clojure
;; https://postman-echo.com/get?q=clojure&q=curl

(-> (http/get "https://postman-echo.com/get" {:query-params {:q ["clojure "curl"]}})
    :body (json/parse-string true) :args)
;;=> {:q ["clojure" "curl"]}
```

### POST

A `POST` request with a `:body`:

``` clojure
(def resp (http/post "https://postman-echo.com/post" {:body "From Clojure"}))
(json/parse-string (:body resp)) ;;=> {"args" {}, "data" "From Clojure", ...}
```

A `POST` request with a JSON `:body`:

``` clojure
(def resp (http/post "https://postman-echo.com/post"
                     {:headers {:content-type "application/json"}
                      :body (json/encode {:a 1 :b "2"})}))
(:data (json/parse-string (:body resp) true)) ;;=> {:a 1, :b "2"}
```

Posting a file as a `POST` body:

``` clojure
(:status (http/post "https://postman-echo.com/post" {:body (io/file "README.md")}))
;; => 200
```

Posting a stream as a `POST` body:

``` clojure
(:status (http/post "https://postman-echo.com/post" {:body (io/input-stream "README.md")}))
;; => 200
```

Posting form params:

``` clojure
(:status (http/post "https://postman-echo.com/post" {:form-params {"name" "Michiel"}}))
;; => 200
```

### Basic auth

Basic auth:

``` clojure
(:body (http/get "https://postman-echo.com/basic-auth" {:basic-auth ["postman" "password"]}))
;; => "{\"authenticated\":true}"
```
### Oauth token

Oauth token:

``` clojure
(:body (http/get "https://httpbin.org/bearer" {:oauth-token "qwertyuiop"}))
;; => "{\n  \"authenticated\": true, \n  \"token\": \"qwertyuiop\"\n}\n"
```

### Streaming

With `:as :stream`:

``` clojure
(:body (http/get "https://github.com/babashka/babashka/raw/master/logo/icon.png"
    {:as :stream}))
```

will return the raw input stream.

### Download binary

Download a binary file:

``` clojure
(io/copy
  (:body (http/get "https://github.com/babashka/babashka/raw/master/logo/icon.png"
    {:as :stream}))
  (io/file "icon.png"))
(.length (io/file "icon.png"))
;;=> 7748
```

To obtain an in-memory byte array you can use `:as :bytes`.

### URI construction

Using the verbose `:uri` API for fine grained (and safer) URI construction:

``` clojure
(-> (http/request {:uri {:scheme "https"
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
  :User-Agent "Java-http-client/11.0.17"
  :X-Amzn-Trace-Id
  "Root=1-5e63989e-7bd5b1dba75e951a84d61b6a"},
 :origin "46.114.35.45",
 :url "https://httpbin.org/get?q=test"}
```

### Custom client

The default client in babashka.http-client is constructed conceptually as follows:

``` clojure
(def client (http/client http/default-client-opts))
```

To pass more options in addition to the default options, you can use `http/default-client-opts` and associate more options:

``` clojure
(def client (http/client (assoc-in http/default-client-opts [:ssl-context :insecure] true)))
```

Then use the custom client with HTTP requests:

``` clojure
(http/get "https://clojure.org" {:client client})
```

### Redirects

The default client is configured to always follow redirects. To opt out of this behaviour, construct a custom client:

```clojure
(:status (http/get "https://httpstat.us/302" {:client (http/client {:follow-redirects :never})}))
;; => 302
(:status (http/get "https://httpstat.us/302" {:client (http/client {:follow-redirects :always})}))
;; => 200
```

### Exceptions

An `ExceptionInfo` will be thrown for all HTTP response status codes other than `#{200 201 202 203 204 205 206 207 300 301 302 303 304 307}`.

```clojure
user=> (http/get "https://httpstat.us/404")
Execution error (ExceptionInfo) at babashka.http-client.interceptors/fn (interceptors.clj:194).
Exceptional status code: 404
 ```

To opt out of an exception being thrown, set `:throw` to false.

```clojure
(:status (http/get "https://httpstat.us/404" {:throw false}))
;;=> 404
```

### Multipart

To perform a multipart request, supply `:multipart` with a sequence of maps with the following options:

- `:name`: The name of the param
- `:part-name`: Override for `:name`
- `:content`: The part's data. May be string or something that can be fed into `clojure.java.io/input-stream`
- `:file-name`: The part's file name. If the `:content` is a file, the name of the file will be used, unless `:file-name` is set.
- `:content-type`: The part's content type. By default, if `:content` is a string it will be `text/plain; charset=UTF-8`; if `:content` is a file it will attempt to guess the best content type or fallback to `application/octet-stream`.

An example request:

``` clojure
(http/post "https://postman-echo.com/post"
           {:multipart [{:name "title" :content "My Title"}
                        {:name "Content/type" :content "image/jpeg"}
                        {:name "file" :content (io/file "foo.jpg") :file-name "foobar.jpg"}]})
```

### Compression

To accept gzipped or zipped responses, use:

``` clojure
(http/get "https://api.stackexchange.com/2.2/sites"
  {:headers {"Accept-Encoding" ["gzip" "deflate"]}})
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
      (let [resp (http/get "https://httpstat.us/200"
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

#### Testing interceptors

For testing interceptors it can be useful to use the `:client` option in combination with a
Clojure function. When passing a function, the request won't be converted to a
`java.net.http.Request` but just passed as a ring request to the function. The
function is expected to return a ring response:

``` clojure
(http/get "https://clojure.org" {:client (fn [req] {:body 200})})
```

### Async

To execute request asynchronously, use `:async true`. The response will be a
`CompletableFuture` with the response map.

``` clojure
(-> (http/get "https://clojure.org" {:async true}) deref :status)
;;=> 200
```

### Timeouts

Two different timeouts can be set:

- The connection timeout, `:connect-timeout`, in `http/client`
- The request `:timeout` in `http/request`

Alternatively you can use `:async` + `deref` with a timeout + default value:

```
(let [resp (http/get "https://httpstat.us/200?sleep=5000" {:async true})] (deref resp 1000 ::too-late))
;;=> :user/too-late
```

## Logging and debug

If you need to debug HTTP requests you need to add a JVM system property with some debug options:
`"-Djdk.httpclient.HttpClient.log=errors,requests,headers,frames[:control:data:window:all..],content,ssl,trace,channel"` 

One way to handle that with tools-deps is to add an alias with `:jvm-opts` option.

Here is a code snippet for `deps.edn`
```clojure
{
;; REDACTED
:aliases {
 :debug
 {:jvm-opts
  [;; enable logging for java.net.http
  "-Djdk.httpclient.HttpClient.log=errors,requests,headers,frames[:control:data:window:all..],content,ssl,trace,channel"]}
}}
```

## Test

``` clojure
$ bb test:clj
$ bb test:bb
```

## Credits

This library has borrowed liberally from [java-http-clj](https://github.com/schmee/java-http-clj) and [hato](https://github.com/gnarroway/hato), both available under the MIT license.

## License

Copyright © 2022 - 2023 Michiel Borkent

Distributed under the MIT License. See LICENSE.
