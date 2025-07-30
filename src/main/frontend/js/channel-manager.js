export class ChannelManager {
  constructor(ui, socketManager) {
    this.ui = ui;
    this.socketManager = socketManager;
    this.channels = [];
    this.currentChannelId = null;
    this.currentChannelName = null;
    this.channelChangeHandler = null;
  }

  init() {
    this.channels = this.ui.initChannelsUI(this.#handleChannelClick.bind(this));

    if (this.channels.length === 0) {
      console.warn("No channel buttons found, cannot initialize.");
      return;
    }

    const defaultChannel = this.channels[0];

    this.socketManager.onConnect(() => {
      // First connection - use default
      if (this.currentChannelId === null) {
        this.#switchChannel(defaultChannel.id, defaultChannel.name).catch((err) =>
          console.error("Initial channel switch failed:", err)
        );
      } else {
        // Reconnection - re-subscribe to current channel
        this.#switchChannel(this.currentChannelId, this.currentChannelName).catch((err) =>
          console.error("Resubscribe failed:", err)
        );
      }
    });
  }

  /**
   * Handles a click event on a channel button, triggered by the UI. Prevents switching
   * if the clicked channel is already active.
   * @private
   */
  #handleChannelClick(channelId, channelName) {
    if (channelId === this.currentChannelId) {
      return;
    }

    this.#switchChannel(channelId, channelName).catch((err) => {
      console.error("Channel switch failed:", err);
    });
  }

  /**
   * Orchestrates the actual channel switching process. Unsubscribes from the previous
   * channel (if any), subscribes to the new one, updates internal state, and notifies
   * the UI and other subscribers.
   * @private
   */
  async #switchChannel(channelId, channelName) {
    console.log(`Switching to channel ID ${channelId}`);
    try {
      if (this.currentChannelId !== null && this.currentChannelId !== channelId) {
        await this.socketManager.unsubscribeFromChannel(this.currentChannelId);
      }

      await this.socketManager.subscribeToChannel(channelId);
      this.currentChannelId = channelId;
      this.currentChannelName = channelName;
      this.ui.updateActiveChannelUI(channelId, channelName);

      if (this.channelChangeHandler) {
        this.channelChangeHandler(channelId);
      }
    } catch (error) {
      console.error("Error switching channels:", error);
      throw error;
    }
  }

  /**
   * Returns the ID of the channel that is currently active.
   */
  getCurrentChannelId() {
    return this.currentChannelId;
  }

  /**
   * Registers a callback function to be invoked when the active channel changes.
   */
  onChannelChange(callback) {
    if (typeof callback !== "function") {
      throw new Error("Channel change handler must be a function");
    }
    this.channelChangeHandler = callback;
  }
}
