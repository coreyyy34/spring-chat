export class OnlineUsersManager {
  constructor(ui, socketManager) {
    this.ui = ui;
    this.socketManager = socketManager;
    this.onlineUsers = new Map();
    this.userCountElement = null;
    this.onlineUsersContainers = [];
  }

  init() {
    this.ui.initOnlineUsersUI();

    this.socketManager.onPresence(this.#handlePresenceUpdate.bind(this));
    this.socketManager.onConnect(() => this.#loadOnlineUsers());
  }

  /**
   * Handles incoming presence updates (JOINED/LEFT) from the socket.
   * @private
   */
  #handlePresenceUpdate(presenceMessage) {
    const { type, user } = presenceMessage;
    if (type === "JOINED") {
      this.#addOnlineUser(user);
    } else if (type === "LEFT") {
      this.#removeOnlineUser(user);
    }
  }

  /**
   * Fetches the current list of online users from the API and updates the UI.
   * @private
   */
  async #loadOnlineUsers() {
    try {
      const response = await fetch(`/api/v1/presence/online-users`);
      const users = await response.json();
      this.#clearOnlineUsers();
      users.forEach((user) => this.#addOnlineUser(user));
    } catch (error) {
      console.error("Failed to load online users:", error);
    }
  }

  /**
   * Adds an online user to the internal map and updates the UI.
   * @private
   */
  #addOnlineUser(user) {
    const { id, username } = user;
    if (!this.onlineUsers.has(id)) {
      this.onlineUsers.set(id, user);
      this.ui.addOnlineUser(id, username);
      this.ui.updateUserCount(this.onlineUsers.size);
    }
  }

  /**
   * Removes an online user from the internal map and updates the UI.
   * @private
   */
  #removeOnlineUser(user) {
    const { id } = user;
    if (this.onlineUsers.delete(id)) {
      this.ui.removeOnlineUser(id);
      this.ui.updateUserCount(this.onlineUsers.size);
    }
  }

  /**
   * Clears all online users from the internal map and the UI.
   * @private
   */
  #clearOnlineUsers() {
    this.onlineUsers.clear();
    this.ui.clearOnlineUsers();
    this.ui.updateUserCount(this.onlineUsers.size);
  }
}
