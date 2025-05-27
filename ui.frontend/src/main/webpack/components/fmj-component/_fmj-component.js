(function () {
  "use strict";

  function setupProductCardButtons() {
    // Select all product card buttons on the page
    var buttons = document.querySelectorAll(".product-card__button");
    buttons.forEach(function (button) {
      button.addEventListener("click", function (event) {
        event.preventDefault();
        console.log("Product card button clicked:", button.textContent.trim());
      });
    });
  }

  if (document.readyState !== "loading") {
    setupProductCardButtons();
  } else {
    document.addEventListener("DOMContentLoaded", setupProductCardButtons);
  }
})();
