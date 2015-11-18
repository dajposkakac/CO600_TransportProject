package transportapp.co600.directionserver;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsRoute;

public class Main {
	
	public Main() throws Exception	{
		GeoApiContext gaContext = new GeoApiContext().setApiKey("AIzaSyD_pZcQHhzIbFjmVkO88oQ8DDaMm-jF3q4");
		DirectionsRoute[] routes = DirectionsApi.getDirections(gaContext, "Sydney, AU", "Melbourne, AU").await();
		printer(routes);
	}
	
	public void printer(DirectionsRoute[] routes) {
		System.out.println(routes[0].legs[0].distance.humanReadable);
	}
	
	public static void main(String[] args) {
		try {
			new Main();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
