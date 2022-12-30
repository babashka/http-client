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
    -  [`accept-header-interceptor`](#babashka.http-client.interceptors/accept-header-interceptor)
    -  [`basic-auth-interceptor`](#babashka.http-client.interceptors/basic-auth-interceptor)
    -  [`decode-body-interceptor`](#babashka.http-client.interceptors/decode-body-interceptor)
    -  [`decompress-body-interceptor`](#babashka.http-client.interceptors/decompress-body-interceptor)
    -  [`default-interceptors`](#babashka.http-client.interceptors/default-interceptors)
    -  [`form-params-interceptor`](#babashka.http-client.interceptors/form-params-interceptor)
    -  [`insert-interceptors-after`](#babashka.http-client.interceptors/insert-interceptors-after)
    -  [`insert-interceptors-before`](#babashka.http-client.interceptors/insert-interceptors-before)
    -  [`query-params-interceptor`](#babashka.http-client.interceptors/query-params-interceptor)

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






## <a name="babashka.http-client.interceptors/accept-header-interceptor">`accept-header-interceptor`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L63-L74)
<a name="babashka.http-client.interceptors/accept-header-interceptor"></a>

## <a name="babashka.http-client.interceptors/basic-auth-interceptor">`basic-auth-interceptor`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L52-L61)
<a name="babashka.http-client.interceptors/basic-auth-interceptor"></a>

## <a name="babashka.http-client.interceptors/decode-body-interceptor">`decode-body-interceptor`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L148-L158)
<a name="babashka.http-client.interceptors/decode-body-interceptor"></a>

## <a name="babashka.http-client.interceptors/decompress-body-interceptor">`decompress-body-interceptor`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L136-L141)
<a name="babashka.http-client.interceptors/decompress-body-interceptor"></a>

## <a name="babashka.http-client.interceptors/default-interceptors">`default-interceptors`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L160-L166)
<a name="babashka.http-client.interceptors/default-interceptors"></a>

## <a name="babashka.http-client.interceptors/form-params-interceptor">`form-params-interceptor`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L83-L92)
<a name="babashka.http-client.interceptors/form-params-interceptor"></a>

## <a name="babashka.http-client.interceptors/insert-interceptors-after">`insert-interceptors-after`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L172-L174)
<a name="babashka.http-client.interceptors/insert-interceptors-after"></a>
``` clojure

(insert-interceptors-after chain after & interceptors)
```


## <a name="babashka.http-client.interceptors/insert-interceptors-before">`insert-interceptors-before`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L168-L170)
<a name="babashka.http-client.interceptors/insert-interceptors-before"></a>
``` clojure

(insert-interceptors-before chain before & interceptors)
```


## <a name="babashka.http-client.interceptors/query-params-interceptor">`query-params-interceptor`</a> [:page_facing_up:](https://github.com/babashka/http-client/blob/main/src/babashka/http_client/interceptors.clj#L76-L81)
<a name="babashka.http-client.interceptors/query-params-interceptor"></a>
