(function () {
  "use strict";

  function getRandomColor() {
    let letters = "0123456789ABCDEF";
    let color = "#";
    for (let i = 0; i < 6; i++) {
      color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
  }

  function onDocumentReady() {
    const btnClick = document.querySelector(".btn");
    btnClick.addEventListener("click", (e) => {
      e.preventDefault();
      document.querySelector(".dot").style.backgroundColor = getRandomColor();
      console.log("Card button clicked - changing dot color");
    });
  }

  if (document.readyState !== "loading") {
    onDocumentReady();
  } else {
    document.addEventListener("DOMContentLoaded", onDocumentReady);
  }
})();
