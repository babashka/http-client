(ns build
  (:require [clojure.edn :as edn]
            [clojure.tools.build.api :as b]))

(def project (-> (edn/read-string (slurp "deps.edn"))
                 :aliases :neil :project))
(def lib (or (:name project) 'my/lib1))

;; use neil project set version 1.2.0 to update the version in deps.edn

(def version (or (:version project)
                 "0.0.1"))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))
(def jar-file (format "target/%s-%s.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis basis
                :src-dirs ["src"]
                :scm {:url "https://github.com/babashka/http-client"
                      :connection "scm:git:git@github.com:babashka/http-client.git"
                      :developerConnection "scm:git:git@github.com:babashka/http-client.git"
                      :tag (format "v%s" version)}
                :pom-data
                [[:description "HTTP client for Clojure and Babashka built on java.net.http"]
                 [:url "https://github.com/babashka/http-client"]
                 [:licenses
                  [:license
                   [:name "MIT License"]
                   [:url "https://opensource.org/license/mit/"]]]]})
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))

(defn install [_]
  (jar {})
  (b/install {:basis basis
              :lib lib
              :version version
              :jar-file jar-file
              :class-dir class-dir}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis}))

(defn deploy [opts]
  (jar opts)
  ((requiring-resolve 'deps-deploy.deps-deploy/deploy)
    (merge {:installer :remote
                       :artifact jar-file
                       :pom-file (b/pom-path {:lib lib :class-dir class-dir})}
                    opts))
  opts)

