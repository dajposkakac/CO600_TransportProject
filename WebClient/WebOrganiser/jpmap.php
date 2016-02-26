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
		$host = '5.81.182.39';
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
		$message = "<?xml version = '1.0' encoding = 'UTF-8'?><request><origin>" . $start . "</origin><destination>" . $end . "</destination><transitMode>" . $mode . "</transitMode></request>" . PHP_EOL;
	// send string to server
		if($result) { 
		socket_write($socket, $message, strlen($message)) or die("Could not send data to server\n");
	// get server response
		$result = socket_read ($socket, 1024) or die("Could not read server response\n");
		}
		$xml = simplexml_load_string($result);
		$destination = $xml->info->destination;
		$origin = $xml->info->origin;
		
		
		
		
		echo "You can travel from " . $origin . " to" . PHP_EOL. "\n";
		echo $destination . " by:". PHP_EOL ."\n";
		foreach($xml->results->result as $rs) {
		echo $rs->transitMode ." ". $rs->distance ." over ". $rs->duration ." at the price of £". $rs->price . PHP_EOL ."\n";
		}
?>
<script language="javascript" type="text/javascript">
function btntest_onclick() 
{
    window.location.href = "localhost/Home.html";
}
</script>

<script type="text/javascript">
function display()
{
	document.getElementById("btn1").value = "kupa"
}
</script>
</head>
<body>
<div id="header">
<img src="images/logo.png" id="logo">
</div>

<div id="container">
<form id="form">
<input type="button" class="button" id="btn1" value="value" onclick="display()">

<input class="button" id="btntest"  type="button" value="Back" onclick="return btntest_onclick()" />
</form>
</div>


<div id="footer">
</div>
</body>
</html>