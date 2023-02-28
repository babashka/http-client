(ns babashka.http-client.internal.multipart
  "Multipart implementation largely inspired by hato. Credits to @gnarroway!"
  {:no-doc true}
  (:refer-clojure :exclude [get])
  (:require [clojure.java.io :as io])
  (:import [java.io InputStream File]
           [java.nio.file Files]))

(set! *warn-on-reflection* true)

;;; Helpers

(defn- content-disposition
  [{:keys [part-name name content file-name]}]
  (str "Content-Disposition: form-data; "
       (format "name=\"%s\"" (or part-name name))
       (when-let [fname (or file-name
                            (when (instance? File content)
                              (.getName ^File content)))]
         (format "; filename=\"%s\"" fname))))

(defn- content-type
  [{:keys [content content-type]}]
  (str "Content-Type: "
       (cond
         content-type content-type
         (string? content) "text/plain; charset=UTF-8"
         (instance? File content) (or (Files/probeContentType (.toPath ^File content))
                                      "application/octet-stream")
         :else "application/octet-stream")))

(defn- content-transfer-encoding
  [{:keys [content]}]
  (if (string? content)
    "Content-Transfer-Encoding: 8bit"
    "Content-Transfer-Encoding: binary"))

(def crlf "\r\n")

(defn boundary
  "Creates a boundary string compliant with RFC2046

  See https://www.ietf.org/rfc/rfc2046.txt"
  []
  (str "babashka_http_client_Boundary" (random-uuid)))

(defn concat-streams [^InputStream is1 ^InputStream is2 & more]
  (let [is (new java.io.SequenceInputStream is1 is2)]
    (if more
      (recur is (first more) (next more))
      is)))

(defn ->input-stream [x]
  (if (string? x)
    (java.io.ByteArrayInputStream. (.getBytes ^String x))
    (io/input-stream x)))

(defn body
  "Returns an InputStream from the multipart input."
  [ms b]
  (let [streams
        (mapcat (fn [m]
                  (map ->input-stream
                       [(str "--" b)
                        crlf
                        (content-disposition m)
                        crlf
                        (content-type m)
                        crlf
                        (content-transfer-encoding m)
                        crlf
                        crlf
                        (:content m)
                        crlf]))
                ms)
        concat-stream (apply concat-streams
                             (concat streams
                                     [(->input-stream (str "--" b "--"))
                                      (->input-stream crlf)]))]
    concat-stream))

(comment
  (def b (boundary))
  (def ms [{:name "title" :content "My Awesome Picture"}
           {:name "Content/type" :content "image/jpeg"}
           {:name "foo.txt" :part-name "eggplant" :content "Eggplants"}
           {:name "file" :content (io/file ".nrepl-port")}])
  (with-open [xin (io/input-stream (body ms b))
              xout (java.io.ByteArrayOutputStream.)]
    (io/copy xin xout)
    (String. (.toByteArray xout))))
