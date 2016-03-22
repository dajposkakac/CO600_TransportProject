package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;

import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

public class DirectionsResults {
	
	public static final String TRAIN = "Train";
	public static final String BUS = "Bus";
	public static final String DRIVE = "Drive";
	
	public static final String CURRENCY_POUND = "£";
	
	private DirectionsResult result;
	private List<DirectionsRoute> results;
	private HashMap<String, String> additionalData;
	private HashMap<String, Integer> priceData;
	private int status;
	
	
	public DirectionsResults(int status, HashMap<String, String> pAdditionalData)	{
		setStatus(status);
		additionalData = pAdditionalData;
	}
	
	public DirectionsResults(int status)	{
		setStatus(status);
	}
	
	public void addResults(DirectionsResult directionsResult, HashMap<String, Integer> pPriceData)	{
		if(results == null)	{
			results = new ArrayList<>(20);
		}
		if(priceData == null)	{
			priceData = new HashMap<>();
		}
		for(int i = 0; i < directionsResult.routes.length; i++)	{
			results.add(directionsResult.routes[i]);
		}
		priceData.putAll(pPriceData);
	}
	
	public String getDestinationForRoute(int route)	{
		return results.get(route).legs[0].endAddress;
	}
	
	public String getOriginForRoute(int route)	{
		return results.get(route).legs[0].startAddress;
	}
	
	public String getDistanceForRoute(int route)	{
		return results.get(route).legs[0].distance.humanReadable;
	}
	
	public String getDurationForRoute(int route)	{
		return results.get(route).legs[0].duration.humanReadable;
	}
	
	public String getArrivalTimeForRoute(int route)	{
		return getTimeReadable(results.get(route).legs[0].arrivalTime);
	}
	
	public String getDepartureTimeForRoute(int route)	{
		return getTimeReadable(results.get(route).legs[0].departureTime);
	}
	
	public String getArrivalDateForRoute(int route)	{
		return getDateReadable(results.get(route).legs[0].arrivalTime);
	}
	
	public String getDepartureDateForRoute(int route)	{
		return getDateReadable(results.get(route).legs[0].departureTime);
	}
	
	/*
	 * @return decoded and detailed polyline of route
	 */
//	public String getPolylineForRoute(int route)	{
//		List<LatLng> polyline = new ArrayList<>();
//		DirectionsStep[] stepsList = results.get(route).legs[0].steps;
//		StringBuilder polylineBuilder = new StringBuilder();
//		for(int i = 0; i < stepsList.length; i++)	{
//			List<LatLng> path = stepsList[i].polyline.decodePath();
//			for(LatLng pos : path)	{
//				polylineBuilder.append(pos.lat);
//				polylineBuilder.append(",");
//				polylineBuilder.append(pos.lng);
//				polylineBuilder.append("|");
//			}
//		}
//		polylineBuilder.deleteCharAt(polylineBuilder.length() - 1);
//		return polylineBuilder.toString().trim();
//	}
	
	/*
	 * @return encoded and smoothed polyline of route
	 */
	public String getPolylineForRoute(int route)	{
		return results.get(route).overviewPolyline.getEncodedPath();
	}
	
	/*
	 * @return encoded and detailed polyline of route
	 */
//	public String getPolylineForRoute(int route)	{
//		DirectionsStep[] stepsList = results.get(route).legs[0].steps;
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
			if(priceData.containsKey(transitMode))	{
				price = priceData.get(transitMode);
			}
		}	else if(transitMode.equalsIgnoreCase(TravelMode.DRIVING.toString()))	{
			price = priceData.get(DRIVE);
		}
		return String.valueOf(price);
	}
	
	public int getNumberOfRoutes()	{
		return results.size();
	}

	public String getTransitModeForRoute(int route) {
		String travelMode = "unknown";
		DirectionsStep[] steps = results.get(route).legs[0].steps;
		int i = 0;
		boolean found = false;
		while(i < steps.length && !found)	{
			TravelMode stepTravelMode = steps[i].travelMode;
			if(stepTravelMode == TravelMode.DRIVING  || stepTravelMode == (TravelMode.BICYCLING))	{
				travelMode = stepTravelMode.toString();
				found = true;
			}	else if(stepTravelMode == TravelMode.TRANSIT)	{
				String vehicleName = steps[i].transitDetails.line.vehicle.name;
				if(vehicleName.equals(TRAIN) || vehicleName.equals(BUS))	{
					travelMode = vehicleName;
					found = true;
				}
			}	else if(stepTravelMode == TravelMode.WALKING)	{
				travelMode = stepTravelMode.toString();
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
