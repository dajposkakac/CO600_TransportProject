package transportapp.co600.directionserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private static InputStreamReader inputStreamReader;
    private static BufferedReader bufferedReader;
    private PrintWriter printwriter;
    private static String data;
    
    private DirectionsResult result;
    
    public Server()	{
    	try {
    	    serverSocket = new ServerSocket(4444); //Server socket
    	    System.out.println("Host IP: " + InetAddress.getLocalHost().getHostAddress());
	    } catch (IOException e) {
    	    System.out.println("Could not listen on port: 4444");
	    }
    	System.out.println("Server started. Listening to the port 4444");
    	
    	while(true)	{
    		try	{
    			clientSocket = serverSocket.accept(); //accept the client connection
    		    inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
    		    bufferedReader = new BufferedReader(inputStreamReader); //get the client message
    		    String data = bufferedReader.readLine();
    		    String[] newData = data.split("!.!");
    		    
    		    DirectionsRequest request = new DirectionsRequest(newData[0], newData[1]);
    		    result = new DirectionsResult(request.getRoutes());
    		    
    		    printwriter = new PrintWriter(clientSocket.getOutputStream(), true);
    		    String resultString = result.getOrigin() + " -> " + result.getDestination() + "\n" + result.getDistance() + ", " + result.getDuration();
    		    System.out.println(resultString);
    		    printwriter.write(resultString);
    		    
    		    printwriter.flush();
    		    printwriter.close();
    		    
    		    inputStreamReader.close();
    		    clientSocket.close();
    		} catch(IOException e)	{
    			System.out.println("Problem in message reading");
    		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
}
