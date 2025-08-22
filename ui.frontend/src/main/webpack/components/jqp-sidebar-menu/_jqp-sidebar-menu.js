document.addEventListener("DOMContentLoaded", () => {
  const toggleSwitch = document.getElementById("toggleSwitch");
  const elNavBar = document.getElementById("nav-bar");

  let currentTheme = localStorage.getItem("theme");

  if (currentTheme == "night") {
    elNavBar.classList.add("night");
  } else {
    elNavBar.classList.remove("night");
  }

  toggleSwitch.addEventListener("change", function () {
    if (this.checked) {
      elNavBar.classList.add("night");
      localStorage.setItem("theme", "night");
    } else {
      elNavBar.classList.remove("night");
      localStorage.setItem("theme", "day");
    }
  });

  const navButtons = document.querySelectorAll(".toggle-submenu");

  navButtons.forEach((btn) => {
    btn.addEventListener("click", () => {
      const parent = btn.closest(".nav-item");

      document.querySelectorAll(".nav-item").forEach((el) => {
        if (el !== parent) el.classList.remove("active");
      });

      parent.classList.toggle("active");
    });
  });

  const toggleButtons = document.querySelectorAll(".toggleSidebar");

  toggleButtons.forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.preventDefault();
      document.getElementById("nav-bar").classList.toggle("collapsed");
    });
  });

  const buttons = document.querySelectorAll(".nav-button");
  const highlight = document.getElementById("nav-content-highlight");

  buttons.forEach((btn, index) => {
    btn.addEventListener("click", () => {
      // Remove existing active class
      buttons.forEach((b) => b.classList.remove("active"));
      // Add to clicked
      btn.classList.add("active");
      // Move highlight
      highlight.style.top = `calc(${index} * 54px + 5px)`;
    });
  });

  // Set initial highlight to first button
  highlight.style.top = `calc(0 * 54px + 5px)`;
});
