import {formatMonthDayYear} from "./date-utils";

export class UI {
    /* Messages */
    initMessagesUI(onSendMessageCallback) {
        this.messageForm = this.#getElement("message-form");
        this.messagesContainer = this.#getElement("message-container");

        if (this.messageForm) {
            this.messageForm.addEventListener("submit", (event) => {
                event.preventDefault();

                const formData = new FormData(event.target);
                const message = formData.get("message-input").trim();
                if (!message || message === "") {
                    return;
                }

                onSendMessageCallback(message);
                event.target.reset();
            });
        }

        const messageInput = document.getElementById("message-input");
        const MAX_LENGTH = 512;

        if (messageInput) {
            messageInput.addEventListener("input", (e) => {
                if (e.target.value.length > MAX_LENGTH) {
                    e.target.value = e.target.value.slice(0, MAX_LENGTH);
                }
            });
        }
    }

    addMessage(message) {
        const templateId = message.isOwn ? "own-message-template" : "message-template";

        this.#createMessageElement(templateId, message);
        this.previousMessageFromId = message.fromId;
    }

    clearMessages() {
        if (this.messagesContainer) {
            this.messagesContainer.innerHTML = "";
        }
        this.previousMessageFromId = null;
    }

    addDateSeparator(date) {
        if (!this.messagesContainer) {
            console.error("Message container not initialized for message element.");
            return;
        }

        const template = document.getElementById("date-separator-template")
        if (!template) {
            console.error("Template date-separator-template not found");
            return;
        }

        const clone = template.content.cloneNode(true);
        const dateElement = clone.querySelector(".date");
        if (dateElement) dateElement.innerHTML = formatMonthDayYear(date);

        this.messagesContainer.appendChild(clone);
    }

    #createMessageElement(templateId, message) {
        const container = this.messagesContainer;
        if (!container) {
            console.error("Message container not initialized for message element.");
            return null;
        }

        const template = document.getElementById(templateId);
        if (!template) {
            console.error(`Template ${templateId} not found`);
            return null;
        }

        const clone = template.content.cloneNode(true);
        const {fromId, fromUsername, content, formattedTimestamp} = message;
        const showHeader = !this.previousMessageFromId || fromId !== this.previousMessageFromId;
        const fromContainer = clone.querySelector(".from-container");
        const fromProfileContainer = clone.querySelector(".from-profile-container");
        const fromProfileInitial = clone.querySelector(".from-profile-initial");
        const userNameElement = clone.querySelector(".user-name");
        const timestampElement = clone.querySelector(".timestamp");
        const contentElement = clone.querySelector(".message-content");

        if (fromContainer) fromContainer.setAttribute("data-show", showHeader.toString());
        if (fromProfileContainer) fromProfileContainer.setAttribute("data-show", showHeader.toString());
        if (fromProfileInitial) fromProfileInitial.textContent = fromUsername[0];
        if (userNameElement) userNameElement.textContent = fromUsername;
        if (timestampElement) timestampElement.textContent = formattedTimestamp;
        if (contentElement) contentElement.textContent = content;

        container.appendChild(clone);

        // Scroll to the bottom if the container has a parent (assuming it's scrollable)
        const parent = container.parentElement;
        if (parent) {
            requestAnimationFrame(() => this.#scrollToBottom(parent));
        }
    }

    /* Channels */
    initChannelsUI(onChannelClickCallback) {
        this.channelButtons = document.querySelectorAll("button[data-channel-id]");
        this.currentChannelHeader = this.#getElement("current-channel-header");

        const channels = [];

        if (this.channelButtons.length === 0) {
            console.warn("No channel buttons found");
            return channels;
        }

        this.channelButtons.forEach((button) => {
            const channelId = parseInt(button.dataset.channelId);
            const channelName = button.dataset.channelName;

            channels.push({
                id: channelId,
                name: channelName,
            });

            button.addEventListener("click", () => {
                onChannelClickCallback(channelId, channelName);
            });
        });

        return channels;
    }

    updateActiveChannelUI(channelId, channelName) {
        if (this.currentChannelHeader) {
            this.currentChannelHeader.textContent = channelName;
        }

        if (this.channelButtons) {
            this.channelButtons.forEach((btn) => {
                const active = parseInt(btn.dataset.channelId) === channelId;
                btn.setAttribute("data-active", active.toString());
            });
        }
    }

    /* Online Users */
    initOnlineUsersUI() {
        this.onlineUsersContainers = [];
        this.userCountElement = this.#getElement("onlineUsersCount");

        const onlineUsersContainer = this.#getElement("onlineUsersContainer");
        if (onlineUsersContainer) {
            this.onlineUsersContainers.push(onlineUsersContainer);
        }

        const mobileOnlineUsersContainer = this.#getElement("mobileOnlineUsersContainer");
        if (mobileOnlineUsersContainer) {
            this.onlineUsersContainers.push(mobileOnlineUsersContainer);
        }
    }

    addOnlineUser(userId, username) {
        if (this.onlineUsersContainers.length === 0) {
            console.error("Cannot render user: online user containers not initialized.");
            return;
        }

        const template = this.#getElement("online-user-template");
        if (!template) {
            console.error("Online user template not found");
            return;
        }

        const clone = template.content.cloneNode(true);
        const userElement = clone.querySelector("div");
        const initialElement = clone.querySelector(".online-user-initial");
        const nameElement = clone.querySelector(".online-user-name");

        if (userElement) userElement.setAttribute("data-user-id", userId);
        if (initialElement) initialElement.textContent = username[0];
        if (nameElement) nameElement.textContent = username;

        this.onlineUsersContainers.forEach((container) => {
            container.appendChild(clone.cloneNode(true)); // clone for each container
        });
    }

    removeOnlineUser(userId) {
        this.onlineUsersContainers.forEach((container) => {
            const element = container.querySelector(`[data-user-id="${userId}"]`);
            if (element) {
                container.removeChild(element);
            }
        });
    }

    clearOnlineUsers() {
        this.onlineUsersContainers.forEach((container) => {
            container.innerHTML = "";
        });
    }

    updateUserCount(count) {
        if (this.userCountElement) {
            this.userCountElement.textContent = count.toString();
        }
    }

    /* Utils */
    #getElement(id) {
        const element = document.getElementById(id);
        if (!element) {
            console.error(`Element with ID ${id} not found`);
        }
        return element;
    }

    #scrollToBottom(container) {
        container.scrollTop = container.scrollHeight;
    }

    #formatTime(date) {
        try {
            return date.toLocaleTimeString("en-NZ", {
                hour: "numeric",
                minute: "2-digit",
                hour12: true,
            });
        } catch (error) {
            console.error("Failed to format time:", date, error);
            return "Invalid time";
        }
    }

    #formatDateAndTime(date) {
        try {
            const datePart = date.toLocaleDateString("en-NZ", {
                year: "2-digit",
                month: "2-digit",
                day: "2-digit",
            });
            const timePart = date.toLocaleTimeString("en-NZ", {
                hour: "numeric",
                minute: "2-digit",
                hour12: true,
            });

            return `${datePart} ${timePart}`;
        } catch (error) {
            console.error("Failed to format date and time:", date, error);
            return "Invalid date";
        }
    }
}
