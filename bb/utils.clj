(ns utils
  (:require [clojure.edn :as edn]
            [clojure.string :as str]))

(defn format-version []
  (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project :version))

(defn bump-version []
  (let [old-version (format-version)
        [major minor release] (str/split old-version #"\.")
        new-version (str/join "." [major minor (inc (parse-long release))])]
    (spit "deps.edn" (str/replace-first (slurp "deps.edn")
                                        (str ":version " (pr-str old-version))
                                        (str ":version " (pr-str new-version))))))
