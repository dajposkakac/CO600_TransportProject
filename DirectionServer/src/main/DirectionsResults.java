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
	
	public static final String TRAIN = "Train";
	public static final String BUS = "Bus";
	public static final String DRIVE = "Drive";
	
	public static final String CURRENCY_POUND = "£";
	
	private final DirectionsResult result;
	private HashMap<String, String> additionalData;
	private HashMap<String, Integer> priceData;
	private int status;
	
	
	public DirectionsResults(int status, DirectionsResult directionsResult, HashMap<String, String> pAdditionalData, HashMap<String, Integer> pPriceData)	{
		setStatus(status);
		result = directionsResult;
		additionalData = pAdditionalData;
		priceData = pPriceData;
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
	
	public String getPriceForRoute(int route)	{
		String transitMode = getTransitModeForRoute(route);
		int price = -1;
		if(transitMode.equals(TRAIN) || transitMode.equals(BUS))	{
			price = priceData.get(transitMode);
		}	else if(transitMode.equals(TravelMode.DRIVING.toString().toUpperCase()))	{
			price = priceData.get(DRIVE);
		}
		return String.valueOf(price);
	}
	
	public int getNumberOfRoutes()	{
		return result.routes.length;
	}

	public String getTransitModeForRoute(int route) {
		String travelMode = "unknown";
		String requestTravelMode = additionalData.get(DirectionsRequest.TRANSIT_MODE);
		DirectionsStep[] steps = result.routes[route].legs[0].steps;
		int i = 0;
		boolean found = false;
		while(i < steps.length && !found)	{
			TravelMode stepTravelMode = steps[i].travelMode;
			if((stepTravelMode == TravelMode.DRIVING || stepTravelMode == TravelMode.WALKING || stepTravelMode == (TravelMode.BICYCLING)) && stepTravelMode == TravelMode.valueOf(requestTravelMode))	{
				travelMode = requestTravelMode;
				found = true;
			}	else if(stepTravelMode == TravelMode.TRANSIT && stepTravelMode == TravelMode.valueOf(requestTravelMode))	{
				String vehicleName = steps[i].transitDetails.line.vehicle.name;
				if(vehicleName.equals(TRAIN) || vehicleName.equals(BUS))	{
					travelMode = vehicleName;
					found = true;
				}
			}
			i++;
		}
		return travelMode;
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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	private String getTimeReadable(DateTime dt)	{
		return addMissingZero(dt.getHourOfDay()) + ":" + addMissingZero(dt.getMinuteOfHour());
	}
	
	private String getDateReadable(DateTime dt)	{
		return addMissingZero(dt.getDayOfMonth()) + "-" + addMissingZero(dt.getMonthOfYear()) + "-" + dt.getYear();
	}
	
	private String addMissingZero(int time)    {
        String timeString = String.valueOf(time);
        if(time < 10 && time > -1)  {
            timeString = "0" + timeString;
        }
        return timeString;
    }
	
	
}
