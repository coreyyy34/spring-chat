import {formatShortDate, formatTime, isToday, isYesterday} from "./date-utils";

export class MessageManager {
  constructor(ui, socketManager, channelManager) {
    this.ui = ui;
    this.socketManager = socketManager;
    this.channelManager = channelManager;
    this.lastMessage = null;
  }

  init() {
    this.ui.initMessagesUI(this.#handleOutgoingMessage.bind(this));
    this.socketManager.onMessage(this.#handleIncomingMessage.bind(this));
    this.channelManager.onChannelChange(this.#handleChannelChange.bind(this));
  }

  /**
   * Handles outgoing messages, triggered by the UI.
   * @private
   */
  async #handleOutgoingMessage(message) {
    try {
      await this.socketManager.sendMessage(this.channelManager.getCurrentChannelId(), message);
    } catch (error) {
      console.error("Failed to send message:", error);
    }
  }

  /**
   * Handles incoming messages received from the socket.
   * @private
   */
  #handleIncomingMessage(message) {
    const timestamp = new Date(message.timestamp);
    const msg = {
      id: message.id,
      fromId: message.fromId,
      fromUsername: message.fromUsername,
      channelId: message.channelId,
      timestamp: timestamp,
      formattedTimestamp: this.#formatIncomingMessageTimestamp(timestamp),
      content: message.content,
      isOwn: message.fromUsername === username
    };

    if (this.#isDifferentDate(message)) {
      this.ui.addDateSeparator(msg.timestamp)
    }
    this.ui.addMessage(msg);
    this.lastMessage = msg;
  }

  #formatIncomingMessageTimestamp(timestamp) {
    const today = isToday(timestamp);
    const yesterday = isYesterday(timestamp);
    const formattedTime = formatTime(timestamp)
    if (today) return formattedTime;
    if (yesterday) return `Yesterday, ${formattedTime}`

    const formattedDate = formatShortDate(timestamp);
    return `${formattedDate}, ${formattedTime}`
  }

  #isDifferentDate(message) {
    if (!this.lastMessage) {
      return true;
    }

    const a = new Date(message.timestamp);
    const b = new Date(this.lastMessage.timestamp);

    return (
        a.getFullYear() !== b.getFullYear() ||
        a.getMonth() !== b.getMonth() ||
        a.getDate() !== b.getDate()
    );
  }

  /**
   * Handles changes in the current channel. Clears existing messages and loads the
   * history for the new channel.
   * @private
   */
  async #handleChannelChange() {
    await this.#loadMessageHistory();
  }

  /**
   * Loads the message history for the current channel. Clears the UI's message display
   * and then adds the fetched historical messages.
   * @private
   */
  async #loadMessageHistory() {
    this.ui.clearMessages();

    try {
      const channelId = this.channelManager.getCurrentChannelId();
      const response = await fetch(`/api/v1/history/${channelId}`);
      const messages = await response.json();

      messages.forEach((msg) => this.#handleIncomingMessage(msg));
    } catch (error) {
      console.error("Failed to load message history:", error);
    }
  }
}
