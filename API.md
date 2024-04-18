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
    -  [`oauth-token`](#babashka.http-client.interceptors/oauth-token) - Request: adds <code>:authorization</code> header based on <code>:oauth-token</code> (a string token) in request.
    -  [`query-params`](#babashka.http-client.interceptors/query-params) - Request: encodes <code>:query-params</code> map and appends to <code>:uri</code>.
    -  [`throw-on-exceptional-status-code`](#babashka.http-client.interceptors/throw-on-exceptional-status-code) - Response: throw on exceptional status codes.
    -  [`unexceptional-statuses`](#babashka.http-client.interceptors/unexceptional-statuses)
    -  [`uri-with-query`](#babashka.http-client.interceptors/uri-with-query) - We can't use the URI constructor because it encodes all arguments for us.
-  [`babashka.http-client.websocket`](#babashka.http-client.websocket)  - Code is very much based on hato's websocket code.
    -  [`abort!`](#babashka.http-client.websocket/abort!) - Closes this WebSocket's input and output abruptly.
    -  [`close!`](#babashka.http-client.websocket/close!) - Initiates an orderly closure of this WebSocket's output by sending a Close message with the given status code and the reason.
    -  [`ping!`](#babashka.http-client.websocket/ping!) - Sends a Ping message with bytes from the given buffer.
    -  [`pong!`](#babashka.http-client.websocket/pong!) - Sends a Pong message with bytes from the given buffer.
    -  [`send!`](#babashka.http-client.websocket/send!) - Sends a message to the WebSocket.
    -  [`websocket`](#babashka.http-client.websocket/websocket) - Builds <code>java.net.http.Websocket</code> client.

-----
# <a name="babashka.http-client">babashka.http-client</a>






## <a name="babashka.http-client/->Authenticator">`->Authenticator`</a><a name="babashka.http-client/->Authenticator"></a>
``` clojure

(->Authenticator opts)
```

Constructs a `java.net.Authenticator`.

  Options:

  * `:user` - the username
  * `:pass` - the password
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L36-L44">Source</a></sub></p>

## <a name="babashka.http-client/->CookieHandler">`->CookieHandler`</a><a name="babashka.http-client/->CookieHandler"></a>
``` clojure

(->CookieHandler opts)
```

Constructs a `java.net.CookieHandler` using `java.net.CookieManager`.

    Options:

    * `:store` - an optional `java.net.CookieStore` implementation
    * `:policy` - a `java.net.CookiePolicy` or one of `:accept-all`, `:accept-none`, `:original-server`
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L46-L54">Source</a></sub></p>

## <a name="babashka.http-client/->Executor">`->Executor`</a><a name="babashka.http-client/->Executor"></a>
``` clojure

(->Executor opts)
```

Constructs a `java.util.concurrent.Executor`.

   Options:

   * `:threads` - constructs a `ThreadPoolExecutor` with the specified number of threads
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L66-L73">Source</a></sub></p>

## <a name="babashka.http-client/->ProxySelector">`->ProxySelector`</a><a name="babashka.http-client/->ProxySelector"></a>
``` clojure

(->ProxySelector opts)
```

Constructs a `java.net.ProxySelector`.
  Options:
  * `:host` - string
  * `:port` - long
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L9-L15">Source</a></sub></p>

## <a name="babashka.http-client/->SSLContext">`->SSLContext`</a><a name="babashka.http-client/->SSLContext"></a>
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
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L17-L34">Source</a></sub></p>

## <a name="babashka.http-client/->SSLParameters">`->SSLParameters`</a><a name="babashka.http-client/->SSLParameters"></a>
``` clojure

(->SSLParameters opts)
```

Constructs a `javax.net.ssl.SSLParameters`.

   Options:

   * `:ciphers` - a list of cipher suite names
   * `:protocols` - a list of protocol names
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L56-L64">Source</a></sub></p>

## <a name="babashka.http-client/client">`client`</a><a name="babashka.http-client/client"></a>
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
  
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L75-L98">Source</a></sub></p>

## <a name="babashka.http-client/default-client-opts">`default-client-opts`</a><a name="babashka.http-client/default-client-opts"></a>




Options used to create the (implicit) default client.
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L5-L7">Source</a></sub></p>

## <a name="babashka.http-client/delete">`delete`</a><a name="babashka.http-client/delete"></a>
``` clojure

(delete uri)
(delete uri opts)
```

Convenience wrapper for [`request`](#babashka.http-client/request) with method `:delete`
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L134-L139">Source</a></sub></p>

## <a name="babashka.http-client/get">`get`</a><a name="babashka.http-client/get"></a>
``` clojure

(get uri)
(get uri opts)
```

Convenience wrapper for [`request`](#babashka.http-client/request) with method `:get`
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L127-L132">Source</a></sub></p>

## <a name="babashka.http-client/head">`head`</a><a name="babashka.http-client/head"></a>
``` clojure

(head uri)
(head uri opts)
```

Convenience wrapper for [`request`](#babashka.http-client/request) with method `:head`
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L141-L146">Source</a></sub></p>

## <a name="babashka.http-client/patch">`patch`</a><a name="babashka.http-client/patch"></a>
``` clojure

(patch url)
(patch url opts)
```

Convenience wrapper for [`request`](#babashka.http-client/request) with method `:patch`
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L155-L162">Source</a></sub></p>

## <a name="babashka.http-client/post">`post`</a><a name="babashka.http-client/post"></a>
``` clojure

(post uri)
(post uri opts)
```

Convenience wrapper for [`request`](#babashka.http-client/request) with method `:post`
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L148-L153">Source</a></sub></p>

## <a name="babashka.http-client/put">`put`</a><a name="babashka.http-client/put"></a>
``` clojure

(put url)
(put url opts)
```

Convenience wrapper for [`request`](#babashka.http-client/request) with method `:put`
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L164-L171">Source</a></sub></p>

## <a name="babashka.http-client/request">`request`</a><a name="babashka.http-client/request"></a>
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
  * `:client` - a client as produced by [`client`](#babashka.http-client/client) or a clojure function. If not provided a default client will be used.
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
  * `:version` - the HTTP version: `:http1.1` or `:http2`.
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L100-L125">Source</a></sub></p>

-----
# <a name="babashka.http-client.interceptors">babashka.http-client.interceptors</a>






## <a name="babashka.http-client.interceptors/accept-header">`accept-header`</a><a name="babashka.http-client.interceptors/accept-header"></a>




Request: adds `:accept` header. Only supported value is `:json`.
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L72-L84">Source</a></sub></p>

## <a name="babashka.http-client.interceptors/basic-auth">`basic-auth`</a><a name="babashka.http-client.interceptors/basic-auth"></a>




Request: adds `:authorization` header based on `:basic-auth` (a map
  of `:user` and `:pass`) in request.
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L46-L57">Source</a></sub></p>

## <a name="babashka.http-client.interceptors/construct-uri">`construct-uri`</a><a name="babashka.http-client.interceptors/construct-uri"></a>




Request: construct uri from map
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L222-L228">Source</a></sub></p>

## <a name="babashka.http-client.interceptors/decode-body">`decode-body`</a><a name="babashka.http-client.interceptors/decode-body"></a>




Response: based on the value of `:as` in request, decodes as `:string`, `:stream` or `:bytes`. Defaults to `:string`.
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L208-L220">Source</a></sub></p>

## <a name="babashka.http-client.interceptors/decompress-body">`decompress-body`</a><a name="babashka.http-client.interceptors/decompress-body"></a>




Response: decompresses body based on  "content-encoding" header. Valid values: `gzip` and `deflate`.
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L194-L201">Source</a></sub></p>

## <a name="babashka.http-client.interceptors/default-interceptors">`default-interceptors`</a><a name="babashka.http-client.interceptors/default-interceptors"></a>




Default interceptor chain. Interceptors are called in order for request and in reverse order for response.
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L256-L267">Source</a></sub></p>

## <a name="babashka.http-client.interceptors/form-params">`form-params`</a><a name="babashka.http-client.interceptors/form-params"></a>




Request: encodes `:form-params` map and adds `:body`.
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L140-L150">Source</a></sub></p>

## <a name="babashka.http-client.interceptors/multipart">`multipart`</a><a name="babashka.http-client.interceptors/multipart"></a>




Adds appropriate body and header if making a multipart request.
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L244-L254">Source</a></sub></p>

## <a name="babashka.http-client.interceptors/oauth-token">`oauth-token`</a><a name="babashka.http-client.interceptors/oauth-token"></a>




Request: adds `:authorization` header based on `:oauth-token` (a string token)
   in request.
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L59-L70">Source</a></sub></p>

## <a name="babashka.http-client.interceptors/query-params">`query-params`</a><a name="babashka.http-client.interceptors/query-params"></a>




Request: encodes `:query-params` map and appends to `:uri`.
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L118-L127">Source</a></sub></p>

## <a name="babashka.http-client.interceptors/throw-on-exceptional-status-code">`throw-on-exceptional-status-code`</a><a name="babashka.http-client.interceptors/throw-on-exceptional-status-code"></a>




Response: throw on exceptional status codes
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L233-L242">Source</a></sub></p>

## <a name="babashka.http-client.interceptors/unexceptional-statuses">`unexceptional-statuses`</a><a name="babashka.http-client.interceptors/unexceptional-statuses"></a>



<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L230-L231">Source</a></sub></p>

## <a name="babashka.http-client.interceptors/uri-with-query">`uri-with-query`</a><a name="babashka.http-client.interceptors/uri-with-query"></a>
``` clojure

(uri-with-query uri new-query)
```

We can't use the URI constructor because it encodes all arguments for us.
  See https://stackoverflow.com/a/77971448/6264
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L102-L116">Source</a></sub></p>

-----
# <a name="babashka.http-client.websocket">babashka.http-client.websocket</a>


Code is very much based on hato's websocket code. Credits to @gnarroway!




## <a name="babashka.http-client.websocket/abort!">`abort!`</a><a name="babashka.http-client.websocket/abort!"></a>
``` clojure

(abort! ws)
```

Closes this WebSocket's input and output abruptly.
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/websocket.clj#L61-L64">Source</a></sub></p>

## <a name="babashka.http-client.websocket/close!">`close!`</a><a name="babashka.http-client.websocket/close!"></a>
``` clojure

(close! ws)
(close! ws status-code reason)
```

Initiates an orderly closure of this WebSocket's output by sending a
  Close message with the given status code and the reason.
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/websocket.clj#L53-L59">Source</a></sub></p>

## <a name="babashka.http-client.websocket/ping!">`ping!`</a><a name="babashka.http-client.websocket/ping!"></a>
``` clojure

(ping! ws data)
```

Sends a Ping message with bytes from the given buffer.
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/websocket.clj#L43-L46">Source</a></sub></p>

## <a name="babashka.http-client.websocket/pong!">`pong!`</a><a name="babashka.http-client.websocket/pong!"></a>
``` clojure

(pong! ws data)
```

Sends a Pong message with bytes from the given buffer.
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/websocket.clj#L48-L51">Source</a></sub></p>

## <a name="babashka.http-client.websocket/send!">`send!`</a><a name="babashka.http-client.websocket/send!"></a>
``` clojure

(send! ws data)
(send! ws data opts)
```

Sends a message to the WebSocket.
  `data` can be a CharSequence (e.g. string), byte array or ByteBuffer

  Options:
  * `:last`: this is the last message, defaults to `true`
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/websocket.clj#L32-L41">Source</a></sub></p>

## <a name="babashka.http-client.websocket/websocket">`websocket`</a><a name="babashka.http-client.websocket/websocket"></a>
``` clojure

(websocket {:keys [client], :as opts})
```

Builds `java.net.http.Websocket` client.
  * `:uri` - the uri to request (required).
     May be a string or map of `:schema` (required), `:host` (required), `:port`, `:path` and `:query`
  * `:headers` - a map of headers for the initial handshake`
  * `:client` - a client as produced by `client`. If not provided a default client will be used.
  * `:connect-timeout` Sets a timeout for establishing a WebSocket connection (in millis).
  * `:subprotocols` - sets a request for the given subprotocols.
  * `:async` - return `CompleteableFuture` of websocket

  Callbacks options:
  * `:on-open` - `[ws]`, called when a `WebSocket` has been connected.
  * `:on-message` - `[ws data last]` A textual/binary data has been received.
  * `:on-ping` - `[ws data]` A Ping message has been received.
  * `:on-pong` - `[ws data]` A Pong message has been received.
  * `:on-close` - `[ws status reason]` Receives a Close message indicating the WebSocket's input has been closed.
  * `:on-error` - `[ws err]` An error has occurred.
<p><sub><a href="https://github.com/babashka/http-client/blob/main/src/babashka/http_client/websocket.clj#L10-L30">Source</a></sub></p>
