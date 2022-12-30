# Table of contents
-  [`babashka.http-client`](#babashka.http-client) 
    -  [`client`](#babashka.http-client/client)
    -  [`delete`](#babashka.http-client/delete)
    -  [`get`](#babashka.http-client/get)
    -  [`head`](#babashka.http-client/head)
    -  [`patch`](#babashka.http-client/patch)
    -  [`post`](#babashka.http-client/post)
    -  [`request`](#babashka.http-client/request)
-  [`babashka.http-client.interceptors`](#babashka.http-client.interceptors) 
    -  [`accept-header`](#babashka.http-client.interceptors/accept-header) - Request: adds <code>:accept</code> header.
    -  [`basic-auth`](#babashka.http-client.interceptors/basic-auth)
    -  [`decode-body`](#babashka.http-client.interceptors/decode-body) - Response: based on the value of <code>:as</code> in request, decodes as <code>:string</code>, <code>:stream</code> or <code>:bytes</code>.
    -  [`decompress-body`](#babashka.http-client.interceptors/decompress-body) - Response: decompresses body based on "content-encoding" header.
    -  [`default-interceptors`](#babashka.http-client.interceptors/default-interceptors)
    -  [`form-params`](#babashka.http-client.interceptors/form-params) - Request: encodes <code>:form-params</code> map and adds <code>:body</code>.
    -  [`query-params`](#babashka.http-client.interceptors/query-params) - Request: encodes <code>:query-params</code> map and appends to <code>:uri</code>.

-----
# <a name="babashka.http-client">babashka.http-client</a>






## <a name="babashka.http-client/client">`client`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L8-L9)
<a name="babashka.http-client/client"></a>
``` clojure

(client opts)
```


## <a name="babashka.http-client/delete">`delete`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L17-L21)
<a name="babashka.http-client/delete"></a>
``` clojure

(delete uri)
(delete uri opts)
```


## <a name="babashka.http-client/get">`get`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L11-L15)
<a name="babashka.http-client/get"></a>
``` clojure

(get uri)
(get uri opts)
```


## <a name="babashka.http-client/head">`head`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L23-L27)
<a name="babashka.http-client/head"></a>
``` clojure

(head uri)
(head uri opts)
```


## <a name="babashka.http-client/patch">`patch`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L35-L41)
<a name="babashka.http-client/patch"></a>
``` clojure

(patch url)
(patch url opts)
```


## <a name="babashka.http-client/post">`post`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L29-L33)
<a name="babashka.http-client/post"></a>
``` clojure

(post uri)
(post uri opts)
```


## <a name="babashka.http-client/request">`request`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L5-L6)
<a name="babashka.http-client/request"></a>
``` clojure

(request opts)
```


-----
# <a name="babashka.http-client.interceptors">babashka.http-client.interceptors</a>






## <a name="babashka.http-client.interceptors/accept-header">`accept-header`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L63-L75)
<a name="babashka.http-client.interceptors/accept-header"></a>

Request: adds `:accept` header. Only supported value is `:json`.

## <a name="babashka.http-client.interceptors/basic-auth">`basic-auth`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L52-L61)
<a name="babashka.http-client.interceptors/basic-auth"></a>

## <a name="babashka.http-client.interceptors/decode-body">`decode-body`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L152-L162)
<a name="babashka.http-client.interceptors/decode-body"></a>

Response: based on the value of `:as` in request, decodes as `:string`, `:stream` or `:bytes`. Defaults to `:string`.

## <a name="babashka.http-client.interceptors/decompress-body">`decompress-body`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L139-L145)
<a name="babashka.http-client.interceptors/decompress-body"></a>

Response: decompresses body based on  "content-encoding" header. Valid values: `gzip` and `deflate`.

## <a name="babashka.http-client.interceptors/default-interceptors">`default-interceptors`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L164-L170)
<a name="babashka.http-client.interceptors/default-interceptors"></a>

## <a name="babashka.http-client.interceptors/form-params">`form-params`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L85-L95)
<a name="babashka.http-client.interceptors/form-params"></a>

Request: encodes `:form-params` map and adds `:body`.

## <a name="babashka.http-client.interceptors/query-params">`query-params`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L77-L83)
<a name="babashka.http-client.interceptors/query-params"></a>

Request: encodes `:query-params` map and appends to `:uri`.
