(ns babashka.http-client.websocket-test
  (:require [babashka.http-client.websocket :as ws] :reload
            [org.httpkit.server :as srv]
            [clojure.test :as t :refer [deftest is]]))

(defn my-chatroom-handler
  [{:keys [on-receive on-close on-open on-ping]}]
  (fn [ring-req]
    (if-not (:websocket? ring-req)
      {:status 200 :body "Welcome to the chatroom! JS client connecting..."}
      (srv/as-channel ring-req
                      {:on-receive on-receive
                       :on-close on-close
                       :on-open on-open
                       :on-ping on-ping}))))

(deftest websocket-test
  (let [port 1345
        pings (atom [])
        received (atom [])
        pongs (atom [])
        actions (atom 0)
        srv (srv/run-server (my-chatroom-handler {:on-ping (fn [_ch data]
                                                             (swap! pings conj data)
                                                             (swap! actions inc))
                                                  :on-receive (fn [_ch data]
                                                                (swap! received conj data)
                                                                (swap! actions inc))})
                            {:port port
                             :legacy-return-value? false})
        ws (ws/websocket {:uri (str "ws://localhost:" port)
                          :on-pong (fn [_ch data]
                                     (swap! pongs conj data))})]
    (ws/send! ws "hello")
    (ws/ping! ws "yolo")
    (while (not= @actions 2))
    (is (= ["hello"] @received))
    (is (= 1 (count @pongs)))
    (srv/server-stop! srv)))