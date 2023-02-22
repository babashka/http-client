(ns babashka.http-client.websocket-test
  (:require [babashka.http-client.websocket :as ws] :reload
            [org.httpkit.server :as srv]))

(defn my-chatroom-handler [ring-req]
  (if-not (:websocket? ring-req)
    {:status 200 :body "Welcome to the chatroom! JS client connecting..."}
    (srv/as-channel ring-req
                    {:on-receive (fn [ch message] (println "on-receive:" message))
                     :on-close (fn [ch status] (println "on-close:" status))
                     :on-open (fn [ch] (println "on-open:" ch))
                     :on-ping (fn [ch data] (println "on-ping" ch data))})))

(comment (srv/run-server my-chatroom-handler {:port 1339})

         (def ws (ws/websocket {:uri "ws://localhost:1339"}))

         @(ws/ping! ws (.getBytes "foo"))
         @(ws/send! ws (.getBytes "dude")))
