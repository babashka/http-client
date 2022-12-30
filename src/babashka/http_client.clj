(ns babashka.http-client
  (:refer-clojure :exclude [send get])
  (:require [babashka.http-client.internal :as i]))

(defn request [opts]
  (i/request opts))

(defn client [opts]
  (i/client opts))

(defn get
  ([uri] (get uri nil))
  ([uri opts]
   (let [opts (assoc opts :uri uri :method :get)]
     (request opts))))

(defn delete
  ([uri] (delete uri nil))
  ([uri opts]
   (let [opts (assoc opts :uri uri :method :delete)]
     (request opts))))

(defn head
  ([uri] (head uri nil))
  ([uri opts]
   (let [opts (assoc opts :uri uri :method :head)]
     (request opts))))

(defn post
  ([uri] (post uri nil))
  ([uri opts]
   (let [opts (assoc opts :uri uri :method :post)]
     (request opts))))

(defn patch
  ([url] (patch url nil))
  ([url opts]
   (let [opts (assoc opts
                     :uri url
                     :method :patch)]
     (request opts))))
