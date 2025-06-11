// (function () {
//     "use strict";

//     function onDocumentReady() {
//         setTimeout(() => {
//             const containerProChildren = document.querySelector('[title="Container PRO"] > [data-type="Editable"]');
//             console.log(containerProChildren.lastChild);
//             if (containerProChildren.childElementCount === 2) return
//             containerProChildren.lastChild.style.visibility = "hidden"

//         }, 1000);


//     }

//     if (document.readyState !== "loading") {
//         onDocumentReady()

//     } else {
//         document.addEventListener("DOMContentLoaded", onDocumentReady);
//         if (window.Granite?.author?.ContentFrame) {
//             window.Granite.author.ContentFrame.addEventListener('content-update', checkAllParsys);
//         }
//     }
// }());
(function (document, $) {
    const checkAllParsys = () => {

        setTimeout(() => {
            const containerProChildren = $('[title="Container PRO"] > [data-type="Editable"]');
            containerProChildren.each(function () {
                const childNodes = this.childNodes;
                const lastNode = childNodes[childNodes.length - 1];
                if (this.childNodes.length > 2) {
                    lastNode.style.visibility = 'hidden';
                }
                
            });



        }, 1000);

    };

    // On initial content load (including after dialog saves)
    $(document).on('foundation-contentloaded', checkAllParsys);

    // On component drop/remove in author mode
    
    $(document).on('cq-content-updated', checkAllParsys);
})(document, Granite.$);