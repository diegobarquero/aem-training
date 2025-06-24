(function () {
  "use strict";

  function onDocumentReady() {
    const cardButtonCta = document.querySelector(".training-aem--card .btn");
    cardButtonCta.addEventListener("click", (e) => {
      e.preventDefault();
      console.log("Card button clicked");
    });
  }

  if (document.readyState !== "loading") {
    onDocumentReady();
  } else {
    document.addEventListener("DOMContentLoaded", onDocumentReady);
  }
})();
