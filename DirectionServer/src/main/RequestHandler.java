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
	
	public static final String XML_SCHEMA_PATH = "./request_schema.xsd";
	
	public static final String RESPONSE = "response";
	public static final String STATUS = "status";
	public static final String ERROR_MESSAGE = "errorMessage";
	public static final String INFO = "info";
	public static final String RESULTS = "results";
	public static final String RESULT = "result";
	
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
			    String[] transitModes = data.get(DirectionsRequest.TRANSIT_MODE).split(",");
			    for(int i = 0; i < transitModes.length; i++)	{
			    	data.put(DirectionsRequest.TRANSIT_MODE, transitModes[i]);
			    	DirectionsRequest request = new DirectionsRequest(data);
				    
				    if(request.getStatus() == 0)	{
				    	if(result == null)	{
				    		result = new DirectionsResults(request.getStatus(), request.getAdditionalData());
				    	}
				    	Document r2rXmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(request.getR2RData())));
			    		result.addResults(request.getRoutes(), parseR2RXml(r2rXmlDoc));
				    }	else	{
				    	result = new DirectionsResults(request.getStatus(), request.getErrorMessage());
				    	break;
				    }
			    }
		    }	else	{
		    	result = new DirectionsResults(-10, "Invalid request");
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
	
	private HashMap<String, Integer> parseR2RXml(Document doc)	{
		HashMap<String, Integer> priceMap = new HashMap<>();
		NodeList nodes = doc.getFirstChild().getChildNodes();
		for(int i = 0; i < nodes.getLength(); i++)	{
			Node n = nodes.item(i);
			String nn = n.getNodeName();
			if(nn.equals("Route"))	{
				String travelModeName = n.getAttributes().getNamedItem("name").getTextContent();
				if(travelModeName.contains(DirectionsResults.TRAIN)) {
					priceMap.put(DirectionsResults.TRAIN, Integer.valueOf(n.getFirstChild().getAttributes().getNamedItem("price").getTextContent()));
				}	else if(travelModeName.contains(DirectionsResults.BUS))	{ 
					priceMap.put(DirectionsResults.BUS, Integer.valueOf(n.getFirstChild().getAttributes().getNamedItem("price").getTextContent()));
				}	else if(travelModeName.contains(DirectionsResults.DRIVE))	{
					priceMap.put(DirectionsResults.DRIVE, Integer.valueOf(n.getFirstChild().getAttributes().getNamedItem("price").getTextContent()));
				}
			}
		}
		return priceMap;
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
		Element response = xmlDoc.createElement(RESPONSE);
		xmlDoc.appendChild(response);
	
		//status
		Element status = xmlDoc.createElement(STATUS);
		status.appendChild(xmlDoc.createTextNode(String.valueOf(res.getStatus())));
		response.appendChild(status);
		
		if(res.getStatus() == 0)	{
			//info
			Element info = xmlDoc.createElement(INFO);
			response.appendChild(info);
			Element origin = xmlDoc.createElement(DirectionsRequest.ORIGIN);
			Element destination = xmlDoc.createElement(DirectionsRequest.DESTINATION);
			Element originLatLng = xmlDoc.createElement(DirectionsRequest.ORIGIN_LATLNG);
			Element destinationLatLng = xmlDoc.createElement(DirectionsRequest.DESTINATION_LATLNG);
			Element sortingPreference = xmlDoc.createElement(DirectionsRequest.SORTING_PREFERENCE);
			origin.appendChild(xmlDoc.createTextNode(res.getOriginForRoute(0)));
			destination.appendChild(xmlDoc.createTextNode(res.getDestinationForRoute(0)));
			originLatLng.appendChild(xmlDoc.createTextNode(res.getOriginLatLng()));
			destinationLatLng.appendChild(xmlDoc.createTextNode(res.getDestinationLatLng()));
			sortingPreference.appendChild(xmlDoc.createTextNode(res.getSortingPreference()));
			info.appendChild(origin);
			info.appendChild(destination);
			info.appendChild(originLatLng);
			info.appendChild(destinationLatLng);
			info.appendChild(sortingPreference);
			
			//results
			Element results = xmlDoc.createElement(RESULTS);
			response.appendChild(results);
			for(int k = 0; k < res.getNumberOfRoutes(); k++)	{
				String travelMode = res.getTransitModeForRoute(k).toUpperCase();
				
	    		Element result = xmlDoc.createElement(RESULT);
	    		Element transitMode = xmlDoc.createElement(DirectionsRequest.TRANSIT_MODE);
	    		transitMode.appendChild(xmlDoc.createTextNode(travelMode));
	    		result.appendChild(transitMode);
	    		Element distance = xmlDoc.createElement(DirectionsRequest.DISTANCE);
	    		distance.appendChild(xmlDoc.createTextNode(res.getDistanceForRoute(k)));
	    		result.appendChild(distance);
	    		Element duration = xmlDoc.createElement(DirectionsRequest.DURATION);
	    		duration.appendChild(xmlDoc.createTextNode(res.getDurationForRoute(k)));
	    		result.appendChild(duration);
	    		Element price = xmlDoc.createElement(DirectionsRequest.PRICE);
	    		price.appendChild(xmlDoc.createTextNode(res.getPriceForRoute(k)));
	    		result.appendChild(price);
	    		Element arrivalTime = xmlDoc.createElement(DirectionsRequest.ARRIVAL_TIME);
	    		arrivalTime.appendChild(xmlDoc.createTextNode(res.getArrivalTimeForRoute(k)));
	    		result.appendChild(arrivalTime);
	    		Element departureTime = xmlDoc.createElement(DirectionsRequest.DEPARTURE_TIME);
	    		departureTime.appendChild(xmlDoc.createTextNode(res.getDepartureTimeForRoute(k)));
	    		result.appendChild(departureTime);
	    		Element arrivalDate = xmlDoc.createElement(DirectionsRequest.ARRIVAL_DATE);
	    		arrivalDate.appendChild(xmlDoc.createTextNode(res.getArrivalDateForRoute(k)));
	    		result.appendChild(arrivalDate);
	    		Element departureDate = xmlDoc.createElement(DirectionsRequest.DEPARTURE_DATE);
	    		departureDate.appendChild(xmlDoc.createTextNode(res.getDepartureDateForRoute(k)));
	    		result.appendChild(departureDate);
	    		Element arrivalTimeInSeconds = xmlDoc.createElement(DirectionsRequest.ARRIVAL_TIME_IN_SECONDS);
	    		arrivalTimeInSeconds.appendChild(xmlDoc.createTextNode(res.getArrivalTimeInSecondsForRoute(k)));
	    		result.appendChild(arrivalTimeInSeconds);
	    		Element departureTimeInSeconds = xmlDoc.createElement(DirectionsRequest.DEPARTURE_TIME_IN_SECONDS);
	    		departureTimeInSeconds.appendChild(xmlDoc.createTextNode(res.getDepartureTimeInSecondsForRoute(k)));
	    		result.appendChild(departureTimeInSeconds);
	    		Element departureOption = xmlDoc.createElement(DirectionsRequest.DEPARTURE_OPTION);
	    		departureOption.appendChild(xmlDoc.createTextNode(res.getDepartureOption()));
	    		result.appendChild(departureOption);
	    		Element polyline = xmlDoc.createElement(DirectionsRequest.POLYLINE);
	    		polyline.appendChild(xmlDoc.createTextNode(res.getPolylineForRoute(k)));
	    		result.appendChild(polyline);
	    		
	    		results.appendChild(result);
			}
		}	else	{
			Element errorMessage = xmlDoc.createElement(ERROR_MESSAGE);
			errorMessage.appendChild(xmlDoc.createTextNode(res.getErrorMessage()));
			response.appendChild(errorMessage);
		}
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StreamResult sr = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(xmlDoc);
        transformer.transform(source, sr);
        return sr.getWriter().toString().trim().replaceAll("[\n\t\r]+", "") + "\n";
    }
}
