export class OverlayManager {
  constructor() {
    this.overlay = null;
    this.connectingOverlayMessage = null;
  }

  init() {
    this.connectingOverlay = document.getElementById("connecting-overlay");
    if (!this.connectingOverlay) {
      console.error("Connecting overlay not found");
    }
    this.connectingOverlayMessage = document.getElementById("connecting-overlay-message");
    if (!this.connectingOverlayMessage) {
      console.error("Connecting overlay message not found");
    }
  }

  show(message = "Connecting...") {
    if (this.connectingOverlay) {
      this.connectingOverlay.classList.add("flex");
      this.connectingOverlay.classList.remove("hidden");
    }
    if (this.connectingOverlayMessage) {
      this.connectingOverlayMessage.textContent = message;
    }
  }

  hide() {
    if (this.connectingOverlay) {
      this.connectingOverlay.classList.add("hidden");
      this.connectingOverlay.classList.remove("flex");
    }
  }
}
