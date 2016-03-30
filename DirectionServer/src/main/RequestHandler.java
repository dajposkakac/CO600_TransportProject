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

/*
 * Thread used to handle requests.
 * Reads XML from the socket, parses the information into maps
 * and creates a new DirectionsRequest for every travel mode 
 * specified in the request. This is followed by creating a 
 * DirectionsResult using DirectionsRequests output, which is then
 * converted to XML format and sent back to the client.
 * 
 * @author jg404
 */
public class RequestHandler extends Thread {
	
	public static final String XML_SCHEMA_PATH = "./request_schema.xsd";
	
	//XML tags
	public static final String RESPONSE = "response";
	public static final String STATUS = "status";
	public static final String ERROR_MESSAGE = "errorMessage";
	public static final String INFO = "info";
	public static final String RESULTS = "results";
	public static final String RESULT = "result";
	
	//Errors
	public static final int STATUS_SUCCESS = 0;
	public static final int STATUS_INVALID_REQUEST = -10;
	public static final int STATUS_LOCATION_NOT_FOUND = 1;
	public static final int STATUS_TIME_DATE_IS_IN_THE_PAST = 2;
	public static final int STATUS_ROUTE_NOT_FOUND = 3;
	public static final String STATUS_INVALID_REQUEST_MESSAGE = "Invalid request";
	public static final String STATUS_LOCATION_NOT_FOUND_MESSAGE = "Location not found:";
	public static final String STATUS_TIME_DATE_IS_IN_THE_PAST_MESSAGE = " is in the past";
	public static final String STATUS_ROUTE_NOT_FOUND_MESSAGE = "No route found: \n";
	
	private Socket socket;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	
	/*
	 * Standard constructor, initialises socket.
	 */
	public RequestHandler(final Socket pSocket)	{
		socket = pSocket;
	}
	
