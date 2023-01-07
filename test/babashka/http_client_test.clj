(ns babashka.http-client-test
  (:require [babashka.http-client :as http]
            [babashka.http-client.interceptors :as interceptors]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]])
  (:import (clojure.lang ExceptionInfo)))

(defmethod clojure.test/report :begin-test-var [m]
  (println "===" (-> m :var meta :name))
  (println))

(deftest get-test
  (is (str/includes? (:body (http/get "https://httpstat.us/200"))
                     "200"))
  (is (= 200
         (-> (http/get "https://httpstat.us/200"
                         {:headers {"Accept" "application/json"}})
             :body
             (json/parse-string true)
             :code)))
  (testing "query params"
    (is (= {:foo1 "bar1" :foo2 "bar2" :foo3 "bar3" :not-string "42" :namespaced/key "foo"}
           (-> (http/get "https://postman-echo.com/get" {:query-params {"foo1" "bar1" "foo2" "bar2" :foo3 "bar3" :not-string 42 :namespaced/key "foo"}})
               :body
               (json/parse-string true)
               :args)))
    (is (= {:foo1 ["bar1" "bar2"]}
           (-> (http/get "https://postman-echo.com/get" {:query-params {"foo1" ["bar1" "bar2"]}})
               :body
               (json/parse-string true)
               :args)))))

(deftest delete-test
  (is (= 200 (:status (http/delete "https://postman-echo.com/delete")))))

(deftest head-test
  (is (= 200 (:status (http/head "https://postman-echo.com/head")))))

(deftest post-test
  (is (subs (:body (http/post "https://postman-echo.com/post"))
            0 10))
  (is (str/includes?
       (:body (http/post "https://postman-echo.com/post"
                           {:body "From Clojure"}))
       "From Clojure"))
  (testing "file body"
    (is (str/includes?
         (:body (http/post "https://postman-echo.com/post"
                             {:body (io/file "README.md")}))
         "babashka")))
  (testing "JSON body"
    (let [response (http/post "https://postman-echo.com/post"
                                {:headers {"Content-Type" "application/json"}
                                 :body (json/generate-string {:a "foo"})})
          body (:body response)
          body (json/parse-string body true)
          json (:json body)]
      (is (= {:a "foo"} json))))
  (testing "stream body"
    (is (str/includes?
         (:body (http/post "https://postman-echo.com/post"
                             {:body (io/input-stream "README.md")}))
         "babashka")))
  (testing "form-params"
    (let [body (:body (http/post "https://postman-echo.com/post"
                                   {:form-params {"name" "Michiel Borkent"
                                                  :location "NL"
                                                  :this-isnt-a-string 42}}))
          body (json/parse-string body true)
          headers (:headers body)
          content-type (:content-type headers)]
      (is (= "application/x-www-form-urlencoded" content-type))))
  ;; TODO:
  #_(testing "multipart"
      (testing "posting file"
        (let [tmp-file (java.io.File/createTempFile "foo" "bar")
              _ (spit tmp-file "Michiel Borkent")
              _ (.deleteOnExit tmp-file)
              body (:body (client/post "https://postman-echo.com/post"
                                       {:multipart [{:name "file"
                                                     :content (io/file tmp-file)}
                                                    {:name "filename" :content (.getPath tmp-file)}
                                                    ["file2" (io/file tmp-file)]]}))
              body (json/parse-string body true)
              headers (:headers body)
              content-type (:content-type headers)]
          (is (str/starts-with? content-type "multipart/form-data"))
          (is (:files body))
          (is (str/includes? (-> body :form :filename) "foo"))
          (prn body)))))

(deftest patch-test
  (is (str/includes?
       (:body (http/patch "https://postman-echo.com/patch"
                            {:body "hello"}))
       "hello")))

(deftest basic-auth-test
  (is (re-find #"authenticated.*true"
               (:body
                (http/get "https://postman-echo.com/basic-auth"
                            {:basic-auth ["postman" "password"]})))))

