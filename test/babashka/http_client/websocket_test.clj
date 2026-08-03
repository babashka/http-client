(ns babashka.http-client.websocket-test
  (:require [babashka.http-client.websocket :as ws] :reload
            [babashka.http-client :as http]
            [babashka.http-client.test-socks-server :as socks]
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

(def port (atom 1446))

(deftest websocket-test
  (let [port (swap! port inc)
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
                          :headers {:foo "bar"}
                          :on-pong (fn [_ch data]
                                     (swap! pongs conj data)
                                     (swap! actions inc))})]
    (ws/send! ws "hello")
    (ws/ping! ws "yolo")
    (while (not= @actions 3))
    (is (= ["hello"] @received))
    (is (= 1 (count @pongs)))
    (srv/server-stop! srv)))

(deftest websocket-through-socks5-test
  (let [port (swap! port inc)
        received (atom [])
        actions (atom 0)
        srv (srv/run-server (fn [req]
                              (srv/as-channel req {:on-receive (fn [ch data]
                                                                 (swap! received conj data)
                                                                 (swap! actions inc)
                                                                 (srv/send! ch (str "echo:" data)))}))
                            {:port port
                             :legacy-return-value? false})
        socks (socks/start)
        client (http/client {:proxy {:type :socks5
                                     :host "127.0.0.1"
                                     :port (:port socks)}})
        echoed (atom [])
        ws (ws/websocket {:uri (str "ws://localhost:" port)
                          :client client
                          :on-message (fn [_ws data _last]
                                        (swap! echoed conj (str data))
                                        (swap! actions inc))})]
    (ws/send! ws "hello")
    (while (not= @actions 2))
    (is (= ["hello"] @received))
    (is (= ["echo:hello"] @echoed))
    (is (= [["localhost" port]] @(:targets socks)))
    ((:stop socks))
    (srv/server-stop! srv)))
