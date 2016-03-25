package main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/*
 * Server will open a ServerSocket on port 4444, listen
 * for an incoming connection and use a separate thread
 * to handle the request, so it can go back to listening
 * as soon as possible.
 * 
 * @author jg404
 */
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
