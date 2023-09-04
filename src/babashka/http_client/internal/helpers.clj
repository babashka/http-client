(ns babashka.http-client.internal.helpers
  {:no-doc true}
  (:require [clojure.string :as str]))

(defn ->uri [uri]
  (cond (string? uri) (java.net.URI/create uri)
        (map? uri)
        (java.net.URI. ^String (:scheme uri)
                       ^String (:user uri)
                       ^String (:host uri)
                       ^Integer (:port uri)
                       ^String (:path uri)
                       ^String (:query uri)
                       ^String (:fragment uri))
        :else uri))

(defn coerce-key
  "Coerces a key to str"
  [k]
  (if (keyword? k)
    (-> k str (subs 1))
    (str k)))

(defn capitalize-header [hdr]
  (str/join "-" (map str/capitalize (str/split hdr #"-"))))

(defn prefer-string-keys
  "Dissoc-es keyword header if equivalent string header is available already."
  [header-map]
  (reduce (fn [m k]
            (if (keyword? k)
              (let [s (coerce-key k)]
                (if (or (clojure.core/get header-map (capitalize-header s))
                        (clojure.core/get header-map s))
                  (dissoc m k)
                  m))
              m))
          header-map
          (keys header-map)))

(defn coerce-headers
  [headers]
  (mapcat
   (fn [[k v]]
     (if (sequential? v)
       (interleave (repeat (coerce-key k)) v)
       [(coerce-key k) v]))
   headers))

;;;;

(comment
  (prefer-string-keys {:accept 1 "Accept" 2})
  )
