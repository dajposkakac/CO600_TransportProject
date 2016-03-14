package transportapp.co600.directionserver;

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

public class DirectionsRequest {

	//Data constants
	public static final String ORIGIN = "origin";
	public static final String DESTINATION = "destination";
	public static final String TRANSIT_MODE = "transitMode";
	public static final String DEPARTURE_OPTION = "departureOption";
	public static final String TIME = "time";
	public static final String DATE = "date";
	
	//Misc
	private static final String LATLNG_REGEXP = "([+-]?\\d+\\.?\\d+)\\s*,\\s*([+-]?\\d+\\.?\\d+)";
	
	//R2R URL constants
	private static final String R2RURL1 = "http://free.rome2rio.com/api/1.2/xml/Search?key=";
	private static final String R2RURL2 = "&oPos=";
	private static final String R2RURL3 = "&dPos=";
	private static final String R2RURL4 = "&currencyCode=";
	private static final String R2RURL5 = "&flags=";
	
	private final GeoApiContext gaContext;
	private DirectionsResult routes;
	private String r2rData;
	private HashMap<String, String> additionalData;
	private int status;
	private HashMap<String, String> request;
	
	public DirectionsRequest(HashMap<String, String> data)	{
		status = 0;
		request = data;
		additionalData = new HashMap<>();
		gaContext = new GeoApiContext().setApiKey(readKey("google_key_server"));
		gatherAdditionalData();
		makeRequests();
	}
	
	private void makeRequests()	{
		String origin = request.get(ORIGIN);
		String destination = request.get(DESTINATION);
		LatLng originLatLng = null;
		LatLng destinationLatLng = null;
		String departureOption = request.get(DEPARTURE_OPTION);
		TravelMode travelMode = TravelMode.valueOf(additionalData.get(TRANSIT_MODE));
		if(origin.matches(LATLNG_REGEXP))	{
			String[] originTemp = origin.split(",");
			originLatLng = new LatLng(Double.valueOf(originTemp[0]), Double.valueOf(originTemp[1]));
		}	else	{
			try {
				originLatLng = geocodeAddress(origin);
			} catch (NotFoundException e) {
				status = 1;
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(destination.matches(LATLNG_REGEXP))	{
			String[] destinationTemp = destination.split(",");
			destinationLatLng = new LatLng(Double.valueOf(destinationTemp[0]), Double.valueOf(destinationTemp[1]));
		}	else	{
			try {
				destinationLatLng = geocodeAddress(destination);
			} catch (NotFoundException e) {
				status = 1;
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			DateTime time = extractDateTime(request.get(TIME), request.get(DATE));
			if(departureOption.startsWith("Arrive"))	{
				routes = DirectionsApi.newRequest(gaContext).origin(originLatLng).destination(destinationLatLng).arrivalTime(time).mode(travelMode).alternatives(true).await();
			}	else	{
				routes = DirectionsApi.newRequest(gaContext).origin(originLatLng).destination(destinationLatLng).departureTime(time).mode(travelMode).alternatives(true).await();
			}
			r2rData = r2rSearch(originLatLng, destinationLatLng, travelMode);
		}	catch (DateInPastException dipe)	{
			status = 2;
			dipe.printStackTrace();
			Thread.currentThread().interrupt();
			return;
		}	catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void gatherAdditionalData()	{
		additionalData.put(TRANSIT_MODE, request.get(TRANSIT_MODE).toUpperCase());
		additionalData.put(DEPARTURE_OPTION, request.get(DEPARTURE_OPTION));
	}
	
	private String r2rSearch(LatLng originLatLng, LatLng destinationLatLng, TravelMode travelMode) throws IOException {
		String sUrl = R2RURL1 + readKey("R2R_key") + R2RURL2 + originLatLng.toString() + R2RURL3 + destinationLatLng.toString() + R2RURL4 + "GBP" + R2RURL5 + getTravelModeFlags(travelMode);
		System.out.println(sUrl);
		URL url = new URL(sUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET"); 
//		int respCode = con.getResponseCode();
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

	public DirectionsResult getRoutes() {
		return routes;
	}
	
	private DateTime extractDateTime(String time, String date) throws DateInPastException	{
		DateTime dt = new DateTime();
		if(time != null && !time.equals("now"))	{
			int[] timeData = Arrays.stream(time.split(":")).mapToInt(Integer::parseInt).toArray();
			dt = dt.withHourOfDay(timeData[0]).withMinuteOfHour(timeData[1]);
		}
		if(time != null && !date.equals("now"))	{
			int[] dateData = Arrays.stream(date.split("/")).mapToInt(Integer::parseInt).toArray();
			dt = dt.withYear(dateData[2]).withMonthOfYear(dateData[1]).withDayOfMonth(dateData[0]);
		}
		dt = dt.plusMillis(250);
		if(!dt.isAfterNow())	{
			throw new DateInPastException(dt.toString() + " is in the past");
		}
		return dt;
	}
	
	private LatLng geocodeAddress(String address) throws NotFoundException, Exception	{
		GeocodingResult[] results =  GeocodingApi.newRequest(gaContext).address(address).await();
		if(results.length < 1)	{
			status = 1;
			throw new NotFoundException(address);
		}
		return results[0].geometry.location;
	}
	
	private String getTravelModeFlags(TravelMode tm)	{
		String flags = "0x018FFFFF";
		if(tm == TravelMode.DRIVING)	{
			flags = "0x0180FFFF";
		}	else if(tm == TravelMode.TRANSIT)	{
			flags = "0x018FF22F";
		}
		return flags;
	}
	
	public String getR2RData()	{
		return r2rData;
	}

	public int getStatus() {
		return status;
	}
	
	public HashMap<String, String> getAdditionalData()	{
		return additionalData;
	}
	
	private String readKey(String keyName)  {
        Document xmlDoc;
		try {
			xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("./templates/supersecretsecret.xml"));
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
