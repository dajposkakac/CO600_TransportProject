function tog(v){return v?'addClass':'removeClass';} 
$(document).on('input', '.textbox', function(){
    $(this)[tog(this.value)]('x');
}).on('mousemove', '.x', function( e ){
    $(this)[tog(this.offsetWidth-18 < e.clientX-this.getBoundingClientRect().left)]('onX');
}).on('touchstart click', '.onX', function( ev ){
    ev.preventDefault();
    $(this).removeClass('x onX').val('').change();
});
var expanded = "false";
function displayopt()
{
	if(expanded == "false")
	{
		document.getElementById("advopt").value = "Hide travel options";
		document.getElementById("h1").style.fontSize = "38pt";
		document.getElementById("form").style.width = "70em";
		document.getElementById("basic").style.width = "26em";
		$('#advanced').toggle();
		document.getElementById("submit1").style.display = "none";
		document.getElementById("advopt").style.marginTop = "253px";
		expanded = "true";
	}
	else
	{
		document.getElementById("advopt").value = "Show travel options";
		document.getElementById("h1").style.fontSize = "25pt";
		document.getElementById("form").style.width = "25em";
		document.getElementById("basic").style.width = "100%";
		$('#advanced').toggle();
		document.getElementById("submit1").style.display = "initial";
		expanded = "false";
	}
}
window.addEvent('domready', function () {
    $$('input').set({
        events: {
            change: function (el) {
                $$('label').removeClass('selected');
                this.getParent('label').addClass('selected');
            }
        }
    });
});
	var map; //Will contain map object.
	var marker = false; ////Has the user plotted their location marker? 
    var autocomplete;
    function initialize() {
    autocomplete = new google.maps.places.Autocomplete(
        /** @type {HTMLInputElement} */(document.getElementById('start')),
        { types: ['geocode'] });
        google.maps.event.addListener(start, 'place_changed', function() {
        });
		var autocomplet;
		autocomplet = new google.maps.places.Autocomplete(
        /** @type {HTMLInputElement} */(document.getElementById('end')),
        { types: ['geocode'] });
        google.maps.event.addListener(end, 'place_changed', function() {
        });
	}