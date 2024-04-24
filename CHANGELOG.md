# Changelog

Babashka [http-client](https://github.com/babashka/http-client): HTTP client for Clojure and babashka built on java.net.http

## 0.4.19 (2024-04-24)

- [#55](https://github.com/babashka/http-client/issues/55): allow `:body` be `java.net.http.HttpRequest$BodyPublisher`

## 0.4.18 (2024-04-18)

- Support a Clojure function as `:client` option, mostly useful for testing

## 0.4.17 (2024-04-12)

- [#49](https://github.com/babashka/http-client/issues/49): add `::oauth-token` interceptor
- [#52](https://github.com/babashka/http-client/issues/52): document `:throw` option

## 0.4.16 (2024-02-10)

- [#45](https://github.com/babashka/http-client/issues/45): query param values are double encoded

## 0.4.15 (2023-09-04)

- [#43](https://github.com/babashka/http-client/issues/43): when using a string key for `Accept` header, the value is overridden by the default

## 0.4.14 (2023-08-17)

- [#41](https://github.com/babashka/http-client/issues/41): add `:uri` to response map

## 0.4.13 (2023-08-08)

- [#38](https://github.com/babashka/http-client/issues/38): Fix double wrapping of futures on exceptions during async requests ([@axvr](https://github.com/axvr))

## 0.4.12

- Add `babashka.http-client.websocket` API (mostly based on hato, thanks [@gnarroway](https://github.com/gnarroway)). See [API docs](https://github.com/babashka/http-client/blob/main/API.md#babashka.http-client.websocket).
- The `:ssl-context {:insecure true}` option was made more accepting, see babashka issue [#1587](https://github.com/babashka/babashka/issues/1587)
- [#32](https://github.com/babashka/http-client/issues/32): Documentation updates for missing parameters and functions ([@casselc](https://github.com/casselc))
- [#34](https://github.com/babashka/http-client/issues/34): add construction helpers for `:cookie-handler`, `:ssl-parameters`, and `:executor` ([@casselc](https://github.com/casselc))

## 0.3.11

- Fix [#28](https://github.com/babashka/http-client/issues/28): add `:authenticator` option

## 0.2.9

- Accept `java.net.URI` as uri directly in `request`, `get`, etc.
- [#22](https://github.com/babashka/http-client/issues/22): Support options for `:ssl-context`, similar to hato
- [#23](https://github.com/babashka/http-client/issues/23): ease construction of `ProxySelector` via `:proxy` key

## 0.1.8

- Fix binary file uploads

## 0.1.7

- Add `:async-then` and `:async-catch` callbacks that go together with `:async`
- Change `:follow-redirects` option from `:always` to the safer `:normal`

## 0.1.6

- Merge client `:request` options earlier to pick up on `:interceptors` settings

## 0.1.5

- Add `http/put` convenience function

## 0.1.4

- Implement `:multipart` uploads, largely based on [hato](https://github.com/gnarroway/hato)'s implementation
- [#13](https://github.com/babashka/http-client/issues/13): Add a default user-agent header: `babashka.http-client/<released-version>` ([@lispyclouds](https://github.com/lispyclouds))

## 0.0.3

- [#12](https://github.com/babashka/http-client/issues/12): Do not uncompress (empty) body of `:head` request

## 0.0.2

- Introduce `:request` option in `client` function for passing default request options via client.
- Expose `default-client-opts`, a map which can be used to get same behavior as (implicit) default client
- Accept `gzip` and `deflate` as encoding in default client
- Set accept header to `*/*` in default client

## 0.0.1

Initial version
