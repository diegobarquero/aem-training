(function () {
  "use strict";

  function onDocumentReady() {
    const btnClick = document.querySelector(".link");
    console.log('Executing Javascript for fvr-component');
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
