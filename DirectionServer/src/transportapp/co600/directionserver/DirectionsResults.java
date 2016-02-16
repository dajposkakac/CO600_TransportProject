package transportapp.co600.directionserver;

import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

public class DirectionsResults {
	
	private final DirectionsResult result;
	private TravelMode mode;
	private String transitMode;
	private String price;
	
	
	public DirectionsResults(DirectionsResult directionsResult, TravelMode travelMode, String pPrice)	{
		result = directionsResult;
		setTransitMode(travelMode.toString());
		setPrice(pPrice);
	}
	
	public String getDestinationForRoute(int route)	{
		return result.routes[route].legs[0].endAddress;
	}
	
	public String getOriginForRoute(int route)	{
		return result.routes[route].legs[0].startAddress;
	}
	
	public String getDistanceForRoute(int route)	{
		return result.routes[route].legs[0].distance.humanReadable;
	}
	
	public String getDurationForRoute(int route)	{
		return result.routes[route].legs[0].duration.humanReadable;
	}
	
	public String getDurationForRoute()	{
		return mode.toString();
	}
	
	public int getNumberOfRoutes()	{
		return result.routes.length;
	}

	public String getTransitMode() {
		return transitMode;
	}

	public void setTransitMode(String transitMode) {
		this.transitMode = transitMode;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}
	
}
