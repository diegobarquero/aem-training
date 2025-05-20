(function () {
  "use strict";

  function onDocumentReady() {
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
