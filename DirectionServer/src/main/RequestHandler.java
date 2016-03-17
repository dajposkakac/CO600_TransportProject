package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.util.HashMap;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RequestHandler extends Thread {
	
	public static final String XML_SCHEMA_PATH = "./templates/request_schema.xsd";
	
	private Socket socket;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	
	public RequestHandler(Socket pSocket)	{
		socket = pSocket;
	}
	
	public void run()	{
		try	{
		    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())); //get the client message
		    String xmlString = bufferedReader.readLine();
		    System.out.println(xmlString);
		    DirectionsResults result = null;
		    if(validateRequest(new StreamSource(new StringReader(xmlString))))	{
		    	Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xmlString)));
			    HashMap<String, String> data = parseToMap(xmlDoc);
			    DirectionsRequest request = new DirectionsRequest(data);
			    
			    if(request.getStatus() == 0)	{
			    	Document r2rXmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(request.getR2RData())));
			    	result = new DirectionsResults(request.getStatus(), request.getRoutes(), request.getAdditionalData(), parseR2RXml(r2rXmlDoc));
			    }	else	{
			    	result = new DirectionsResults(request.getStatus());
			    }
		    }	else	{
		    	result = new DirectionsResults(-10);
		    }
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
	
	private boolean validateRequest(StreamSource streamSource) {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema;
		try {
			schema = factory.newSchema(new File(XML_SCHEMA_PATH));
			Validator validator = schema.newValidator();
			validator.validate(streamSource);
		} catch (SAXException | IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private String createXMLResponse(DirectionsResults res) throws ParserConfigurationException, IOException, TransformerException, SAXException {
		Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		
		//root
		Element response = xmlDoc.createElement("response");
		xmlDoc.appendChild(response);
	
		//status
		Element status = xmlDoc.createElement("status");
		status.appendChild(xmlDoc.createTextNode(String.valueOf(res.getStatus())));
		response.appendChild(status);
		
		if(res.getStatus() == 0)	{
			//info
			Element info = xmlDoc.createElement("info");
			response.appendChild(info);
			Element origin = xmlDoc.createElement("origin");
			Element destination = xmlDoc.createElement("destination");
			origin.appendChild(xmlDoc.createTextNode(res.getOriginForRoute(0)));
			destination.appendChild(xmlDoc.createTextNode(res.getDestinationForRoute(0)));
			info.appendChild(origin);
			info.appendChild(destination);
			
			//results
			Element results = xmlDoc.createElement("results");
			response.appendChild(results);
			for(int k = 0; k < res.getNumberOfRoutes(); k++)	{
	    		Element result = xmlDoc.createElement("result");
	    		Element transitMode = xmlDoc.createElement(DirectionsRequest.TRANSIT_MODE);
	    		transitMode.appendChild(xmlDoc.createTextNode(res.getTransitMode()));
	    		result.appendChild(transitMode);
	    		Element distance = xmlDoc.createElement("distance");
	    		distance.appendChild(xmlDoc.createTextNode(res.getDistanceForRoute(k)));
	    		result.appendChild(distance);
	    		Element duration = xmlDoc.createElement("duration");
	    		duration.appendChild(xmlDoc.createTextNode(res.getDurationForRoute(k)));
	    		result.appendChild(duration);
	    		Element price = xmlDoc.createElement("price");
	    		price.appendChild(xmlDoc.createTextNode(res.getPrice()));
	    		result.appendChild(price);
	    		if(res.getTransitMode().equals("TRANSIT"))	{
		    		Element arrivalTime = xmlDoc.createElement("arrivalTime");
		    		arrivalTime.appendChild(xmlDoc.createTextNode(res.getArrivalTimeForRoute(k)));
		    		result.appendChild(arrivalTime);
		    		Element departureTime = xmlDoc.createElement("departureTime");
		    		departureTime.appendChild(xmlDoc.createTextNode(res.getDepartureTimeForRoute(k)));
		    		result.appendChild(departureTime);
		    		Element date = xmlDoc.createElement("date");
		    		date.appendChild(xmlDoc.createTextNode(res.getArrivalDateForRoute(k)));
		    		result.appendChild(date);
	    		}
	    		Element departureOption = xmlDoc.createElement(DirectionsRequest.DEPARTURE_OPTION);
	    		departureOption.appendChild(xmlDoc.createTextNode(res.getDepartureOption()));
	    		result.appendChild(departureOption);
	    		results.appendChild(result);
			}
		}
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StreamResult sr = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(xmlDoc);
        transformer.transform(source, sr);
        return sr.getWriter().toString().trim().replaceAll("[\n\t\r]+", "") + "\n";
    }
}