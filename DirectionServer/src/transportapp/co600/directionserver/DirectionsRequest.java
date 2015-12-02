package transportapp.co600.directionserver;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

public class DirectionsRequest {
	
	private DirectionsRoute[] routes;
	
	public DirectionsRequest(String origin, String destination, String transitMode) throws Exception	{
		GeoApiContext gaContext = new GeoApiContext().setApiKey("AIzaSyD_pZcQHhzIbFjmVkO88oQ8DDaMm-jF3q4");
		
		routes = DirectionsApi.getDirections(gaContext, origin, destination).mode(chooseTravelMode(transitMode)).await();
//		printer(routes);
	}
	
	public DirectionsRoute[] getRoutes() {
		return routes;
	}
	
	private TravelMode chooseTravelMode(String sTM)	{
		TravelMode tm = TravelMode.UNKNOWN;
		if(sTM.equals("Car"))	{
			tm = TravelMode.DRIVING;
		}	else if(sTM.equals("Public"))	{
			tm = TravelMode.TRANSIT;
		}	else if(sTM.equals("Walking"))	{
			tm = TravelMode.WALKING;
		}	else if(sTM.equals("Bicycle"))	{
			tm = TravelMode.BICYCLING;
		}
		return tm;
	}

	public void printer(DirectionsRoute[] routes) {
		System.out.println(routes[0].legs[0].distance.humanReadable);
	}
}
