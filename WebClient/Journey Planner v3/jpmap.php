<!doctype html>
<?php
if (empty($_POST['start']) || empty($_POST['end']))
{
  header('Location: Home.html');
  exit; 
} ?>
<html>
<head>
<meta charset="utf-8">
<title>Journey Organizer</title>
<link href="CSS/Cascade.css" rel="stylesheet" type="text/css">
<style> .mapp {width:60%; height:600px; } </style>
<?php
		$host = '52.37.113.133';
		$port = 4444;
	// create socket
		$socket = socket_create(AF_INET, SOCK_STREAM, 0) or die("Could not create socket\n");
	// connect to server
		$result = socket_connect($socket, $host, $port) or die("Could not connect to server\n");
		$start = $_POST['start'];
		$end = $_POST['end'];
		$mode ="";
		$mode1="";
		$mode2="";
		$mode3="";
		$mode4="";
		$mode5="";
		if (empty($_POST['mode1'])&& empty($_POST['mode2'])&& empty($_POST['mode3'])&& empty($_POST['mode4'])&& empty($_POST['mode5']))
		{
			$mode = "unknown"; 
		}
		if (!empty($_POST['mode1']))
		{
			if($_POST['mode1'] == "driving") $mode = $mode . "driving". ","; 
		}
		if (!empty($_POST['mode2']) || !empty(!$_POST['mode2']))
		{
			if($_POST['mode2'] == "transit") $mode = $mode . "transit". ","; 
		}
		if (!empty($_POST['mode4']))
		{
			if($_POST['mode4'] == "walking") $mode = $mode . "walking". ",";
		}
		if (!empty($_POST['mode5']))
		{
			if($_POST['mode5'] == "bicycling") $mode = $mode . "bicycling" . ",";
		}
		$mode = rtrim($mode, ",");
		$date = $_POST['date'];
		$time = $_POST['time'];
		if(empty($_POST['deparr'])){$ad = "Depart at";}
		else {$ad = $_POST['deparr'];}
		$message = "<?xml version = '1.0' encoding = 'UTF-8'?><request><origin>" . $start . "</origin><destination>" . $end . "</destination><transitMode>" . $mode . "</transitMode><time>" .$time. ":59</time><date>" .$date. "</date><departureOption>" .$ad. "</departureOption><sortingPreference>Distance</sortingPreference></request>" . PHP_EOL;
	// send string to server
		if($result) { 
		socket_write($socket, $message, strlen($message)) or die("Could not send data to server\n");
	// get server response
		$result = socket_read ($socket, 999999) or die("Could not read server response\n");
		}
		$xml = simplexml_load_string($result);
		
	   
		if($xml->status == "-10") header('Location: error-10.html');
		if($xml->status == "-2") header('Location: error-2.html');
		if($xml->status == "-1") header('Location: error-1.html');
		if($xml->status == "1") header('Location: error1.html');
		if($xml->status == "2") header('Location: error2.html');
		if($xml->status == "3") header('Location: error3.html');
		$destination = $xml->info->destinationDisplay;
		$origin = $xml->info->originDisplay;
?>
<script src="https://maps.googleapis.com/maps/api/js?sensor=false"></script>
<script src="https://maps.googleapis.com/maps/api/js?libraries=geometry"></script>
<script language="javascript" type="text/javascript">
function drawMap(poly, id){
	var mapDiv = document.getElementById(id);
	var mapOptions = {
		center: new google.maps.LatLng (51.49662437, 0.144870312),
		zoom:4,
		mapTypeId: google.maps.MapTypeId.ROADMAP
		};
		var map = new google.maps.Map(mapDiv, mapOptions);
		var points = google.maps.geometry.encoding.decodePath(poly);
		var polylineOptions = {path: points};
		var polyline = new google.maps.Polyline( polylineOptions);
		polyline.setMap(map);
}
function btntest_onclick() 
{
    window.location.href = "Home.html";
}
</script>
</head>
<body>
<div id="header">
<a href="Home.html"><img src="images/logo.png" id="logo"></a>
<nav>
<a class="menu" href="Home.html" style="text-decoration:underline; background:rgba(255,255,255,0.5);">Home</a>
<a class="menu" href="About.html">About us</a>
<a class="menu" href="Contact.html">Contact us</a>
</nav>
</div>

<div id="container">
<form id="form">
<fieldset class="tabs">
<div class="radio-toolbar tb2">
<input type="radio" id="radio1" name="route" value="distance" checked>
<label id="left" for="radio1" onclick="blyat()">Distance</label>
<input type="radio" id="radio2" name="route" value="time">
<label id="middle" for="radio2" onclick="blyat()">Time</label>
<input type="radio" id="radio3" name="route" value="cost">
<label id="right" for="radio3" onclick="blyat()">Cost</label>
</div>
</fieldset>

<?php 
$idd = 0;
	foreach($xml->results->result as $rs) {
$idreal = "map" . $idd;
echo"<fieldset class=\"result\" onclick=\"show('fs".$idd."')\">" . PHP_EOL . "\n";
echo "<table class=\"result\"> \n";
echo "<tr> \n";
echo "<td> \n";
if($rs->transitMode == "BUS"){
	echo "<img src=\"images/bus.png\" alt=\"\" class=\"resulticon\"> \n";
	}
else if($rs->transitMode == "TRAIN"){
	echo "<img src=\"images/train.png\" alt=\"\" class=\"resulticon\"> \n";
	}
else if($rs->transitMode == "WALKING"){
	echo "<img src=\"images/walk.png\" alt=\"\" class=\"resulticon\"> \n";
	}
else if($rs->transitMode == "DRIVING"){
	echo "<img src=\"images/car.png\" alt=\"\" class=\"resulticon\"> \n";
	}
else{
	echo "<img src=\"images/cycle.png\" alt=\"\" class=\"resulticon\"> \n";
	}
echo "</td>\n";
echo "<td>\n";
echo "<h6 class=\"one\">Departure: ".$rs->departureTime."</h6>" . PHP_EOL . "\n";
echo "<h6>Arrival:". $rs->arrivalTime ."</h6>" . PHP_EOL . "\n";
echo "</td>\n";
echo "<td>\n";
echo "<h6 class=\"two ext\">Duration: ". $rs->duration ."</h6>" . PHP_EOL . "\n";
echo "<h6 class=\"two ext\">Distance: ". $rs->distance ."</h6>" . PHP_EOL . "\n";
if($rs->price <0) $pprice = "not available";
else $pprice = "£". $rs->price;
echo "<h6 class=\"ext\">Price: ". $pprice . "</h6>" . PHP_EOL . "\n";
echo "</td>\n";
echo "</tr>\n";
echo "</table>\n";
echo "<table>\n";
echo "<tr>\n";
echo "<td id=\"fs".$idd."\" style=\"display:none;\">\n";
echo "<h6 class=\"two exth\">From: ". $origin ."</h6>". PHP_EOL . "\n";
echo "<h6 class=\"exth\">To: ". $destination ."</h6>". PHP_EOL . "\n";
echo "</td>\n";
echo "</tr>\n";
echo "</table>\n";
echo "</fieldset>\n";
$idd = $idd+1;
}
?>
<input class="button" id="btntest"  type="button" value="Back" onclick="return btntest_onclick()" />
</form>
</div>
<div id="footer">
</div>

<script type="text/javascript">
function show(idd)
{
	if(document.getElementById(idd).style.display == "none")
	{
		document.getElementById(idd).style.display = "initial";
	}
	else
	{
		document.getElementById(idd).style.display = "none";
	}
}
</script>
</body>
</html>
