#!/usr/bin/env bb

(ns test-clj
  (:require
   [babashka.tasks :as tasks]
   [clojure.edn :as edn]
   [clojure.string :as str]))

(defn -main[& args]
  (let [farg (first args)
        ;; allow for missing leading colon
        farg (if (and farg (str/starts-with? farg "clj-"))
               (str ":" farg)
               farg)
        clj-version-aliases (->> "deps.edn"
                                 slurp
                                 edn/read-string
                                 :aliases
                                 keys
                                 (map str)
                                 (filter (fn [a] (-> a name (str/starts-with?  ":clj-"))))
                                 sort
                                 (into []))
        [aliases args] (cond
                         (nil? farg) [[":clj-1.11"] []]

                         (= ":clj-all" farg) [clj-version-aliases (rest args)]

                         (and (str/starts-with? farg ":clj-")
                              (not (some #{farg} clj-version-aliases)))
                         (throw (ex-info (format "%s not recognized, valid clj- args are: %s or \":clj-all\"" farg clj-version-aliases) {}))

                         (some #{farg} clj-version-aliases) [[farg] (rest args)]

                        :else [[":clj-1.11"] args])]
    (doseq [alias aliases]
      (println (format "-[Running jvm tests for %s]-" alias))
      (apply tasks/clojure (str "-M:test" alias) args))))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
