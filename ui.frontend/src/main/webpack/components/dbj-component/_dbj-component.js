(function () {
  "use strict";

  function onDocumentReady() {
    const component = document.querySelector(".dbj-component");
    if (!component) return; // Exit early if component not found

    const btnClick = document.querySelector(".card-link");

    btnClick.addEventListener("click", (e) => {
      e.preventDefault();
      console.log("Card button clicked");
    });
  }

  if (document.readyState !== "loading") {
    onDocumentReady();
  } else {
    document.addEventListener("DOMContentLoaded", onDocumentReady);
  }
}());
