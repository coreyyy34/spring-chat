document.addEventListener("DOMContentLoaded", () => {
  setupSidebarToggle();
  setupOnlineUsersToggle();
});

const setupSidebarToggle = () => {
  const openSidebar = document.getElementById("open-sidebar");
  const closeSidebar = document.getElementById("close-sidebar");
  const sidebar = document.getElementById("sidebar");
  const sidebarOverlay = document.getElementById("sidebar-overlay");

  openSidebar.addEventListener("click", () => {
    sidebar.classList.remove("-translate-x-full");
    sidebarOverlay.classList.remove("hidden");
  });

  closeSidebar.addEventListener("click", () => {
    sidebar.classList.add("-translate-x-full");
    sidebarOverlay.classList.add("hidden");
  });

  sidebarOverlay.addEventListener("click", () => {
    sidebar.classList.add("-translate-x-full");
    sidebarOverlay.classList.add("hidden");
  });
};

const setupOnlineUsersToggle = () => {
  const toggleUsers = document.getElementById("toggle-users");
  const closeUsers = document.getElementById("close-users");
  const usersOverlay = document.getElementById("users-overlay");
  const mobileUsersPanel = document.getElementById("mobile-users-panel");

  toggleUsers.addEventListener("click", () => {
    usersOverlay.classList.remove("hidden");
    setTimeout(() => {
      mobileUsersPanel.classList.remove("translate-x-full");
    }, 10);
  });

  closeUsers.addEventListener("click", () => {
    mobileUsersPanel.classList.add("translate-x-full");
    setTimeout(() => {
      usersOverlay.classList.add("hidden");
    }, 300);
  });

  usersOverlay.addEventListener("click", (e) => {
    if (e.target === usersOverlay) {
      mobileUsersPanel.classList.add("translate-x-full");
      setTimeout(() => {
        usersOverlay.classList.add("hidden");
      }, 300);
    }
  });
};