	public void run()	{
		try	{
		    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())); //get the client message
		    final String xmlString = bufferedReader.readLine();
		    System.out.println(xmlString + "\n");
		    DirectionsResults result = null;
		    if(validateRequest(xmlString))	{
		    	final Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xmlString)));
		    	final HashMap<String, String> data = parseToMap(xmlDoc);
		    	final String[] transitModes = data.get(DirectionsRequest.TRANSIT_MODE).split(",");
			    for(int i = 0; i < transitModes.length; i++)	{
			    	data.put(DirectionsRequest.TRANSIT_MODE, transitModes[i]);
			    	final DirectionsRequest request = new DirectionsRequest(data);
				    
				    if(request.getStatus() == 0)	{
				    	if(result == null)	{
				    		result = new DirectionsResults(request.getStatus(), request.getAdditionalData());
				    	}
				    	final Document r2rXmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(request.getR2RData())));
			    		result.addResults(request.getRoutes(), parseR2RXml(r2rXmlDoc));
				    }	else	{
				    	result = new DirectionsResults(request.getStatus(), request.getErrorMessage());
				    	break;
				    }
			    }
		    }	else	{
		    	result = new DirectionsResults(STATUS_INVALID_REQUEST, STATUS_INVALID_REQUEST_MESSAGE);
		    }
		    final String resultString = createXMLResponse(result);
		    printWriter = new PrintWriter(socket.getOutputStream(), true);
		    System.out.println(resultString + "\n\n");
		    printWriter.write(resultString); 
		    printWriter.flush(); //send response
		}	catch(final Exception e)	{
			e.printStackTrace();
		}	finally {
			printWriter.close();
			try	{
				bufferedReader.close();
				socket.close();
			}	catch(Exception e)	{}
		}
	}
	
	/*
	 * Takes an XML document and parses children of the first node
	 * into a HashMap<String, String>.
	 */
	private HashMap<String,String> parseToMap(final Document doc)	{
		final NodeList nodes = doc.getFirstChild().getChildNodes();
		final HashMap<String, String> map = new HashMap<>();
		for(int i = 0; i < nodes.getLength(); i++)  {
			final Node n = nodes.item(i);
			final String nodeName = n.getNodeName();
        	map.put(nodeName, n.getTextContent());
        }
		return map;
	}
	
	/*
	 * Takes an XML document and parses prices for trains, buses and
	 * driving into a HashMap<String, Integer>
	 */
	private HashMap<String, Integer> parseR2RXml(final Document doc)	{
		final HashMap<String, Integer> priceMap = new HashMap<>();
		final NodeList nodes = doc.getFirstChild().getChildNodes();
		for(int i = 0; i < nodes.getLength(); i++)	{
			final Node n = nodes.item(i);
			String nn = n.getNodeName();
			if(nn.equals("Route"))	{
				final String travelModeName = n.getAttributes().getNamedItem("name").getTextContent();
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
	
	/*
	 * Checks whether the request was correctly formatted.
	 */
	private boolean validateRequest(final String xmlString) {
		final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema;
		try {
			schema = factory.newSchema(new File(XML_SCHEMA_PATH));
			final Validator validator = schema.newValidator();
			validator.validate(new StreamSource(new StringReader(xmlString)));
		} catch (final SAXException | IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/*
	 * Creates a String containing the response to the request, in format
	 * specified in the template files in the templates folder.
	 */
	private String createXMLResponse(final DirectionsResults res) throws ParserConfigurationException, IOException, TransformerException, SAXException {
		final Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		
		//root
		final Element response = xmlDoc.createElement(RESPONSE);
		xmlDoc.appendChild(response);
	
		//status
		final Element status = xmlDoc.createElement(STATUS);
		status.appendChild(xmlDoc.createTextNode(String.valueOf(res.getStatus())));
		response.appendChild(status);
		
		if(res.getStatus() == 0)	{
			//info
			final Element info = xmlDoc.createElement(INFO);
			response.appendChild(info);
			final Element origin = xmlDoc.createElement(DirectionsRequest.ORIGIN);
			final Element destination = xmlDoc.createElement(DirectionsRequest.DESTINATION);
			final Element originLatLng = xmlDoc.createElement(DirectionsRequest.ORIGIN_LATLNG);
			final Element destinationLatLng = xmlDoc.createElement(DirectionsRequest.DESTINATION_LATLNG);
			final Element originDisplay = xmlDoc.createElement(DirectionsRequest.ORIGIN_DISPLAY);
			final Element destinationDisplay = xmlDoc.createElement(DirectionsRequest.DESTINATION_DISPLAY);
			final Element sortingPreference = xmlDoc.createElement(DirectionsRequest.SORTING_PREFERENCE);
			final Element departureOption = xmlDoc.createElement(DirectionsRequest.DEPARTURE_OPTION);
			origin.appendChild(xmlDoc.createTextNode(res.getOriginForRoute(0)));
			destination.appendChild(xmlDoc.createTextNode(res.getDestinationForRoute(0)));
			originLatLng.appendChild(xmlDoc.createTextNode(res.getOriginLatLng()));
			destinationLatLng.appendChild(xmlDoc.createTextNode(res.getDestinationLatLng()));
			originDisplay.appendChild(xmlDoc.createTextNode(res.getOriginDisplayName()));
			destinationDisplay.appendChild(xmlDoc.createTextNode(res.getDestinationDisplayName()));
			sortingPreference.appendChild(xmlDoc.createTextNode(res.getSortingPreference()));
    		departureOption.appendChild(xmlDoc.createTextNode(res.getDepartureOption()));
			info.appendChild(origin);
			info.appendChild(destination);
			info.appendChild(originLatLng);
			info.appendChild(destinationLatLng);
			info.appendChild(originDisplay);
			info.appendChild(destinationDisplay);
			info.appendChild(sortingPreference);
			info.appendChild(departureOption);
			
			//results
			final Element results = xmlDoc.createElement(RESULTS);
			response.appendChild(results);
			for(int k = 0; k < res.getNumberOfRoutes(); k++)	{
				final Element result = xmlDoc.createElement(RESULT);
				final Element transitMode = xmlDoc.createElement(DirectionsRequest.TRANSIT_MODE);
	    		transitMode.appendChild(xmlDoc.createTextNode(res.getTransitModeForRoute(k).toUpperCase()));
	    		result.appendChild(transitMode);
	    		final Element distance = xmlDoc.createElement(DirectionsRequest.DISTANCE);
	    		distance.appendChild(xmlDoc.createTextNode(res.getDistanceForRoute(k)));
	    		result.appendChild(distance);
	    		final Element duration = xmlDoc.createElement(DirectionsRequest.DURATION);
	    		duration.appendChild(xmlDoc.createTextNode(res.getDurationForRoute(k)));
	    		result.appendChild(duration);
	    		final Element price = xmlDoc.createElement(DirectionsRequest.PRICE);
	    		price.appendChild(xmlDoc.createTextNode(res.getPriceForRoute(k)));
	    		result.appendChild(price);
	    		final Element arrivalTime = xmlDoc.createElement(DirectionsRequest.ARRIVAL_TIME);
	    		arrivalTime.appendChild(xmlDoc.createTextNode(res.getArrivalTimeForRoute(k)));
	    		result.appendChild(arrivalTime);
	    		final Element departureTime = xmlDoc.createElement(DirectionsRequest.DEPARTURE_TIME);
	    		departureTime.appendChild(xmlDoc.createTextNode(res.getDepartureTimeForRoute(k)));
	    		result.appendChild(departureTime);
	    		final Element arrivalDate = xmlDoc.createElement(DirectionsRequest.ARRIVAL_DATE);
	    		arrivalDate.appendChild(xmlDoc.createTextNode(res.getArrivalDateForRoute(k)));
	    		result.appendChild(arrivalDate);
	    		final Element departureDate = xmlDoc.createElement(DirectionsRequest.DEPARTURE_DATE);
	    		departureDate.appendChild(xmlDoc.createTextNode(res.getDepartureDateForRoute(k)));
	    		result.appendChild(departureDate);
	    		final Element arrivalTimeInSeconds = xmlDoc.createElement(DirectionsRequest.ARRIVAL_TIME_IN_SECONDS);
	    		arrivalTimeInSeconds.appendChild(xmlDoc.createTextNode(res.getArrivalTimeInSecondsForRoute(k)));
	    		result.appendChild(arrivalTimeInSeconds);
	    		final Element departureTimeInSeconds = xmlDoc.createElement(DirectionsRequest.DEPARTURE_TIME_IN_SECONDS);
	    		departureTimeInSeconds.appendChild(xmlDoc.createTextNode(res.getDepartureTimeInSecondsForRoute(k)));
	    		result.appendChild(departureTimeInSeconds);
	    		final Element polyline = xmlDoc.createElement(DirectionsRequest.POLYLINE);
	    		polyline.appendChild(xmlDoc.createTextNode(res.getPolylineForRoute(k)));
	    		result.appendChild(polyline);
	    		
	    		results.appendChild(result);
			}
		}	else	{
			final Element errorMessage = xmlDoc.createElement(ERROR_MESSAGE);
			errorMessage.appendChild(xmlDoc.createTextNode(res.getErrorMessage()));
			response.appendChild(errorMessage);
		}
		final Transformer transformer = TransformerFactory.newInstance().newTransformer();
		final StreamResult sr = new StreamResult(new StringWriter());
		final DOMSource source = new DOMSource(xmlDoc);
        transformer.transform(source, sr);
        return sr.getWriter().toString().trim().replaceAll("[\n\t\r]+", "") + "\n";
    }
}
