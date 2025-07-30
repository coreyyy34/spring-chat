export class SocketManager {
  constructor(overlayManager) {
    this.overlayManager = overlayManager;
    this.socket = null;
    this.stompClient = null;
    this.isConnected = false;
    this.subscriptions = {};
    this.connectionHandlers = [];
    this.messageHandlers = [];
    this.presenceHandlers = [];
  }

  async connect() {
    this.stompClient = new StompJs.Client({
      brokerURL: "/ws",
      onConnect: this.#onSocketConnect,
      onStompError: this.#onSocketError,
      onWebSocketClose: this.#onSocketDisconnect,
    });
    this.stompClient.activate();
  }

  #onSocketConnect = (frame) => {
    console.log("Socket connected");
    this.isConnected = true;
    this.#setupSystemSubscriptions();
    this.connectionHandlers.forEach((handler) => handler());
    this.overlayManager.hide();
  };

  #onSocketDisconnect = (frame) => {
    console.log("Socket closed");
    this.overlayManager.show();
  };

  #onSocketError = (error) => {
    console.error("Socket error", error);
  };

  #setupSystemSubscriptions() {
    try {
      this.stompClient.subscribe("/topic/presence", (message) => {
        const presenceMessage = JSON.parse(message.body);
        this.presenceHandlers.forEach((handler) => handler(presenceMessage));
      });
    } catch (error) {
      console.error("Error setting up system subscriptions:", error);
    }
  }

  // Channel Subscriptions
  async subscribeToChannel(channelId) {
    if (!this.isConnected) {
      throw new Error("Cannot subscribe: STOMP client not connected");
    }

    const destination = `/topic/channel/${channelId}`;
    try {
      this.subscriptions[destination] = this.stompClient.subscribe(destination, (message) => {
        try {
          const parsedMessage = JSON.parse(message.body);
          this.messageHandlers.forEach((handler) => handler(parsedMessage));
        } catch (error) {
          console.error("Error parsing message:", error);
        }
      });
      console.log(`Subscribed to ${destination}`);
    } catch (error) {
      throw new Error(`Failed to subscribe to channel ${channelId}: ${error.message}`);
    }
  }

  async unsubscribeFromChannel(channelId) {
    if (!this.isConnected) {
      console.warn("Cannot unsubscribe: STOMP client not connected");
      return;
    }

    const destination = `/topic/channel/${channelId}`;
    try {
      if (this.subscriptions[destination]) {
        this.subscriptions[destination].unsubscribe();
        delete this.subscriptions[destination];
        console.log(`Unsubscribed from ${destination}`);
      }
    } catch (error) {
      console.error(`Error unsubscribing from channel ${channelId}:`, error);
    }
  }

  // Message Sending
  async sendMessage(channelId, content) {
    if (!this.isConnected) {
      throw new Error("Cannot send message: STOMP client not connected");
    }

    const message = JSON.stringify({
      channelId,
      content,
    });

    try {
      console.log(`Sending message to channel ${channelId}`);
      this.stompClient.publish({
        destination: "/app/chat",
        body: message,
      });
      console.log(`Message sent to channel ${channelId}`);
    } catch (error) {
      console.error(`Failed to send message to channel ${channelId}:`, error);
    }
  }

  // Handler Registers
  onMessage(handler) {
    if (typeof handler !== "function") {
      throw new Error("Message handler must be a function");
    }
    this.messageHandlers.push(handler);
  }

  onPresence(handler) {
    if (typeof handler !== "function") {
      throw new Error("Presence handler must be a function");
    }
    this.presenceHandlers.push(handler);
  }

  onConnect(handler) {
    if (typeof handler !== "function") {
      throw new Error("Connect handler must be a function");
    }
    this.connectionHandlers.push(handler);
  }
}
