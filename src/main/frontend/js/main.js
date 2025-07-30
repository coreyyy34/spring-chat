import { OverlayManager } from "./overlay-manager.js";
import { SocketManager } from "./socket-manager.js";
import { ChannelManager } from "./channel-manager.js";
import { MessageManager } from "./message-manager.js";
import { OnlineUsersManager } from "./online-users-manager.js";
import { UI } from "./ui.js";
import "./responsive.js";

class App {
  constructor() {
    this.ui = new UI();
    this.overlayManager = new OverlayManager();
    this.socketManager = new SocketManager(this.overlayManager);
    this.channelManager = new ChannelManager(this.ui, this.socketManager);
    this.messageManager = new MessageManager(this.ui, this.socketManager, this.channelManager);
    this.onlineUsersManager = new OnlineUsersManager(this.ui, this.socketManager);
  }

  async init() {
    try {
      this.overlayManager.init();
      this.channelManager.init();
      this.messageManager.init();
      this.onlineUsersManager.init();
      await this.socketManager.connect();
    } catch (error) {
      console.error("Failed to initialize:", error);
    }
  }
}

document.addEventListener("DOMContentLoaded", () => {
  const app = new App();
  app.init();
});
