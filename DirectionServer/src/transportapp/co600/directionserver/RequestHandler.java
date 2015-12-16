package transportapp.co600.directionserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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
//		    Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(bufferedReader.readLine())));
//		    HashMap<String, String> data = parseToMap(xmlDoc);
//		    DirectionsRequest request = new DirectionsRequest(data.get("origin"), data.get("destination"), data.get("transitMode"));
//		    DirectionsResult result = new DirectionsResult(request.getRoutes());
//		    
//		    String resultString = result.getOrigin() + " -> " + result.getDestination() + ", " + result.getDistance() + ", " + result.getDuration() + "\n";
		    String resultString = "test";
		    printWriter = new PrintWriter(socket.getOutputStream(), true);
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
	
	private HashMap<String,String> parseToMap(Document doc)	{
		NodeList nodes = doc.getFirstChild().getChildNodes();
		HashMap<String, String> map = new HashMap<>();
		for(int i = 0; i < nodes.getLength(); i++)  {
            Node n = nodes.item(i);
            String nodeName = n.getNodeName();
        	map.put(nodeName, n.getTextContent());
        }
		return map;
	}
	
//	private String createXMLResult(DirectionsResult res) throws ParserConfigurationException, IOException, SAXException, TransformerException {
//        Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(activity.getResources().openRawResource(R.raw.request_template));
//        NodeList nodes = xmlDoc.getFirstChild().getChildNodes();
//        for(int i = 0; i < nodes.getLength(); i++)  {
//            Node n = nodes.item(i);
//            String nn = n.getNodeName();
//            if(nn.equals("origin"))    {
//                n.setTextContent(req.getOrigin());
//            }   else if(nn.equals("destination"))   {
//                n.setTextContent(req.getDestination());
//            }   else if(nn.equals("transitMode"))  {
//                n.setTextContent(req.getTransitMode());
//            }
//        }
//        Transformer transformer = TransformerFactory.newInstance().newTransformer();
//        StreamResult sr = new StreamResult(new StringWriter());
//        DOMSource source = new DOMSource(xmlDoc);
//        transformer.transform(source, sr);
//        return sr.getWriter().toString() + "\n";
//    }
}
