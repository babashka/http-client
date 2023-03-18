# Changelog

Babashka [http-client](https://github.com/babashka/http-client): HTTP client for Clojure and babashka built on java.net.http

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
