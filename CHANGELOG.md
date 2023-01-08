# Changelog

Babashka [http-client](https://github.com/babashka/http-client): HTTP client for Clojure and babashka built on java.net.http

## Unreleased

## 0.0.2

- Introduce `:request` option in `client` function for passing default request options via client.
- Expose `default-client-opts`, a map which can be used to get same behavior as (implicit) default client
- Accept `gzip` and `deflate` as encoding in default client
- Set accept header to `*/*` in default client

## 0.0.1

Initial version
