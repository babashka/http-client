(ns babashka.http-client.internal.helpers-test
  (:require
   [babashka.http-client.internal.helpers :as h]
   [clojure.test :as t]))

(t/deftest ->uri-tests
  (let [^java.net.URI uri (h/->uri {:scheme "https" :host "example.com" :path "/foo"})]
    (t/is (= (.getPort uri) -1))))
