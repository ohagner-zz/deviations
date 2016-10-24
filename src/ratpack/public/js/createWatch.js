$(document).ready(function() {

    var transportIndex = 0;

    $('#watchForm')

    // Add button click handler
        .on('click', '.addButton', function() {
            transportIndex++;
            var $template = $('#transportTemplate'),
                $clone = $template
                    .clone()
                    .removeClass('hide')
                    .removeAttr('id')
                    .insertBefore($template);

            // Update the name attributes
            $clone
                .find('[id="transportMode"]')
                    .attr('name', 'transport[' + transportIndex + '].transportMode')
                    .attr('required', '')
                    .removeAttr('id')
                    .end()
                .find('[id="line"]')
                    .attr('name', 'transport[' + transportIndex + '].line')
                    .attr('required', '')
                    .removeAttr('id')
                    .end();
        })

        // Remove button click handler
        .on('click', '.removeButton', function() {
            var $row = $(this).parents('.form-group'),
                index = $row.attr('data-book-index');
            // Remove element containing the fields
            $row.remove();
        });
});
