(function () {
    "use strict";

    function onDocumentReady() {
        const container = document.querySelector(".container-parsys");
        const attribute = container.dataset.columns;

        for (let i = 0; i < attribute; i++) {
            const newSly = document.createElement("sly");
            newSly.setAttribute("data-sly-unwrap", "");
            newSly.setAttribute("data-sly-resource", "${@path='par', resourceType='foundation/components/parsys'}");
            container.appendChild(newSly);

        }



        // btnClick.addEventListener("click", (e) => {
        //   e.preventDefault();
        //   console.log("Card button clicked");
        // });
    }

    if (document.readyState !== "loading") {
        onDocumentReady();
    } else {
        document.addEventListener("DOMContentLoaded", onDocumentReady);
    }
}());