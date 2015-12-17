package transportapp.co600.directionserver;

import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

public class DirectionsResult {
	
	private String destination;
	private String distance;
	private String duration;
	private String origin;
	private String transitMode;
	private String price;
	
	public DirectionsResult(DirectionsRoute[] routes, TravelMode travelMode, String pPrice)	{
		setDestination(routes[0].legs[0].endAddress);
		setDistance(routes[0].legs[0].distance.humanReadable);
		setDuration(routes[0].legs[0].duration.humanReadable);
		setOrigin(routes[0].legs[0].startAddress);
		setTransitMode(travelMode.toString());
		setPrice(pPrice);
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
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
