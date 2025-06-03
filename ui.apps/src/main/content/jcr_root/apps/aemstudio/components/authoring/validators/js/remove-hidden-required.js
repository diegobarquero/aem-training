(function ($) {
    'use strict';

    $(window).adaptTo('foundation-registry').register('foundation.validation.validator', {
        selector: '[data-validation|="mf-remove-hidden-required"],[data-validation*=",mf-remove-hidden-required"]',
        validate: function (el) {
            var targetClass = $(el).data("cqDialogDropdownShowhideTarget"),
                elements = $(el).parents(".carousel-slide").eq(0).find(targetClass);

                // Loop over the multifield elements based on the cqDialogDropdownShowhideTarget
                elements.each(function() {
                    if ($(this).hasClass("hide")){
                        // If hidden, remove the required atributes and add the temporay class to change it later, just in case
                        $(this).find('input[aria-required="true"]').each(function() {
                            $(this).attr({required:false,"aria-required":false}).addClass("was-required");
                        })
                    } else {
                        // If visible, check if there are rewuired that need to be set and do so, remove the temp class
                        $(this).find('.was-required').each(function() {
                            $(this).attr({required:true,"aria-required":true}).removeClass("was-required");
                        })
                    }
                });

        }
    });
}) (jQuery);
