(ns babashka.http-client.internal.websocket
  "Code is very much based on hato's websocket code. Credits to @gnarroway!"
  {:no-doc true}
  (:require
   [babashka.http-client.internal.helpers :as aux])
  (:import
   [java.net URI]
   [java.net.http
    HttpClient
    WebSocket
    WebSocket$Builder
    WebSocket$Listener]
   [java.nio ByteBuffer]
   [java.time Duration]
   [java.util.concurrent CompletableFuture]
   [java.util.function Function]))

(set! *warn-on-reflection* true)

(defn request->WebSocketListener
  [{:keys [on-open
           on-message
           on-ping
           on-pong
           on-close
           on-error]}]
  ; The .requests below is from the implementation of the default listener
  (reify WebSocket$Listener
    (onOpen [_ ws]
      (.request ws 1)
      (when on-open
        (on-open ws)))
    (onText [_ ws data last?]
      (.request ws 1)
      (when on-message
        (.thenApply (CompletableFuture/completedFuture nil)
                    (reify Function
                      (apply [_ _] (on-message ws data last?))))))
    (onBinary [_ ws data last?]
      (.request ws 1)
      (when on-message
        (.thenApply (CompletableFuture/completedFuture nil)
                    (reify Function
                      (apply [_ _] (on-message ws data last?))))))
    (onPing [_ ws data]
      (.request ws 1)
      (when on-ping
        (.thenApply (CompletableFuture/completedFuture nil)
                    (reify Function
                      (apply [_ _] (on-ping ws data))))))
    (onPong [_ ws data]
      (.request ws 1)
      (when on-pong
        (.thenApply (CompletableFuture/completedFuture nil)
                    (reify Function
                      (apply [_ _] (on-pong ws data))))))
    (onClose [_ ws status reason]
      (when on-close
        (.thenApply (CompletableFuture/completedFuture nil)
                    (reify Function
                      (apply [_ _] (on-close ws status reason))))))
    (onError [_ ws err]
      (when on-error
        (on-error ws err)))))

(defn- with-headers
  ^WebSocket$Builder [builder headers]
  (reduce (fn [^WebSocket$Builder builder [k v]]
            (.header builder (aux/coerce-key k) v))
          builder
          headers))

(defn websocket
  [{:keys [uri
           client
           headers
           connect-timeout
           subprotocols
           async]
    :as opts}]
  (let [^HttpClient http-client client
        ^WebSocket$Listener listener (request->WebSocketListener opts)]
    (cond-> (.newWebSocketBuilder http-client)
      connect-timeout (.connectTimeout (Duration/ofMillis connect-timeout))
      (seq subprotocols) (.subprotocols (first subprotocols) (into-array String (rest subprotocols)))
      headers (with-headers headers)
      true (.buildAsync (aux/->uri uri) listener)
      (not async) deref)))

(defn ->buffer ^java.nio.ByteBuffer [x]
  (cond (bytes? x)
        (java.nio.ByteBuffer/wrap ^bytes x)
        (string? x) (recur (.getBytes ^String x))
        :else x))

(defn send!
  ([^WebSocket ws data]
   (send! ws data nil))
  ([^WebSocket ws data {:keys [last] :or {last true}}]
   (cond (instance? CharSequence data)
         (.sendText ws ^CharSequence data last)
         :else
         (.sendBinary ws ^java.nio.ByteBuffer (->buffer data) last))))

(defn ping!
  [^WebSocket ws data]
  (.sendPing ws (->buffer data)))

(defn pong!
  [^WebSocket ws ^ByteBuffer data]
  (.sendPong ws (->buffer data)))

(defn close!
  ([^WebSocket ws]
   (close! ws WebSocket/NORMAL_CLOSURE ""))
  ([^WebSocket ws status-code ^String reason]
   (.sendClose ws status-code reason)))

(defn abort!
  [^WebSocket ws]
  (.abort ws))
