(ns babashka.http-client.internal.aux
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
