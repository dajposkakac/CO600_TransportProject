package transportapp.co600.directionserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
		    Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(bufferedReader.readLine())));
		    HashMap<String, String> data = parseToMap(xmlDoc);
		    DirectionsRequest request = new DirectionsRequest(data.get("origin"), data.get("destination"), data.get("transitMode"));
		    Document r2rXmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(request.getR2RData())));
		    DirectionsResults result = new DirectionsResults(request.getRoutes(), request.getTravelMode(), parseR2RXml(r2rXmlDoc));
		    String resultString = createXMLResponse(result);
//		    String resultString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><request><origin>London, UK</origin><destination>Oxford, Oxford, UK</destination><distance>85.3 km</distance><duration>1 hour 35 mins</duration><transitMode>transit</transitMode><price>33</price></request>";
		    printWriter = new PrintWriter(socket.getOutputStream(), true);
		    System.out.println(resultString);
		    printWriter.write(resultString);
		    printWriter.flush();
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
	
	private String parseR2RXml(Document doc)	{
		NodeList nodes = doc.getFirstChild().getChildNodes();
		for(int i = 0; i < nodes.getLength(); i++)	{
			Node n = nodes.item(i);
			String nn = n.getNodeName();
			if(nn.equals("Route"))	{
				if(n.getAttributes().getNamedItem("name").getTextContent().equals("Train") || n.getAttributes().getNamedItem("name").getTextContent().equals("Drive"))	{
					return n.getFirstChild().getAttributes().getNamedItem("price").getTextContent();
				}
			}
		}
		return "-1";
	}
	
	private String createXMLResponse(DirectionsResults res) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("templates/response_template.xml"));
        NodeList nodes = xmlDoc.getFirstChild().getChildNodes();
        for(int i = 0; i < nodes.getLength(); i++)  {
            Node n = nodes.item(i);
            String nn = n.getNodeName();
            if(nn.equals("origin"))    {
                n.setTextContent(res.getOrigin());
            }   else if(nn.equals("destination"))   {
                n.setTextContent(res.getDestination());
            }   else if(nn.equals("transitMode"))  {
                n.setTextContent(res.getTransitMode());
            }	else if(nn.equals("distance"))	{
            	n.setTextContent(res.getDistance());
            }	else if(nn.equals("duration"))	{
            	n.setTextContent(res.getDuration());
            }	else if(nn.equals("price"))	{
            	n.setTextContent(res.getPrice());
            }
        }
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StreamResult sr = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(xmlDoc);
        transformer.transform(source, sr);
        return sr.getWriter().toString() + "\n";
    }
}
