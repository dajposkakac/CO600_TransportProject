package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;

import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

public class DirectionsResults {
	
	private final DirectionsResult result;
	private HashMap<String, String> additionalData;
	private int status;
	private String price;
	
	
	public DirectionsResults(int status, DirectionsResult directionsResult, HashMap<String, String> pAdditionalData, String pPrice)	{
		setStatus(status);
		result = directionsResult;
		additionalData = pAdditionalData;
		setPrice(pPrice);
	}
	
	public DirectionsResults(int status)	{
		result = null;
		setStatus(status);
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
	
	public String getArrivalTimeForRoute(int route)	{
		return getTimeReadable(result.routes[route].legs[0].arrivalTime);
	}
	
	public String getDepartureTimeForRoute(int route)	{
		return getTimeReadable(result.routes[route].legs[0].departureTime);
	}
	
	public String getArrivalDateForRoute(int route)	{
		return getDateReadable(result.routes[route].legs[0].arrivalTime);
	}
	
	public String getDepartureDateForRoute(int route)	{
		return getDateReadable(result.routes[route].legs[0].departureTime);
	}
	
	public String getPolylineForRoute(int route)	{
		List<LatLng> polyline = new ArrayList<>();
		DirectionsStep[] stepsList = result.routes[route].legs[0].steps;
		StringBuilder polylineBuilder = new StringBuilder();
		for(int i = 0; i < stepsList.length; i++)	{
			List<LatLng> path = stepsList[i].polyline.decodePath();
			for(LatLng pos : path)	{
				polylineBuilder.append(pos.lat);
				polylineBuilder.append(",");
				polylineBuilder.append(pos.lng);
				polylineBuilder.append("|");
			}
		}
		polylineBuilder.deleteCharAt(polylineBuilder.length() - 1);
		return polylineBuilder.toString().trim();
	}
	
//	public String getPolylineForRoute(int route)	{
//		DirectionsStep[] stepsList = result.routes[route].legs[0].steps;
//		StringBuilder polylineBuilder = new StringBuilder();
//		for(int i = 0; i < stepsList.length; i++)	{
//			List<LatLng> path = stepsList[i].polyline.decodePath();
//			polylineBuilder.append(stepsList[i].polyline.getEncodedPath());
//			
//		}
//		return polylineBuilder.toString();
//	}
	
	public int getNumberOfRoutes()	{
		return result.routes.length;
	}

	public String getTransitMode() {
		return additionalData.get(DirectionsRequest.TRANSIT_MODE);
	}
	
	public String getDepartureOption()	{
		return additionalData.get(DirectionsRequest.DEPARTURE_OPTION);
	}

//	public void setTransitMode(String transitMode) {
//		this.transitMode = transitMode;
//	}
	
	public String getOriginLatLng() {
		return additionalData.get(DirectionsRequest.ORIGIN_LATLNG);
	}
	
	public String getDestinationLatLng() {
		return additionalData.get(DirectionsRequest.DESTINATION_LATLNG);
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	private String getTimeReadable(DateTime dt)	{
		return dt.getHourOfDay() + ":" + dt.getMinuteOfHour();
	}
	
	private String getDateReadable(DateTime dt)	{
		return dt.getDayOfMonth() + "-" + dt.getMonthOfYear() + "-" + dt.getYear();
	}
	
	
}
