package transportapp.co600.directionserver;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsRoute;

public class DirectionsRequest {
	
	private DirectionsRoute[] routes;
	
	public DirectionsRequest(String origin, String destination) throws Exception	{
		GeoApiContext gaContext = new GeoApiContext().setApiKey("AIzaSyD_pZcQHhzIbFjmVkO88oQ8DDaMm-jF3q4");
		
		routes = DirectionsApi.getDirections(gaContext, origin, destination).await();
//		printer(routes);
	}
	
	public DirectionsRoute[] getRoutes() {
		return routes;
	}

	public void printer(DirectionsRoute[] routes) {
		System.out.println(routes[0].legs[0].distance.humanReadable);
	}
}
