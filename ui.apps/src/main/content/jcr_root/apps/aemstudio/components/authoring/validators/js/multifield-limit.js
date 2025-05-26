(function (Granite, $, document) {
  'use strict';

  // Common validation function, pass the multified to get the params
  function validate(el) {

    var elem = $(el),
      value = elem.data('validation'),
      items = elem.find('> coral-multifield-item'),
      minItems = parseInt(value.match(/min:([0-9]*)/)[1]) || 0,
      maxItems = parseInt(value.match(/max:([0-9]*)/)[1]) || 9999,
      msg,
      button = elem.find('button:last');

    if (items.length >= maxItems) {
      button.hide();
    } else {
      button.show();
    }

    if (!(items.length >= minItems && items.length <= maxItems)) {
      elem.addClass('is-invalid');
      msg = Granite.I18n.get('The number of items must be from {0} to {1}', [minItems, maxItems]);
    } else {
      elem.removeClass('is-invalid');
    }
    return msg;

  }

  // On Dialog load, validate only if there's a multifield with this validation in place
  $(document).on('foundation-contentloaded', function (e) {
    console.log("Dialog load");
    var dialog = $(e.target),
      multifields = dialog.find('coral-multifield');
    // loop over the multifields
    $(multifields).each(function (index) {
      var validation = $(this).data('validation');
      //validate only if applicable
      if (validation && validation.startsWith("multifield-limit")) {
        validate(this);
      }
    });
  });

  // Validation on button click
  $(window).adaptTo('foundation-registry').register('foundation.validation.validator', {
    selector: '[data-validation|="multifield-limit"],[data-validation*=",multifield-limit"]',
    validate: function (el) {
      return validate(el);
    }
  });

}(Granite, jQuery, document));
