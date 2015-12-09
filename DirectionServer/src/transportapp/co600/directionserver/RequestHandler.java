package transportapp.co600.directionserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RequestHandler extends Thread {
	private Socket socket;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	
	public RequestHandler(Socket pSocket)	{
		socket = pSocket;
	}
	
	public void run()	{
		try	{
		    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())); //get the client message
		    String data = bufferedReader.readLine();
		    String[] newData = data.split("!.!");
		    DirectionsRequest request = new DirectionsRequest(newData[0], newData[1], newData[2]);
		    DirectionsResult result = new DirectionsResult(request.getRoutes());
		    
		    printWriter = new PrintWriter(socket.getOutputStream(), true);
		    String resultString = result.getOrigin() + " -> " + result.getDestination() + "\n" + result.getDistance() + ", " + result.getDuration();
		    System.out.println(resultString);
		    printWriter.write(resultString);
		    printWriter.flush();
//		    printWriter.close();
		}	catch(Exception e)	{
			e.printStackTrace();
		}	finally {
			printWriter.close();
			try	{
				bufferedReader.close();
				socket.close();
			}	catch(Exception e)	{}
		}
	}
}
