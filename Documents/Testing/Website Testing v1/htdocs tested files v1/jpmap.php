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
<?php
		$host = '192.168.1.2';
		$port = 4444;
	// create socket
		$socket = socket_create(AF_INET, SOCK_STREAM, 0) or die("Could not create socket\n");
	// connect to server
		$result = socket_connect($socket, $host, $port) or die("Could not connect to server\n"); 		
		$start = $_POST['start'];
		$end = $_POST['end'];
		if (empty($_POST['mode']))
		{
			$mode = "unknown"; 
		}
		else{
			$mode = $_POST['mode'];
		}
		$date = $_POST['date'];
		$time = $_POST['time'];
		$ad = $_POST['deparr'];
		$message = "<?xml version = '1.0' encoding = 'UTF-8'?><request><origin>" . $start . "</origin><destination>" . $end . "</destination><transitMode>" . $mode . "</transitMode><time>" .$time. ":00</time><date>" .$date. "</date><departureOption>" .$ad. "</departureOption></request>" . PHP_EOL;
	// send string to server
		if($result) { 
		socket_write($socket, $message, strlen($message)) or die("Could not send data to server\n");
	// get server response
		$result = socket_read ($socket, 10000) or die("Could not read server response\n");
		}
		$xml = simplexml_load_string($result);
		$destination = $xml->info->destination;
		$origin = $xml->info->origin;
?>
<script language="javascript" type="text/javascript">
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
<?php 
		echo "You can travel from " . $origin . " to" . PHP_EOL. "\n";
		echo $destination . " by:". PHP_EOL ."\n";
        $id = 0;
	foreach($xml->results->result as $rs) {
			
		echo "<textarea class=\"textarea\"> \n";
		echo "Travel Mode:". $rs->transitMode . PHP_EOL . "\n";
		echo "Time of the Journey:". $rs->duration .  PHP_EOL . "\n";
		echo "Distance:". $rs->distance . PHP_EOL . "\n";
		echo "Price:". $rs->price . PHP_EOL . "\n";
		if($rs->transitMode == "TRANSIT"){
		echo "Departure Time:" . $rs->departureTime . PHP_EOL . "\n";
		echo "Arrival Time:" . $rs->arrivalTime . PHP_EOL . "\n";
		}
		echo "</textarea>";
		}
?>
<input class="button" id="btntest"  type="button" value="Back" onclick="return btntest_onclick()" />
</form>
</div>

<div id="footer">
</div>

<div id="footer">
</div>
</body>
</html>