(function () {
    "use strict";

    function onDocumentReady() {

        setTimeout(() => {

            let pageCategory = document.querySelectorAll(".rapm-nav-pages-container .page-category-dynamic");
            if (pageCategory.length === 0) return;
            pageCategory.forEach(item => {
                item.textContent = item.textContent.split(':').slice(1).join(':').trim();
            });


        }, 100);

    }

    if (document.readyState !== "loading") {
        onDocumentReady();
    } else {
        document.addEventListener("DOMContentLoaded", onDocumentReady);
    }
}());