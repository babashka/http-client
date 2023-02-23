(ns babashka.http-client.websocket
  "Code is very much based on hato's websocket code. Credits to @gnarroway!"
  (:require
   [babashka.http-client.internal :as i]
   [babashka.http-client.internal.websocket :as w])
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
  * `:async` - return `CompleteableFuture` of websocket

  Callbacks options:
  * `:on-open` - `[ws]`, called when a `WebSocket` has been connected.
  * `:on-message` - `[ws data last]` A textual/binary data has been received.
  * `:on-ping` - `[ws data]` A Ping message has been received.
  * `:on-pong` - `[ws data]` A Pong message has been received.
  * `:on-close` - `[ws status reason]` Receives a Close message indicating the WebSocket's input has been closed.
  * `:on-error` - `[ws err]` An error has occurred."
  [{:keys [client]
    :as opts}]
  (let [client (or client (:client @i/default-client))]
    (w/websocket (assoc opts :client client))))

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
  ^CompletableFuture [ws data]
  (w/ping! ws data))

(defn pong!
  "Sends a Pong message with bytes from the given buffer."
  ^CompletableFuture [ws data]
  (w/pong! ws data))

(defn close!
  "Initiates an orderly closure of this WebSocket's output by sending a
  Close message with the given status code and the reason."
  (^CompletableFuture [ws]
   (w/close! ws))
  (^CompletableFuture [ws status-code reason]
   (w/close! ws status-code reason)))

(defn abort!
  "Closes this WebSocket's input and output abruptly."
  ^CompletableFuture [ws]
  (w/abort! ws))
