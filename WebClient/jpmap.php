<?php
	header("Content-Type: text/javascript; charset=utf-8");
	$host = '192.168.1.2';
	$port = 4444;
	//$fp = fsockopen($host, $port, $errno, $errstr, 30);
	// create socket
	$socket = socket_create(AF_INET, SOCK_STREAM, 0) or die("Could not create socket\n");
	// connect to server
	if(($result = socket_connect($socket, $host, $port)) === FALSE)
		echo "socket_connect() error.\n" . socket_strerror(socket_last_error($socket));
	else	{
		$start = $_POST['start'];
		$end = $_POST['end'];
		$mode = $_POST['mode'];
		$message = "<?xml version = '1.0' encoding = 'UTF-8'?><request><origin>" . $start . "</origin><destination>" . $end . "</destination><transitMode>" . $mode . "</transitMode></request>" . PHP_EOL;
		// send string to server
		socket_write($socket, $message, strlen($message)) or die("Could not send data to server\n");
		// get server response
		$result = socket_read ($socket, 1024) or die("Could not read server response\n");
		echo "Reply From Server  :".$result ."<br>";
	}
	
	// close socket
	//socket_close($socket);
?>