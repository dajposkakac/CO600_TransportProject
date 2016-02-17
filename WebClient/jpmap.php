<?php
		header("Content-Type: text/javascript; charset=utf-8");
		$host = '86.183.210.224';
		$port = 4444;
	// create socket
		$socket = socket_create(AF_INET, SOCK_STREAM, 0) or die("Could not create socket\n");
	// connect to server
		$result = socket_connect($socket, $host, $port) or die("Could not connect to server\n"); 		
		$start = $_POST['start'];
		$end = $_POST['end'];
		$mode = $_POST['mode'];
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
