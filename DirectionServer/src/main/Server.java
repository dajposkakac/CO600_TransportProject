package main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class Server {
	private static ServerSocket serverSocket;
    
    public Server()	{
    	try {
    	    serverSocket = new ServerSocket(4444); //Server socket
    	    System.out.println("Host IP: " + InetAddress.getLocalHost().getHostAddress());
    	    System.out.println("Server started. Listening to the port 4444");
    	    try	{
    			while(true)	{
    				new RequestHandler(serverSocket.accept()).start();//accept the client connection
    			}
    		} catch(IOException e)	{
    			System.out.println("Problem in message reading");
    		} catch (Exception e) {
    			e.printStackTrace();
    		}	finally	{
    			try {
    				serverSocket.close();
    			} catch (IOException e) {}
    		}
	    } catch (IOException e) {
    	    System.out.println("Could not listen on port: 4444");
	    }
    }
    
    
}
