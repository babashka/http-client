# Table of contents
-  [`babashka.http-client`](#babashka.http-client) 
    -  [`->request-builder`](#babashka.http-client/->request-builder)
    -  [`basic-auth-value`](#babashka.http-client/basic-auth-value)
    -  [`client`](#babashka.http-client/client)
    -  [`client-builder`](#babashka.http-client/client-builder)
    -  [`decompress-body`](#babashka.http-client/decompress-body)
    -  [`default-client`](#babashka.http-client/default-client)
    -  [`default-request-interceptors`](#babashka.http-client/default-request-interceptors)
    -  [`default-response-interceptors`](#babashka.http-client/default-response-interceptors)
    -  [`delete`](#babashka.http-client/delete)
    -  [`get`](#babashka.http-client/get)
    -  [`gunzip`](#babashka.http-client/gunzip) - Returns a gunzip'd version of the given byte array or input stream.
    -  [`head`](#babashka.http-client/head)
    -  [`inflate`](#babashka.http-client/inflate) - Returns a zlib inflate'd version of the given byte array or InputStream.
    -  [`map->form-params`](#babashka.http-client/map->form-params)
    -  [`map->query-params`](#babashka.http-client/map->query-params)
    -  [`map->request`](#babashka.http-client/map->request)
    -  [`patch`](#babashka.http-client/patch)
    -  [`post`](#babashka.http-client/post)
    -  [`request`](#babashka.http-client/request)
    -  [`response->map`](#babashka.http-client/response->map)
    -  [`stream-bytes`](#babashka.http-client/stream-bytes)
    -  [`with-accept-header`](#babashka.http-client/with-accept-header)
    -  [`with-basic-auth`](#babashka.http-client/with-basic-auth)
    -  [`with-decoded-body`](#babashka.http-client/with-decoded-body)
    -  [`with-form-params`](#babashka.http-client/with-form-params)
    -  [`with-query-params`](#babashka.http-client/with-query-params)

-----
# <a name="babashka.http-client">babashka.http-client</a>






## <a name="babashka.http-client/->request-builder">`->request-builder`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L164-L178)
<a name="babashka.http-client/->request-builder"></a>
``` clojure

(->request-builder opts)
```


## <a name="babashka.http-client/basic-auth-value">`basic-auth-value`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L140-L143)
<a name="babashka.http-client/basic-auth-value"></a>
``` clojure

(basic-auth-value x)
```


## <a name="babashka.http-client/client">`client`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L58-L63)
<a name="babashka.http-client/client"></a>
``` clojure

(client)
(client opts)
```


## <a name="babashka.http-client/client-builder">`client-builder`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L34-L56)
<a name="babashka.http-client/client-builder"></a>
``` clojure

(client-builder)
(client-builder opts)
```


## <a name="babashka.http-client/decompress-body">`decompress-body`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L200-L203)
<a name="babashka.http-client/decompress-body"></a>

## <a name="babashka.http-client/default-client">`default-client`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L65-L66)
<a name="babashka.http-client/default-client"></a>

## <a name="babashka.http-client/default-request-interceptors">`default-request-interceptors`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L194-L198)
<a name="babashka.http-client/default-request-interceptors"></a>

## <a name="babashka.http-client/default-response-interceptors">`default-response-interceptors`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L292-L294)
<a name="babashka.http-client/default-response-interceptors"></a>

## <a name="babashka.http-client/delete">`delete`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L313-L317)
<a name="babashka.http-client/delete"></a>
``` clojure

(delete uri)
(delete uri opts)
```


## <a name="babashka.http-client/get">`get`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L307-L311)
<a name="babashka.http-client/get"></a>
``` clojure

(get uri)
(get uri opts)
```


## <a name="babashka.http-client/gunzip">`gunzip`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L205-L210)
<a name="babashka.http-client/gunzip"></a>
``` clojure

(gunzip b)
```


Returns a gunzip'd version of the given byte array or input stream.

## <a name="babashka.http-client/head">`head`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L319-L323)
<a name="babashka.http-client/head"></a>
``` clojure

(head uri)
(head uri opts)
```


## <a name="babashka.http-client/inflate">`inflate`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L216-L233)
<a name="babashka.http-client/inflate"></a>
``` clojure

(inflate b)
```


Returns a zlib inflate'd version of the given byte array or InputStream.

## <a name="babashka.http-client/map->form-params">`map->form-params`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L130-L138)
<a name="babashka.http-client/map->form-params"></a>
``` clojure

(map->form-params form-params-map)
```


## <a name="babashka.http-client/map->query-params">`map->query-params`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L122-L128)
<a name="babashka.http-client/map->query-params"></a>
``` clojure

(map->query-params query-params-map)
```


## <a name="babashka.http-client/map->request">`map->request`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L248-L249)
<a name="babashka.http-client/map->request"></a>
``` clojure

(map->request req-map)
```


## <a name="babashka.http-client/patch">`patch`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L331-L337)
<a name="babashka.http-client/patch"></a>
``` clojure

(patch url)
(patch url opts)
```


## <a name="babashka.http-client/post">`post`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L325-L329)
<a name="babashka.http-client/post"></a>
``` clojure

(post uri)
(post uri opts)
```


## <a name="babashka.http-client/request">`request`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L296-L305)
<a name="babashka.http-client/request"></a>
``` clojure

(request {:keys [client raw], :as req})
```


## <a name="babashka.http-client/response->map">`response->map`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L267-L275)
<a name="babashka.http-client/response->map"></a>
``` clojure

(response->map resp)
```


## <a name="babashka.http-client/stream-bytes">`stream-bytes`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L278-L281)
<a name="babashka.http-client/stream-bytes"></a>
``` clojure

(stream-bytes is)
```


## <a name="babashka.http-client/with-accept-header">`with-accept-header`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L154-L162)
<a name="babashka.http-client/with-accept-header"></a>
``` clojure

(with-accept-header opts)
```


## <a name="babashka.http-client/with-basic-auth">`with-basic-auth`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L145-L152)
<a name="babashka.http-client/with-basic-auth"></a>
``` clojure

(with-basic-auth opts)
```


## <a name="babashka.http-client/with-decoded-body">`with-decoded-body`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L283-L290)
<a name="babashka.http-client/with-decoded-body"></a>
``` clojure

(with-decoded-body resp)
```


## <a name="babashka.http-client/with-form-params">`with-form-params`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L185-L192)
<a name="babashka.http-client/with-form-params"></a>
``` clojure

(with-form-params opts)
```


## <a name="babashka.http-client/with-query-params">`with-query-params`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client.clj#L180-L183)
<a name="babashka.http-client/with-query-params"></a>
``` clojure

(with-query-params opts)
```

