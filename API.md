# Table of contents
-  [`babashka.http-client`](#babashka.http-client) 
    -  [`client`](#babashka.http-client/client) - Construct a custom client.
    -  [`default-client-opts`](#babashka.http-client/default-client-opts) - Options used to create the (implicit) default client.
    -  [`delete`](#babashka.http-client/delete) - Convenience wrapper for <code>request</code> with method <code>:delete</code>.
    -  [`get`](#babashka.http-client/get) - Convenience wrapper for <code>request</code> with method <code>:get</code>.
    -  [`head`](#babashka.http-client/head) - Convenience wrapper for <code>request</code> with method <code>:head</code>.
    -  [`patch`](#babashka.http-client/patch) - Convenience wrapper for <code>request</code> with method <code>:patch</code>.
    -  [`post`](#babashka.http-client/post) - Convenience wrapper for <code>request</code> with method <code>:post</code>.
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
-  [`babashka.http-client.websocket`](#babashka.http-client.websocket) 
    -  [`abort!`](#babashka.http-client.websocket/abort!) - Closes this WebSocket's input and output abruptly.
    -  [`close!`](#babashka.http-client.websocket/close!) - Initiates an orderly closure of this WebSocket's output by sending a Close message with the given status code and the reason.
    -  [`ping!`](#babashka.http-client.websocket/ping!) - Sends a Ping message with bytes from the given buffer.
    -  [`pong!`](#babashka.http-client.websocket/pong!) - Sends a Pong message with bytes from the given buffer.
    -  [`send!`](#babashka.http-client.websocket/send!) - Sends a message to the WebSocket.
    -  [`websocket`](#babashka.http-client.websocket/websocket) - Builds <code>java.net.http.Websocket</code> client.

-----
# <a name="babashka.http-client">babashka.http-client</a>






## <a name="babashka.http-client/client">`client`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L10-L25)
<a name="babashka.http-client/client"></a>
``` clojure

(client opts)
```


Construct a custom client. To get the same behavior as the (implicit) default client, pass [`default-client-opts`](#babashka.http-client/default-client-opts).

  Options:
  * `:follow-redirects` - `:never`, `:always` or `:normal`
  * `:connect-timeout` - connection timeout in milliseconds.
  * `:request` - default request options which will be used in requests made with this client.

  Returns map with:

  * `:client`, a `java.net.http.HttpClient`.

  The map can be passed to [`request`](#babashka.http-client/request) via the `:client` key.
  

## <a name="babashka.http-client/default-client-opts">`default-client-opts`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L6-L8)
<a name="babashka.http-client/default-client-opts"></a>

Options used to create the (implicit) default client.

## <a name="babashka.http-client/delete">`delete`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L52-L57)
<a name="babashka.http-client/delete"></a>
``` clojure

(delete uri)
(delete uri opts)
```


Convenience wrapper for [`request`](#babashka.http-client/request) with method `:delete`

## <a name="babashka.http-client/get">`get`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L45-L50)
<a name="babashka.http-client/get"></a>
``` clojure

(get uri)
(get uri opts)
```


Convenience wrapper for [`request`](#babashka.http-client/request) with method `:get`

## <a name="babashka.http-client/head">`head`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L59-L64)
<a name="babashka.http-client/head"></a>
``` clojure

(head uri)
(head uri opts)
```


Convenience wrapper for [`request`](#babashka.http-client/request) with method `:head`

## <a name="babashka.http-client/patch">`patch`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L73-L80)
<a name="babashka.http-client/patch"></a>
``` clojure

(patch url)
(patch url opts)
```


Convenience wrapper for [`request`](#babashka.http-client/request) with method `:patch`

## <a name="babashka.http-client/post">`post`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L66-L71)
<a name="babashka.http-client/post"></a>
``` clojure

(post uri)
(post uri opts)
```


Convenience wrapper for [`request`](#babashka.http-client/request) with method `:post`

## <a name="babashka.http-client/request">`request`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L27-L43)
<a name="babashka.http-client/request"></a>
``` clojure

(request opts)
```


Perform request. Returns map with at least `:body`, `:status`

  Options:

  * `:uri` - the uri to request (required).
     May be a string or map of `:schema` (required), `:host` (required), `:port`, `:path` and `:query`
  * `:headers` - a map of headers
  * `:method` - the request method: `:get`, `:post`, `:head`, `:delete` or `:patch`
  * `:interceptors` - custom interceptor chain
  * `:client` - a client as produced by [`client`](#babashka.http-client/client). If not provided a default client will be used.
  * `:async` - perform request asynchronously. The response will be a `CompletableFuture` of the response map.
  * `:timeout` - request timeout in milliseconds.
  * `:version` - the HTTP version: `:http1.1` or `:http2`.
  

-----
# <a name="babashka.http-client.interceptors">babashka.http-client.interceptors</a>






## <a name="babashka.http-client.interceptors/accept-header">`accept-header`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L73-L85)
<a name="babashka.http-client.interceptors/accept-header"></a>

Request: adds `:accept` header. Only supported value is `:json`.

## <a name="babashka.http-client.interceptors/basic-auth">`basic-auth`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L60-L71)
<a name="babashka.http-client.interceptors/basic-auth"></a>

Request: adds `:authorization` header based on `:basic-auth` (a map
  of `:user` and `:pass`) in request.

## <a name="babashka.http-client.interceptors/construct-uri">`construct-uri`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L175-L181)
<a name="babashka.http-client.interceptors/construct-uri"></a>

Request: construct uri from map

## <a name="babashka.http-client.interceptors/decode-body">`decode-body`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L163-L173)
<a name="babashka.http-client.interceptors/decode-body"></a>

Response: based on the value of `:as` in request, decodes as `:string`, `:stream` or `:bytes`. Defaults to `:string`.

## <a name="babashka.http-client.interceptors/decompress-body">`decompress-body`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L149-L156)
<a name="babashka.http-client.interceptors/decompress-body"></a>

Response: decompresses body based on  "content-encoding" header. Valid values: `gzip` and `deflate`.

## <a name="babashka.http-client.interceptors/default-interceptors">`default-interceptors`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L208-L218)
<a name="babashka.http-client.interceptors/default-interceptors"></a>

Default interceptor chain. Interceptors are called in order for request and in reverse order for response.

## <a name="babashka.http-client.interceptors/form-params">`form-params`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L95-L105)
<a name="babashka.http-client.interceptors/form-params"></a>

Request: encodes `:form-params` map and adds `:body`.

## <a name="babashka.http-client.interceptors/multipart">`multipart`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L196-L206)
<a name="babashka.http-client.interceptors/multipart"></a>

Adds appropriate body and header if making a multipart request.

## <a name="babashka.http-client.interceptors/query-params">`query-params`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L87-L93)
<a name="babashka.http-client.interceptors/query-params"></a>

Request: encodes `:query-params` map and appends to `:uri`.

## <a name="babashka.http-client.interceptors/throw-on-exceptional-status-code">`throw-on-exceptional-status-code`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L186-L194)
<a name="babashka.http-client.interceptors/throw-on-exceptional-status-code"></a>

Response: throw on exceptional status codes

## <a name="babashka.http-client.interceptors/unexceptional-statuses">`unexceptional-statuses`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L183-L184)
<a name="babashka.http-client.interceptors/unexceptional-statuses"></a>

-----
# <a name="babashka.http-client.websocket">babashka.http-client.websocket</a>






## <a name="babashka.http-client.websocket/abort!">`abort!`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/websocket.clj#L51-L54)
<a name="babashka.http-client.websocket/abort!"></a>
``` clojure

(abort! ws)
```


Closes this WebSocket's input and output abruptly.

## <a name="babashka.http-client.websocket/close!">`close!`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/websocket.clj#L43-L49)
<a name="babashka.http-client.websocket/close!"></a>
``` clojure

(close! ws)
(close! ws status-code reason)
```


Initiates an orderly closure of this WebSocket's output by sending a
  Close message with the given status code and the reason.

## <a name="babashka.http-client.websocket/ping!">`ping!`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/websocket.clj#L33-L36)
<a name="babashka.http-client.websocket/ping!"></a>
``` clojure

(ping! ws data)
```


Sends a Ping message with bytes from the given buffer.

## <a name="babashka.http-client.websocket/pong!">`pong!`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/websocket.clj#L38-L41)
<a name="babashka.http-client.websocket/pong!"></a>
``` clojure

(pong! ws data)
```


Sends a Pong message with bytes from the given buffer.

## <a name="babashka.http-client.websocket/send!">`send!`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/websocket.clj#L22-L31)
<a name="babashka.http-client.websocket/send!"></a>
``` clojure

(send! ws data)
(send! ws data opts)
```


Sends a message to the WebSocket.
  `data` can be a CharSequence (e.g. string), byte array or ByteBuffer

  Options:
  * `:last`: this is the last message, defaults to `true`

## <a name="babashka.http-client.websocket/websocket">`websocket`</a> [📃](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/websocket.clj#L8-L20)
<a name="babashka.http-client.websocket/websocket"></a>
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
