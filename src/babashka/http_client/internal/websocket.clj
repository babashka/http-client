(ns babashka.http-client.internal.websocket
  "Largely based on hato code."
  (:require
   [babashka.http-client.interceptors :as ics])
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

(defn request->WebSocketListener
  "Constructs a new WebSocket listener to receive events for a given WebSocket connection.

  Takes a map of:

  - `:on-open`    Called when a `WebSocket` has been connected. Called with the WebSocket instance.
  - `:on-message` A textual/binary data has been received. Called with the WebSocket instance, the data, and whether this invocation completes the message.
  - `:on-ping`    A Ping message has been received. Called with the WebSocket instance and the ping message.
  - `:on-pong`    A Pong message has been received. Called with the WebSocket instance and the pong message.
  - `:on-close`   Receives a Close message indicating the WebSocket's input has been closed. Called with the WebSocket instance, the status code, and the reason.
  - `:on-error`   An error has occurred. Called with the WebSocket instance and the error."
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
  (reduce-kv
   (fn [^WebSocket$Builder b ^String hk ^String hv]
     (.header b hk hv))
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
      true (.buildAsync (URI/create (ics/uri->str uri)) listener)
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

(defn ^CompletableFuture ping!
  "Sends a Ping message with bytes from the given buffer."
  [^WebSocket ws data]
  (.sendPing ws (->buffer data)))

(defn ^CompletableFuture pong!
  "Sends a Pong message with bytes from the given buffer."
  [^WebSocket ws ^ByteBuffer data]
  (.sendPong ws (->buffer data)))

(defn ^CompletableFuture close!
  "Initiates an orderly closure of this WebSocket's output by sending a
  Close message with the given status code and the reason."
  ([^WebSocket ws]
   (close! ws WebSocket/NORMAL_CLOSURE ""))
  ([^WebSocket ws status-code ^String reason]
   (.sendClose ws status-code reason)))

(defn abort!
  "Closes this WebSocket's input and output abruptly."
  [^WebSocket ws]
  (.abort ws))
