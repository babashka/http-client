(ns babashka.http-client.websocket
  (:require [babashka.http-client.internal.websocket :as w]
            [babashka.http-client.internal :as i])
  (:import [java.util.concurrent CompletableFuture]))

(set! *warn-on-reflection* true)

(defn websocket
  "Builds `java.net.http.Websocket` client.
  * `:uri` - the uri to request (required).
     May be a string or map of `:schema` (required), `:host` (required), `:port`, `:path` and `:query`
  * `:headers` - a map of headers for the initial handshake`
  * `:client` - a client as produced by `client`. If not provided a default client will be used.
  * `:connect-timeout` Sets a timeout for establishing a WebSocket connection (in millis).
  * `:subprotocols` - sets a request for the given subprotocols.
  * `:async` - return `CompleteableFuture` of websocket"
  [{:keys [client]
    :or {client @i/default-client}
    :as opts}]
  (w/websocket (assoc opts :client client)))

(defn send!
  "Sends a message to the WebSocket.
  `data` can be a CharSequence (e.g. string), byte array or ByteBuffer

  Options:
  * `:last`: this is the last message, defaults to `true`"
  ([ws data]
   (send! ws data nil))
  ([ws data opts]
   (w/send! ws data opts)))

(defn ping!
  "Sends a Ping message with bytes from the given buffer."
  [ws data]
  (w/ping! ws data))

(defn pong!
  "Sends a Pong message with bytes from the given buffer."
  [ws data]
  (w/pong! ws data))

(defn close!
  "Initiates an orderly closure of this WebSocket's output by sending a
  Close message with the given status code and the reason."
  ([ws]
   (w/close! ws))
  ([ws status-code reason]
   (w/close! ws status-code reason)))

(defn abort!
  "Closes this WebSocket's input and output abruptly."
  [ws]
  (w/abort! ws))
