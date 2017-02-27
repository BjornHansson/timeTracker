$(document).ready(function() {
    'use strict'; // Prevents certain (unsafe) actions from being taken and throws more exceptions.
    const DATE_FORMAT = 'Y-m-d';
    const START_STOP_BTN = '#startTimeTracking';
    const HISTORY_BTN = '#fetchHistory';
    const DATE_START = '#dateStart';
    const DATE_END = '#dateEnd';
    const HISTORY_DIV = '#history';

    // Check if server is tracking time or not when page is ready
    var trackingTime;
    $.get('/time', function(data, status){
        trackingTime = data.trackingTime;
        toggleBtn(trackingTime);
    });

    // Setup the date pickers
    $(DATE_START).datetimepicker({
        format: DATE_FORMAT
    });
    $(DATE_END).datetimepicker({
        format: DATE_FORMAT
    });

    // When start/stop tracking time button is clicked
    $(START_STOP_BTN).click(function() {
        trackingTime = !trackingTime;
        $.post('/time', JSON.stringify({
            'trackingTime': trackingTime
        }),
        function(data, status) {
            toggleBtn(data.trackingTime);
        });
    });

    // When fetching history button is clicked
    $(HISTORY_BTN).click(function() {
        var start = $(DATE_START).val();
        var end = $(DATE_END).val();
        if(start != '' && end != '') {
            $.get('/time/' + start + '/' + end, function(data, status){
                console.log(data);
                $(HISTORY_DIV).html('');
                var arrayLength = data.length;
                for(var i = 0; i < arrayLength; i++) {
                    $(HISTORY_DIV).append('<p>' + data[i].date + ' &#124 ' + data[i].time + '</p>');
                }
            });
        }
        return false;
    });

    // Toggle color of button
    function toggleBtn(tracking) {
        if(tracking === true) {
            $(START_STOP_BTN).removeClass('btn-success');
            $(START_STOP_BTN).addClass('btn-danger');
        }
        else {
            $(START_STOP_BTN).removeClass('btn-danger');
            $(START_STOP_BTN).addClass('btn-success');
        }
    }
});
