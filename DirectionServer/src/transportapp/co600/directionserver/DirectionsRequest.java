package transportapp.co600.directionserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.DirectionsResult;
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
	
	public DirectionsRequest(String origin, String destination, String transitMode) throws Exception	{
		gaContext = new GeoApiContext().setApiKey("AIzaSyD_pZcQHhzIbFjmVkO88oQ8DDaMm-jF3q4");
		travelMode = TravelMode.valueOf(transitMode.toUpperCase());
		routes = DirectionsApi.getDirections(gaContext, origin, destination).mode(travelMode).await();
		r2rData = r2rSearch(getOriginLatLng(origin), getOriginLatLng(destination));
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
	
	private LatLng getOriginLatLng(String origin) throws Exception	{
		GeocodingResult[] results = GeocodingApi.geocode(gaContext, origin).await();
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
	
}
