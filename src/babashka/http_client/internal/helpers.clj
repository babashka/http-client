(ns babashka.http-client.internal.helpers
  {:no-doc true})

(defn ->uri [uri]
  (cond (string? uri) uri
        (map? uri)
        (str (java.net.URI. ^String (:scheme uri)
                            ^String (:user uri)
                            ^String (:host uri)
                            ^Integer (:port uri)
                            ^String (:path uri)
                            ^String (:query uri)
                            ^String (:fragment uri)))
        :else uri))

(defn coerce-key
  "Coerces a key to str"
  [k]
  (if (keyword? k)
    (-> k str (subs 1))
    (str k)))

(defn coerce-headers
  [headers]
  (mapcat
   (fn [[k v]]
     (if (sequential? v)
       (interleave (repeat (coerce-key k)) v)
       [(coerce-key k) v]))
   headers))