(deftest get-response-object-test
  (let [response (http/get "https://httpstat.us/200")]
    (is (map? response))
    (is (= 200 (:status response)))
    (is (= "200 OK" (:body response)))
    (is (string? (get-in response [:headers "server"]))))

  (testing "response object as stream"
    (let [response (http/get "https://httpstat.us/200" {:as :stream})]
      (is (map? response))
      (is (= 200 (:status response)))
      (is (instance? java.io.InputStream (:body response)))
      (is (= "200 OK" (slurp (:body response))))))

  (testing "response object with following redirect"
    (let [response (http/get "https://httpbin.org/redirect-to?url=https://www.httpbin.org")]
      (is (map? response))
      (is (= 200 (:status response)))))

  (testing "response object without fully following redirects"
    ;; (System/getProperty "jdk.httpclient.redirects.retrylimit" "0")
    (let [response (http/get "https://httpbin.org/redirect-to?url=https://www.httpbin.org"
                               {:client (http/client {:follow-redirects :never})})]
      (is (map? response))
      (is (= 302 (:status response)))
      (is (= "" (:body response)))
      (is (= "https://www.httpbin.org" (get-in response [:headers "location"])))
      (is (empty? (:redirects response))))))

(deftest accept-header-test
  (is (= 200
         (-> (http/get "https://httpstat.us/200"
                         {:accept :json})
             :body
             (json/parse-string true)
             :code))))

(deftest url-encode-query-params-test
  (is (= {"my query param?" "hello there"}
         (-> (http/get "https://postman-echo.com/get" {:query-params {"my query param?" "hello there"}})
             :body
             (json/parse-string)
             (get "args")))))

;; (deftest low-level-url-test
;;   (let [response (-> (client/request {:url {:scheme "https"
;;                                           :host   "httpbin.org"
;;                                           :port   443
;;                                           :path   "/get"
;;                                           :query  "q=test"}})
;;                      :body
;;                      (json/parse-string true))]
;;     (is (= {:q "test"} (:args response)))
;;     (is (= "httpbin.org" (get-in response [:headers :Host])))))

;; (deftest download-binary-file-as-stream-test
;;   (testing "download image"
;;     (let [tmp-file (java.io.File/createTempFile "icon" ".png")]
;;       (.deleteOnExit tmp-file)
;;       (io/copy (:body (client/get "https://github.com/babashka/babashka/raw/master/logo/icon.png" {:as :stream}))
;;                tmp-file)
;;       (is (= (.length (io/file "test" "icon.png"))
;;              (.length tmp-file)))))
;;   (testing "download image with response headers"
;;     (let [tmp-file (java.io.File/createTempFile "icon" ".png")]
;;       (.deleteOnExit tmp-file)
;;       (let [resp (client/get "https://github.com/babashka/babashka/raw/master/logo/icon.png" {:as :stream})]
;;         (is (= 200 (:status resp)))
;;         (io/copy (:body resp) tmp-file))
;;       (is (= (.length (io/file "test" "icon.png"))
;;              (.length tmp-file)))))
;;   (testing "direct bytes response"
;;     (let [tmp-file (java.io.File/createTempFile "icon" ".png")]
;;       (.deleteOnExit tmp-file)
;;       (let [resp (client/get "https://github.com/babashka/babashka/raw/master/logo/icon.png" {:as :bytes})]
;;         (is (= 200 (:status resp)))
;;         (is (= (Class/forName "[B") (class (:body resp))))
;;         (io/copy (:body resp) tmp-file)
;;         (is (= (count (:body resp))
;;                (.length (io/file "test" "icon.png"))
;;                (.length tmp-file)))))))

