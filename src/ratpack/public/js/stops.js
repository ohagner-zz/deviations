$(document).ready(function() {
    $("#getstopsButton").click(function() {
            $.ajax({
                dataType: "json",
                url: "http://localhost:8080/api2/typeahead.json",
                data: "key=hej",
                success: function(data) {

                    var stations = []
                    for(index in data.ResponseData) {
                        stations[index] = data.ResponseData[index].Name
                    }
                    alert(stations);
                }
            });
        });
});