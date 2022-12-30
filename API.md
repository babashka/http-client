# Table of contents
-  [`babashka.http-client`](#babashka.http-client) 
    -  [`client`](#babashka.http-client/client) - Construct a custom client.
    -  [`delete`](#babashka.http-client/delete) - Convenience wrapper for <code>request</code> with method <code>:delete</code>.
    -  [`get`](#babashka.http-client/get) - Convenience wrapper for <code>request</code> with method <code>:get</code>.
    -  [`head`](#babashka.http-client/head) - Convenience wrapper for <code>request</code> with method <code>:head</code>.
    -  [`patch`](#babashka.http-client/patch) - Convenience wrapper for <code>request</code> with method <code>:patch</code>.
    -  [`post`](#babashka.http-client/post) - Convenience wrapper for <code>request</code> with method <code>:post</code>.
    -  [`request`](#babashka.http-client/request) - Perform request.
-  [`babashka.http-client.interceptors`](#babashka.http-client.interceptors) 
    -  [`accept-header`](#babashka.http-client.interceptors/accept-header) - Request: adds <code>:accept</code> header.
    -  [`basic-auth`](#babashka.http-client.interceptors/basic-auth) - Request: adds <code>:authorization</code> header based on <code>:basic-auth</code> (a map of <code>:user</code> and <code>:pass</code>) in request.
    -  [`construct-uri`](#babashka.http-client.interceptors/construct-uri) - Request.
    -  [`decode-body`](#babashka.http-client.interceptors/decode-body) - Response: based on the value of <code>:as</code> in request, decodes as <code>:string</code>, <code>:stream</code> or <code>:bytes</code>.
    -  [`decompress-body`](#babashka.http-client.interceptors/decompress-body) - Response: decompresses body based on "content-encoding" header.
    -  [`default-interceptors`](#babashka.http-client.interceptors/default-interceptors) - Default interceptor chain.
    -  [`form-params`](#babashka.http-client.interceptors/form-params) - Request: encodes <code>:form-params</code> map and adds <code>:body</code>.
    -  [`query-params`](#babashka.http-client.interceptors/query-params) - Request: encodes <code>:query-params</code> map and appends to <code>:uri</code>.

-----
# <a name="babashka.http-client">babashka.http-client</a>






## <a name="babashka.http-client/client">`client`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L20-L27)
<a name="babashka.http-client/client"></a>
``` clojure

(client opts)
```


Construct a custom client.

  Options:
  * `:follow-redirects`: `:never`, `:always` or `:normal`
  

## <a name="babashka.http-client/delete">`delete`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L36-L41)
<a name="babashka.http-client/delete"></a>
``` clojure

(delete uri)
(delete uri opts)
```


Convenience wrapper for [`request`](#babashka.http-client/request) with method `:delete`

## <a name="babashka.http-client/get">`get`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L29-L34)
<a name="babashka.http-client/get"></a>
``` clojure

(get uri)
(get uri opts)
```


Convenience wrapper for [`request`](#babashka.http-client/request) with method `:get`

## <a name="babashka.http-client/head">`head`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L43-L48)
<a name="babashka.http-client/head"></a>
``` clojure

(head uri)
(head uri opts)
```


Convenience wrapper for [`request`](#babashka.http-client/request) with method `:head`

## <a name="babashka.http-client/patch">`patch`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L57-L64)
<a name="babashka.http-client/patch"></a>
``` clojure

(patch url)
(patch url opts)
```


Convenience wrapper for [`request`](#babashka.http-client/request) with method `:patch`

## <a name="babashka.http-client/post">`post`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L50-L55)
<a name="babashka.http-client/post"></a>
``` clojure

(post uri)
(post uri opts)
```


Convenience wrapper for [`request`](#babashka.http-client/request) with method `:post`

## <a name="babashka.http-client/request">`request`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L5-L18)
<a name="babashka.http-client/request"></a>
``` clojure

(request opts)
```


Perform request. Returns map with at least `:body`, `:status`

  Options:

  * `:uri` - the uri to request (required).
     May be a string or map of `:schema` (required), `:host` (required), `:port`, `:path` and `:query`
  * `:method` - the request method: `:get`, `:post`, `:head`, `:delete` or `:patch`
  * `:interceptors` - custom interceptor chain
  * `:client` - a client as produced by [`client`](#babashka.http-client/client). If not provided a default client will be used.
  * `:async` - perform request asynchronously. The response will be a `CompletableFuture` of the response map.
  

-----
# <a name="babashka.http-client.interceptors">babashka.http-client.interceptors</a>






## <a name="babashka.http-client.interceptors/accept-header">`accept-header`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L65-L77)
<a name="babashka.http-client.interceptors/accept-header"></a>

Request: adds `:accept` header. Only supported value is `:json`.

## <a name="babashka.http-client.interceptors/basic-auth">`basic-auth`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L52-L63)
<a name="babashka.http-client.interceptors/basic-auth"></a>

Request: adds `:authorization` header based on `:basic-auth` (a map
  of `:user` and `:pass`) in request.

## <a name="babashka.http-client.interceptors/construct-uri">`construct-uri`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L166-L181)
<a name="babashka.http-client.interceptors/construct-uri"></a>

Request

## <a name="babashka.http-client.interceptors/decode-body">`decode-body`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L154-L164)
<a name="babashka.http-client.interceptors/decode-body"></a>

Response: based on the value of `:as` in request, decodes as `:string`, `:stream` or `:bytes`. Defaults to `:string`.

## <a name="babashka.http-client.interceptors/decompress-body">`decompress-body`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L141-L147)
<a name="babashka.http-client.interceptors/decompress-body"></a>

Response: decompresses body based on  "content-encoding" header. Valid values: `gzip` and `deflate`.

## <a name="babashka.http-client.interceptors/default-interceptors">`default-interceptors`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L183-L191)
<a name="babashka.http-client.interceptors/default-interceptors"></a>

Default interceptor chain. Interceptors are called in order for request and in reverse order for response.

## <a name="babashka.http-client.interceptors/form-params">`form-params`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L87-L97)
<a name="babashka.http-client.interceptors/form-params"></a>

Request: encodes `:form-params` map and adds `:body`.

## <a name="babashka.http-client.interceptors/query-params">`query-params`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L79-L85)
<a name="babashka.http-client.interceptors/query-params"></a>

Request: encodes `:query-params` map and appends to `:uri`.
