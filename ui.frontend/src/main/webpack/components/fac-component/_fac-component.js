(function () {
"use strict";

function onDocumentReady() {
    const btn = document.querySelector(".card-link");
    if (!btn) return;

    btn.addEventListener("click", function (e) {
    e.preventDefault();
    this.classList.toggle("clicked");
    console.log("Card button clicked");
    });
}

if (document.readyState !== "loading") {
    onDocumentReady();
} else {
    document.addEventListener("DOMContentLoaded", onDocumentReady);
}
})();
