package transportapp.co600.directionserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.crypto.Data;

import org.joda.time.DateTime;

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
	private static final String R2RKEY = "ZMjs2oRB";
	private static final String R2RURL1 = "http://free.rome2rio.com/api/1.2/xml/Search?key=";
	private static final String R2RURL2 = "&oPos=";
	private static final String R2RURL3 = "&dPos=";
	private static final String R2RURL4 = "&currencyCode=";
	private static final String R2RURL5 = "&flags=";
	private final GeoApiContext gaContext;
	private DirectionsResult routes;
	private TravelMode travelMode;
	private String r2rData;
	private int status;
	private HashMap<String, String> request;
	
	public DirectionsRequest(HashMap<String, String> data)	{
		status = 0;
		request = data;
		gaContext = new GeoApiContext().setApiKey("AIzaSyD_pZcQHhzIbFjmVkO88oQ8DDaMm-jF3q4");
		travelMode = TravelMode.valueOf(request.get("transitMode").toUpperCase());
		makeRequests();
	}
	
	private void makeRequests()	{
		String origin = request.get("origin");
		String destination = request.get("destination");
		LatLng originLatLng = null;
		LatLng destinationLatLng = null;
		String departureOption = request.get("departureOption");
		if(origin.matches("([+-]?\\d+\\.?\\d+)\\s*,\\s*([+-]?\\d+\\.?\\d+)"))	{
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
		if(destination.matches("([+-]?\\d+\\.?\\d+)\\s*,\\s*([+-]?\\d+\\.?\\d+)"))	{
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
			DateTime time = extractDateTime(request.get("time"), request.get("date"));
			if(departureOption.startsWith("Arrive"))	{
				routes = DirectionsApi.newRequest(gaContext).origin(originLatLng).destination(destinationLatLng).arrivalTime(time).mode(travelMode).alternatives(true).await();
			}	else	{
				routes = DirectionsApi.newRequest(gaContext).origin(originLatLng).destination(destinationLatLng).departureTime(time).mode(travelMode).alternatives(true).await();
			}
			r2rData = r2rSearch(originLatLng, destinationLatLng);
		}	catch (DateInPastException dipe)	{
			status = 2;
			dipe.printStackTrace();
			Thread.currentThread().interrupt();
			return;
		}	catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String r2rSearch(LatLng originLatLng, LatLng destinationLatLng) throws IOException {
		String sUrl = R2RURL1 + R2RKEY + R2RURL2 + originLatLng.toString() + R2RURL3 + destinationLatLng.toString() + R2RURL4 + "GBP" + R2RURL5 + getTravelModeFlags(travelMode);
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
	
	public TravelMode getTravelMode() {
		return travelMode;
	}
	
	private DateTime extractDateTime(String time, String date) throws DateInPastException	{
		DateTime dt = new DateTime();
		if(!time.equals("now"))	{
			int[] timeData = Arrays.stream(time.split(":")).mapToInt(Integer::parseInt).toArray();
			dt = dt.withHourOfDay(timeData[0]).withMinuteOfHour(timeData[1]);
		}
		if(!date.equals("now"))	{
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
	
}
