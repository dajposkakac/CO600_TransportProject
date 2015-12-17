package transportapp.co600.directionserver;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

public class DirectionsRequest {
	
	private DirectionsRoute[] routes;
	private TravelMode travelMode;
	
	public DirectionsRequest(String origin, String destination, String transitMode) throws Exception	{
		GeoApiContext gaContext = new GeoApiContext().setApiKey("AIzaSyD_pZcQHhzIbFjmVkO88oQ8DDaMm-jF3q4");
		travelMode = TravelMode.valueOf(transitMode.toUpperCase());
		routes = DirectionsApi.getDirections(gaContext, origin, destination).mode(travelMode).await();
	}
	
	public DirectionsRoute[] getRoutes() {
		return routes;
	}
	
	public TravelMode getTravelMode() {
		return travelMode;
	}
	
}
