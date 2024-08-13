(println "Toggling warn on reflection to true...")
(alter-var-root #'*warn-on-reflection* (constantly true))
