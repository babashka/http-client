# Table of contents
-  [`babashka.http-client`](#babashka.http-client) 
    -  [`->Authenticator`](#babashka.http-client/->Authenticator) - Constructs a <code>java.net.Authenticator</code>.
    -  [`->CookieHandler`](#babashka.http-client/->CookieHandler) - Constructs a <code>java.net.CookieHandler</code> using <code>java.net.CookieManager</code>.
    -  [`->Executor`](#babashka.http-client/->Executor) - Constructs a <code>java.util.concurrent.Executor</code>.
    -  [`->ProxySelector`](#babashka.http-client/->ProxySelector) - Constructs a <code>java.net.ProxySelector</code>.
    -  [`->SSLContext`](#babashka.http-client/->SSLContext) - Constructs a <code>javax.net.ssl.SSLContext</code>.
    -  [`->SSLParameters`](#babashka.http-client/->SSLParameters) - Constructs a <code>javax.net.ssl.SSLParameters</code>.
    -  [`client`](#babashka.http-client/client) - Construct a custom client.
    -  [`default-client-opts`](#babashka.http-client/default-client-opts) - Options used to create the (implicit) default client.
    -  [`delete`](#babashka.http-client/delete) - Convenience wrapper for <code>request</code> with method <code>:delete</code>.
    -  [`get`](#babashka.http-client/get) - Convenience wrapper for <code>request</code> with method <code>:get</code>.
    -  [`head`](#babashka.http-client/head) - Convenience wrapper for <code>request</code> with method <code>:head</code>.
    -  [`patch`](#babashka.http-client/patch) - Convenience wrapper for <code>request</code> with method <code>:patch</code>.
    -  [`post`](#babashka.http-client/post) - Convenience wrapper for <code>request</code> with method <code>:post</code>.
    -  [`put`](#babashka.http-client/put) - Convenience wrapper for <code>request</code> with method <code>:put</code>.
    -  [`request`](#babashka.http-client/request) - Perform request.
-  [`babashka.http-client.interceptors`](#babashka.http-client.interceptors) 
    -  [`accept-header`](#babashka.http-client.interceptors/accept-header) - Request: adds <code>:accept</code> header.
    -  [`basic-auth`](#babashka.http-client.interceptors/basic-auth) - Request: adds <code>:authorization</code> header based on <code>:basic-auth</code> (a map of <code>:user</code> and <code>:pass</code>) in request.
    -  [`construct-uri`](#babashka.http-client.interceptors/construct-uri) - Request: construct uri from map.
    -  [`decode-body`](#babashka.http-client.interceptors/decode-body) - Response: based on the value of <code>:as</code> in request, decodes as <code>:string</code>, <code>:stream</code> or <code>:bytes</code>.
    -  [`decompress-body`](#babashka.http-client.interceptors/decompress-body) - Response: decompresses body based on "content-encoding" header.
    -  [`default-interceptors`](#babashka.http-client.interceptors/default-interceptors) - Default interceptor chain.
    -  [`form-params`](#babashka.http-client.interceptors/form-params) - Request: encodes <code>:form-params</code> map and adds <code>:body</code>.
    -  [`multipart`](#babashka.http-client.interceptors/multipart) - Adds appropriate body and header if making a multipart request.
    -  [`query-params`](#babashka.http-client.interceptors/query-params) - Request: encodes <code>:query-params</code> map and appends to <code>:uri</code>.
    -  [`throw-on-exceptional-status-code`](#babashka.http-client.interceptors/throw-on-exceptional-status-code) - Response: throw on exceptional status codes.
    -  [`unexceptional-statuses`](#babashka.http-client.interceptors/unexceptional-statuses)

-----
# <a name="babashka.http-client">babashka.http-client</a>






## <a name="babashka.http-client/->Authenticator">`->Authenticator`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L36-L44)
<a name="babashka.http-client/->Authenticator"></a>
``` clojure

(->Authenticator opts)
```


Constructs a `java.net.Authenticator`.

  Options:

  * `:user` - the username
  * `:pass` - the password

## <a name="babashka.http-client/->CookieHandler">`->CookieHandler`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L46-L54)
<a name="babashka.http-client/->CookieHandler"></a>
``` clojure

(->CookieHandler opts)
```


Constructs a `java.net.CookieHandler` using `java.net.CookieManager`.
  
    Options:
  
    * `:store` - an optional `java.net.CookieStore` implementation
    * `:policy` - a `java.net.CookiePolicy` or one of `:accept-all`, `:accept-none`, `:original-server`

## <a name="babashka.http-client/->Executor">`->Executor`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L66-L73)
<a name="babashka.http-client/->Executor"></a>
``` clojure

(->Executor opts)
```


Constructs a `java.util.concurrent.Executor`.
   
   Options:
   
   * `:threads` - constructs a `ThreadPoolExecutor` with the specified number of threads

## <a name="babashka.http-client/->ProxySelector">`->ProxySelector`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L9-L15)
<a name="babashka.http-client/->ProxySelector"></a>
``` clojure

(->ProxySelector opts)
```


Constructs a `java.net.ProxySelector`.
  Options:
  * `:host` - string
  * `:port` - long

## <a name="babashka.http-client/->SSLContext">`->SSLContext`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L17-L34)
<a name="babashka.http-client/->SSLContext"></a>
``` clojure

(->SSLContext opts)
```


Constructs a `javax.net.ssl.SSLContext`.

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
  properties globally.

## <a name="babashka.http-client/->SSLParameters">`->SSLParameters`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L56-L64)
<a name="babashka.http-client/->SSLParameters"></a>
``` clojure

(->SSLParameters opts)
```


Constructs a `javax.net.ssl.SSLParameters`.
   
   Options:
   
   * `:ciphers` - a list of cipher suite names
   * `:protocols` - a list of protocol names

## <a name="babashka.http-client/client">`client`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L75-L98)
<a name="babashka.http-client/client"></a>
``` clojure

(client opts)
```


Construct a custom client. To get the same behavior as the (implicit) default client, pass [`default-client-opts`](#babashka.http-client/default-client-opts).

  Options:
  * `:follow-redirects` - `:never`, `:always` or `:normal`
  * `:connect-timeout` - connection timeout in milliseconds.
  * `:request` - default request options which will be used in requests made with this client.
  * `:executor` - a `java.util.concurrent.Executor` or a map of options, see docstring of [`->Executor`](#babashka.http-client/->Executor)
  * `:ssl-context` - a `javax.net.ssl.SSLContext` or a map of options, see docstring of [`->SSLContext`](#babashka.http-client/->SSLContext).
  * `:ssl-parameters` - a `javax.net.ssl.SSLParameters' or a map of options, see docstring of `->SSLParameters`.
  * `:proxy` - a `java.net.ProxySelector` or a map of options, see docstring of [`->ProxySelector`](#babashka.http-client/->ProxySelector).
  * `:authenticator` - a `java.net.Authenticator` or a map of options, see docstring of [`->Authenticator`](#babashka.http-client/->Authenticator).
  * `:cookie-handler` - a `java.net.CookieHandler` or a map of options, see docstring of [`->CookieHandler`](#babashka.http-client/->CookieHandler).
  * `:version` - the HTTP version: `:http1.1` or `:http2`.
  * `:priority` - priority for HTTP2 requests, integer between 1-256 inclusive.

  Returns map with:

  * `:client` - a `java.net.http.HttpClient`.

  The map can be passed to [`request`](#babashka.http-client/request) via the `:client` key.
  

## <a name="babashka.http-client/default-client-opts">`default-client-opts`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L5-L7)
<a name="babashka.http-client/default-client-opts"></a>

Options used to create the (implicit) default client.

## <a name="babashka.http-client/delete">`delete`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L131-L136)
<a name="babashka.http-client/delete"></a>
``` clojure

(delete uri)
(delete uri opts)
```


Convenience wrapper for [`request`](#babashka.http-client/request) with method `:delete`

## <a name="babashka.http-client/get">`get`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L124-L129)
<a name="babashka.http-client/get"></a>
``` clojure

(get uri)
(get uri opts)
```


Convenience wrapper for [`request`](#babashka.http-client/request) with method `:get`

## <a name="babashka.http-client/head">`head`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L138-L143)
<a name="babashka.http-client/head"></a>
``` clojure

(head uri)
(head uri opts)
```


Convenience wrapper for [`request`](#babashka.http-client/request) with method `:head`

## <a name="babashka.http-client/patch">`patch`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L152-L159)
<a name="babashka.http-client/patch"></a>
``` clojure

(patch url)
(patch url opts)
```


Convenience wrapper for [`request`](#babashka.http-client/request) with method `:patch`

## <a name="babashka.http-client/post">`post`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L145-L150)
<a name="babashka.http-client/post"></a>
``` clojure

(post uri)
(post uri opts)
```


Convenience wrapper for [`request`](#babashka.http-client/request) with method `:post`

## <a name="babashka.http-client/put">`put`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L161-L168)
<a name="babashka.http-client/put"></a>
``` clojure

(put url)
(put url opts)
```


Convenience wrapper for [`request`](#babashka.http-client/request) with method `:put`

## <a name="babashka.http-client/request">`request`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L100-L122)
<a name="babashka.http-client/request"></a>
``` clojure

(request opts)
```


Perform request. Returns map with at least `:body`, `:status`

  Options:

  * `:uri` - the uri to request (required).
     May be a string or map of `:scheme` (required), `:host` (required), `:port`, `:path` and `:query`
  * `:headers` - a map of headers
  * `:method` - the request method: `:get`, `:post`, `:head`, `:delete`, `:patch` or `:put`
  * `:interceptors` - custom interceptor chain
  * `:client` - a client as produced by [`client`](#babashka.http-client/client). If not provided a default client will be used.
  * `:query-params` - a map of query params. The values can be a list to send multiple params with the same key.
  * `:form-params` - a map of form params to send in the request body.
  * `:body` - a file, inputstream or string to send as the request body.
  * `:basic-auth` - a sequence of `user` `password` or map with `:user` `:pass` used for basic auth.
  * `:async` - perform request asynchronously. The response will be a `CompletableFuture` of the response map.
  * `:async-then` - a function that is called on the async result if successful
  * `:async-catch` - a function that is called on the async result if exceptional
  * `:timeout` - request timeout in milliseconds.
  * `:version` - the HTTP version: `:http1.1` or `:http2`.
  

-----
# <a name="babashka.http-client.interceptors">babashka.http-client.interceptors</a>






## <a name="babashka.http-client.interceptors/accept-header">`accept-header`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L58-L70)
<a name="babashka.http-client.interceptors/accept-header"></a>

Request: adds `:accept` header. Only supported value is `:json`.

## <a name="babashka.http-client.interceptors/basic-auth">`basic-auth`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L45-L56)
<a name="babashka.http-client.interceptors/basic-auth"></a>

Request: adds `:authorization` header based on `:basic-auth` (a map
  of `:user` and `:pass`) in request.

## <a name="babashka.http-client.interceptors/construct-uri">`construct-uri`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L181-L196)
<a name="babashka.http-client.interceptors/construct-uri"></a>

Request: construct uri from map

## <a name="babashka.http-client.interceptors/decode-body">`decode-body`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L169-L179)
<a name="babashka.http-client.interceptors/decode-body"></a>

Response: based on the value of `:as` in request, decodes as `:string`, `:stream` or `:bytes`. Defaults to `:string`.

## <a name="babashka.http-client.interceptors/decompress-body">`decompress-body`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L155-L162)
<a name="babashka.http-client.interceptors/decompress-body"></a>

Response: decompresses body based on  "content-encoding" header. Valid values: `gzip` and `deflate`.

## <a name="babashka.http-client.interceptors/default-interceptors">`default-interceptors`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L223-L233)
<a name="babashka.http-client.interceptors/default-interceptors"></a>

Default interceptor chain. Interceptors are called in order for request and in reverse order for response.

## <a name="babashka.http-client.interceptors/form-params">`form-params`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L101-L111)
<a name="babashka.http-client.interceptors/form-params"></a>

Request: encodes `:form-params` map and adds `:body`.

## <a name="babashka.http-client.interceptors/multipart">`multipart`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L211-L221)
<a name="babashka.http-client.interceptors/multipart"></a>

Adds appropriate body and header if making a multipart request.

## <a name="babashka.http-client.interceptors/query-params">`query-params`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L86-L99)
<a name="babashka.http-client.interceptors/query-params"></a>

Request: encodes `:query-params` map and appends to `:uri`.

## <a name="babashka.http-client.interceptors/throw-on-exceptional-status-code">`throw-on-exceptional-status-code`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L201-L209)
<a name="babashka.http-client.interceptors/throw-on-exceptional-status-code"></a>

Response: throw on exceptional status codes

## <a name="babashka.http-client.interceptors/unexceptional-statuses">`unexceptional-statuses`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L198-L199)
<a name="babashka.http-client.interceptors/unexceptional-statuses"></a>
