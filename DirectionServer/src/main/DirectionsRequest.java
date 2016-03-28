package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.crypto.Data;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.NotFoundException;
import com.google.maps.errors.ZeroResultsException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

/*
 * Handles making requests to external APIs, collecting the
 * data into HashMaps. Also handles exceptions and sets appropriate
 * fields so errors can be reported back to the client. Defines response
 * XML tag constants and some other ones for convenience.
 * 
 * @author jg404
 */
public class DirectionsRequest {

	//Data constants
	public static final String ORIGIN = "origin";
	public static final String DESTINATION = "destination";
	public static final String ORIGIN_LATLNG= "originLatLng";
	public static final String DESTINATION_LATLNG = "destinationLatLng";
	public static final String DISTANCE = "distance";
	public static final String DURATION = "duration";
	public static final String PRICE = "price";
	public static final String ARRIVAL_TIME = "arrivalTime";
	public static final String DEPARTURE_TIME = "departureTime";
	public static final String ARRIVAL_DATE = "arrivalDate";
	public static final String DEPARTURE_DATE = "departureDate";
	public static final String TRANSIT_MODE = "transitMode";
	public static final String DEPARTURE_OPTION = "departureOption";
	public static final String TIME = "time";
	public static final String DATE = "date";
	public static final String POLYLINE = "polyline";
	public static final String ARRIVAL_TIME_IN_SECONDS = "arrivalTimeInSeconds";
	public static final String DEPARTURE_TIME_IN_SECONDS = "departureTimeInSeconds";
	public static final String SORTING_PREFERENCE = "sortingPreference";
	public static final String ORIGIN_DISPLAY = "originDisplay";
	public static final String DESTINATION_DISPLAY = "destinationDisplay";
	
	//Misc
	public static final String COMMA = ",";
	public static final String SPACE = " ";
	public static final String COMMASPACE = COMMA + SPACE; 
	private static final String LATLNG_REGEXP = "([+-]?\\d+\\.?\\d+)\\s*,\\s*([+-]?\\d+\\.?\\d+)";
	
	//R2R constants
	private static final String R2RURL1 = "http://free.rome2rio.com/api/1.2/xml/Search?key=";
	private static final String R2RURL2 = "&oPos=";
	private static final String R2RURL3 = "&dPos=";
	private static final String R2RURL4 = "&currencyCode=";
	private static final String R2RURL5 = "&flags=";
	private static final String R2R_TRAVEL_MODE_FLAGS_DEFAULT = "0x018FFFFF";
	private static final String R2R_TRAVEL_MODE_FLAGS_DRIVING = "0x0180FFFF";
	private static final String R2R_TRAVEL_MODE_FLAGS_TRANSIT = "0x018FF22F";
	private static final String CONN_TYPE = "GET";
	
	private final GeoApiContext gaContext;
	private DirectionsResult routes;
	private String r2rData;
	private HashMap<String, String> additionalData;
	private int status;
	private String errorMessage;
	private HashMap<String, String> request;
	private LatLng originLatLng = null;
	private LatLng destinationLatLng = null;
	private TravelMode travelMode;
	private DateTime time;
	
	/*
	 * Constructor sets reads API key for GoogleAPI and initialises
	 * object.
	 */
	public DirectionsRequest(HashMap<String, String> data)	{
		status = 0;
		request = data;
		additionalData = new HashMap<>();
		gaContext = new GeoApiContext().setApiKey(readKey("google_key_server"));
		makeRequests();
		gatherAdditionalData();
	}
	