(deftest stream-test
  ;; This test aims to test what is tested manually as follows:
  ;; - from https://github.com/enkot/SSE-Fake-Server: npm install sse-fake-server
  ;; - start with: PORT=1668 node fakeserver.js
  ;; - ./bb '(let [resp (client/get "http://localhost:1668/stream" {:as :stream}) body (:body resp) proc (:process resp)] (prn (take 1 (line-seq (io/reader body)))) (.destroy proc))'
  ;; ("data: Stream Hello!")
  (let [server (java.net.ServerSocket. 1668)
        port (.getLocalPort server)]
    (future (try (with-open
                   [socket (.accept server)
                    out (io/writer (.getOutputStream socket))]
                   (binding [*out* out]
                     (println "HTTP/1.1 200 OK")
                     (println "Content-Type: text/event-stream")
                     (println "Connection: keep-alive")
                     (println)
                     (try (loop []
                            (println "data: Stream Hello!")
                            (Thread/sleep 20)
                            (recur))
                          (catch Exception _ nil))))
                 (catch Exception e
                   (prn e))))
    (let [resp (http/get (str "http://localhost:" port)
                           {:as :stream})
          status (:status resp)
          headers (:headers resp)
          body (:body resp)]
      (is (= 200 status))
      (is (= "text/event-stream" (get headers "content-type")))
      (is (= (repeat 2 "data: Stream Hello!") (take 2 (line-seq (io/reader body)))))
      (is (= (repeat 10 "data: Stream Hello!") (take 10 (line-seq (io/reader body))))))))

(deftest exceptional-status-test
    (testing "should throw"
      (let [ex (is (thrown? ExceptionInfo (http/get "https://httpstat.us/404")))
            response (ex-data ex)]
        (is (= 404 (:status response)))))
    (testing "should throw when streaming based on status code"
      (let [ex (is (thrown? ExceptionInfo (http/get "https://httpstat.us/404" {:throw true
                                                                                 :as :stream})))
            response (ex-data ex)]
        (is (= 404 (:status response)))
        (is (= "404 Not Found" (slurp (:body response))))))
    (testing "should not throw"
      (let [response (http/get "https://httpstat.us/404" {:throw false})]
        (is (= 404 (:status response))))))

(deftest compressed-test
  (let [resp (http/get "https://api.stackexchange.com/2.2/sites"
                         {:headers {"Accept-Encoding" ["gzip" "deflate"]}})]
    (is (-> resp :body (json/parse-string true) :items))))

(deftest default-client-test
  (let [resp (http/get "https://postman-echo.com/get")
        headers (-> resp :body (json/parse-string true) :headers)]
    (is (= "*/*" (:accept headers)))
    (is (= "gzip, deflate" (:accept-encoding headers)))))

(deftest client-request-opts-test
  (let [client (http/client {:request {:headers {"x-my-header" "yolo"}}})
        resp (http/get "https://postman-echo.com/get"
                       {:client client})
        header (-> resp :body (json/parse-string true) :headers :x-my-header)]
    (is (= "yolo" header))
    (let [resp (http/get "https://postman-echo.com/get"
                         {:client client :headers {"x-my-header" "dude"}})
          header (-> resp :body (json/parse-string true) :headers :x-my-header)]
      (is (= "dude" header)))))

(deftest header-with-keyword-key-test
  (is (= 200
         (-> (http/get "https://httpstat.us/200"
                         {:headers {:accept "application/json"}})
             :body
             (json/parse-string true)
             :code))))

(deftest follow-redirects-test
  (testing "default behaviour of following redirects automatically"
    (is (= 200 (:status (http/get "https://httpstat.us/302")))))

  (testing "follow redirects set to false"
    (is (= 302 (:status (http/get "https://httpstat.us/302" {:client (http/client {:follow-redirects false})}))))))

(deftest interceptor-test
  (let [json-interceptor
        {:name ::json
         :description
         "A request with `:as :json` will automatically get the
         \"application/json\" accept header and the response is decoded as JSON."
         :request (fn [request]
                    (if (= :json (:as request))
                      (-> (assoc-in request [:headers :accept] "application/json")
                          ;; Read body as :string
                          ;; Mark request as amenable to json decoding
                          (assoc :as :string ::json true))
                      request))
         :response (fn [response]
                     (if (get-in response [:request ::json])
                       (update response :body #(json/parse-string % true))
                       response))}
        ;; Add json interceptor add beginning of chain
        ;; It will be the first to see the request and the last to see the response
        interceptors (cons json-interceptor interceptors/default-interceptors)
        ]
    (testing "interceptors on request"
      (let [resp (http/get "https://httpstat.us/200"
                             {:interceptors interceptors
                              :as :json})]
        (is (= 200 (-> resp :body
                       ;; response as JSON
                       :code)))))))