	/*
	 * Converts origin and destination to coordinates if necessary, makes a Google Maps
	 * Directions API request and if a route exists makes a Rome2Rio Search API requests
	 * to look for a price for all routes. Handles all exceptions, which set error status,
	 * a readable error message and stops the request.
	 */
	private void makeRequests()	{
		String origin = request.get(ORIGIN);
		String destination = request.get(DESTINATION);
		String departureOption = request.get(DEPARTURE_OPTION);
		travelMode = TravelMode.valueOf(request.get(TRANSIT_MODE).toUpperCase());
		if(origin.matches(LATLNG_REGEXP))	{
			String[] originTemp = origin.split(COMMA);
			originLatLng = new LatLng(Double.valueOf(originTemp[0]), Double.valueOf(originTemp[1]));
		}	else	{
			try {
				originLatLng = geocodeAddress(origin);
			} catch (LocationNotFoundException e) {
				status = e.getStatus();
				errorMessage = e.getMessage();
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		if(destination.matches(LATLNG_REGEXP))	{
			String[] destinationTemp = destination.split(COMMA);
			destinationLatLng = new LatLng(Double.valueOf(destinationTemp[0]), Double.valueOf(destinationTemp[1]));
		}	else	{
			try {
				destinationLatLng = geocodeAddress(destination);
			} catch (LocationNotFoundException e) {
				status = e.getStatus();
				errorMessage = e.getMessage();
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		try {
			time = extractDateTime(request.get(TIME), request.get(DATE));
			if(departureOption.startsWith("Arrive"))	{
				routes = DirectionsApi.newRequest(gaContext).origin(originLatLng).destination(destinationLatLng).arrivalTime(time).mode(travelMode).alternatives(true).await();
			}	else	{
				routes = DirectionsApi.newRequest(gaContext).origin(originLatLng).destination(destinationLatLng).departureTime(time).mode(travelMode).alternatives(true).await();
			}
			routeExists(routes);
			r2rData = r2rSearch(originLatLng, destinationLatLng, travelMode);
		}	catch (DateInPastException dipe)	{
			status = dipe.getStatus();
			errorMessage = dipe.getMessage();
			return;
		}	catch (RouteNotFoundException rnfe)	{
			status = rnfe.getStatus();
			errorMessage = rnfe.getMessage();
			return;
		}	catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	/*
	 * Convenient method which specifies a single place in code to add some
	 * additional information to the request.
	 */
	private void gatherAdditionalData()	{
		if(status == 0)	{
			additionalData.put(TRANSIT_MODE, request.get(TRANSIT_MODE).toUpperCase());
			additionalData.put(DEPARTURE_OPTION, request.get(DEPARTURE_OPTION));
			additionalData.put(ORIGIN_LATLNG, originLatLng.lat + COMMA + originLatLng.lng);
			additionalData.put(DESTINATION_LATLNG, destinationLatLng.lat + COMMA + destinationLatLng.lng);
			additionalData.put(TIME, String.valueOf(time.getMillis() / 1000));
			additionalData.put(SORTING_PREFERENCE, request.get(SORTING_PREFERENCE));
			additionalData.put(ORIGIN_DISPLAY, request.get(ORIGIN));
			additionalData.put(DESTINATION_DISPLAY, request.get(DESTINATION));
		}
	}
	
	/*
	 * Makes the Rome2Rio Search API request using origin and destination coordinates 
	 * and the specified travel mode.
	 */
	private String r2rSearch(LatLng originLatLng, LatLng destinationLatLng, TravelMode travelMode) throws IOException {
		String sUrl = R2RURL1 + readKey("R2R_key") + R2RURL2 + originLatLng.toString() + R2RURL3 + destinationLatLng.toString() + R2RURL4 + "GBP" + R2RURL5 + getTravelModeFlags(travelMode);
		System.out.println(sUrl);
		URL url = new URL(sUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod(CONN_TYPE); 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		System.out.println(response.toString());
		return response.toString();
	}

	/*
	 * Returns routes found by Google Maps Directions API request.
	 */
	public DirectionsResult getRoutes() {
		return routes;
	}
	
	/*
	 * Checks whether any routes were found, if not, throws RouteNotFoundException.
	 */
	private void routeExists(DirectionsResult routes) throws RouteNotFoundException {
		if(routes.routes.length == 0)	{
			throw new RouteNotFoundException(3, "No route found");
		}
	}
	
	/*
	 * Uses time and date information from the client request to create an object which 
	 * is easier to operate with.
	 */
	public DateTime extractDateTime(String time, String date) throws DateInPastException	{
		DateTime dt = new DateTime();
		if(time != null)	{
			int[] timeData = Arrays.stream(time.split(":")).mapToInt(Integer::parseInt).toArray();
			dt = dt.withHourOfDay(timeData[0]).withMinuteOfHour(timeData[1]);
		}
		if(date != null)	{
			int[] dateData = Arrays.stream(date.split("-")).mapToInt(Integer::parseInt).toArray();
			dt = dt.withYear(dateData[0]).withMonthOfYear(dateData[1]).withDayOfMonth(dateData[2]);
		}
		dt = dt.plusMillis(250);
		if(!dt.isAfterNow())	{
			throw new DateInPastException(2, dt.toString() + " is in the past");
//			dt = DateTime.now().plusMillis(250);
		}
		return dt;
	}
	
	/*
	 * Takes an address and returns its LatLng. If the location is not found throws 
	 * LocationNotFoundException.
	 */
	private LatLng geocodeAddress(String address) throws NotFoundException, Exception	{
		GeocodingResult[] results =  GeocodingApi.newRequest(gaContext).address(address).await();
		if(results.length < 1)	{
			throw new LocationNotFoundException(1, "Location not found:" + address);
		}
		return results[0].geometry.location;
	}
	
	/*
	 * Returns Rome2Rio travel mode flags for the specified travel mode.
	 */
	private String getTravelModeFlags(TravelMode tm)	{
		String flags = R2R_TRAVEL_MODE_FLAGS_DEFAULT;
		if(tm == TravelMode.DRIVING)	{
			flags = R2R_TRAVEL_MODE_FLAGS_DRIVING;
		}	else if(tm == TravelMode.TRANSIT)	{
			flags = R2R_TRAVEL_MODE_FLAGS_TRANSIT;
		}
		return flags;
	}
	
	/*
	 * Returns output of Rome2Rio Search API request.
	 */
	public String getR2RData()	{
		return r2rData;
	}

	/*
	 * Returns status of the request.
	 */
	public int getStatus() {
		return status;
	}
	
	/*
	 * Returns a readable eror message if an error occurs.
	 */
	public String getErrorMessage()	{
		return errorMessage;
	}
	
	/*
	 * Returns the map of additional data to be included in the response.
	 */
	public HashMap<String, String> getAdditionalData()	{
		return additionalData;
	}
	
	/*
	 * Reads the key for the key type specified.
	 */
	private String readKey(String keyName)  {
        Document xmlDoc;
		try {
			xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("./supersecretsecret.xml"));
			NodeList nodes = xmlDoc.getFirstChild().getChildNodes();
	        for(int i = 0; i < nodes.getLength(); i++)  {
	            Node node = nodes.item(i);
	            if(node.getNodeName().equals(keyName))    {
	                return node.getTextContent();
	            }
	        }
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
        return null;
    }
}
