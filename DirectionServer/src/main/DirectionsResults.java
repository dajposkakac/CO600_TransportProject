package main;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;

import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.Duration;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

public class DirectionsResults {
	
	public static final String TRAIN = "Train";
	public static final String BUS = "Bus";
	public static final String DRIVE = "Drive";
	
	public static final String DAYS_SHORT = "d";
	public static final String HOURS_SHORT = "h";
	public static final String MINUTES_SHORT = "m";
	public static final String SECONDS_SHORT = "s";
	
	public static final String CURRENCY_POUND = "£";
	public static final String COLON = ":";
	public static final String DASH = "-";
	
	private List<DirectionsRoute> results;
	private HashMap<String, String> additionalData;
	private HashMap<String, Integer> priceData;
	private int status;
	private String errorMessage;
	
	
	public DirectionsResults(int status, HashMap<String, String> pAdditionalData)	{
		setStatus(status);
		additionalData = pAdditionalData;
	}
	
	public DirectionsResults(int status, String pErrorMessage)	{
		setStatus(status);
		errorMessage = pErrorMessage;
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
		long seconds = results.get(route).legs[0].duration.inSeconds;
		long days = TimeUnit.SECONDS.toDays(seconds);
		seconds -= TimeUnit.DAYS.toSeconds(days);
		long hours = TimeUnit.SECONDS.toHours(seconds);
		seconds -= TimeUnit.HOURS.toSeconds(hours);
		long minutes = TimeUnit.SECONDS.toMinutes(seconds);
		seconds -= TimeUnit.MINUTES.toSeconds(minutes);
		StringBuilder builder = new StringBuilder();
		if(days > 0)	{
			builder.append(days + DAYS_SHORT + DirectionsRequest.SPACE);
		}
		if(hours > 0)	{
			builder.append(hours + HOURS_SHORT + DirectionsRequest.SPACE);
		}
		if(minutes > 0)	{
			builder.append(minutes + MINUTES_SHORT);
		}
		return builder.toString().trim();
	}
	
	public String getArrivalTimeForRoute(int route)	{
		String time = null;
		String tm = getTransitModeForRoute(route);
		if(!tm.equalsIgnoreCase(TRAIN)
				&& !tm.equalsIgnoreCase(TRAIN))	{
			long timeSeconds = Integer.valueOf(additionalData.get(DirectionsRequest.TIME));
			long durationSeconds = results.get(route).legs[0].duration.inSeconds;
			timeSeconds = (timeSeconds + durationSeconds);
			additionalData.put(DirectionsRequest.ARRIVAL_TIME_IN_SECONDS, String.valueOf(timeSeconds));
			time = getTimeReadable(new DateTime(timeSeconds  * 1000));
		}	else 	{
			DateTime dt = results.get(route).legs[0].arrivalTime;
			additionalData.put(DirectionsRequest.ARRIVAL_TIME_IN_SECONDS, String.valueOf(dt.getMillis() / 1000));
			time = getTimeReadable(dt);
		}
		return time;
	}
	
	public String getDepartureTimeForRoute(int route)	{
		String time = null;
		String tm = getTransitModeForRoute(route);
		if(!tm.equalsIgnoreCase(TRAIN)
				&& !tm.equalsIgnoreCase(TRAIN))	{
			long timeSeconds = Integer.valueOf(additionalData.get(DirectionsRequest.TIME));
			additionalData.put(DirectionsRequest.DEPARTURE_TIME_IN_SECONDS, String.valueOf(timeSeconds));
			time = getTimeReadable(new DateTime(timeSeconds * 1000));
		}	else	{
			DateTime dt = results.get(route).legs[0].arrivalTime;
			additionalData.put(DirectionsRequest.DEPARTURE_TIME_IN_SECONDS, String.valueOf(dt.getMillis() / 1000));
			time = getTimeReadable(results.get(route).legs[0].departureTime);
		}
		return time;
	}
	
	public String getArrivalDateForRoute(int route)	{
		String date = null;
		String tm = getTransitModeForRoute(route);
		if(!tm.equalsIgnoreCase(TRAIN)
				&& !tm.equalsIgnoreCase(TRAIN))	{
			long dateSeconds = Integer.valueOf(additionalData.get(DirectionsRequest.TIME));
			long durationSeconds = results.get(route).legs[0].duration.inSeconds;
			date = getDateReadable(new DateTime((dateSeconds + durationSeconds)* 1000));
		}	else 	{
			date = getDateReadable(results.get(route).legs[0].arrivalTime);
		}
		return date;
	}
	
	public String getDepartureDateForRoute(int route)	{
		String date = null;
		String tm = getTransitModeForRoute(route);
		if(!tm.equalsIgnoreCase(TRAIN)
				&& !tm.equalsIgnoreCase(TRAIN))	{
			long dateSeconds = Integer.valueOf(additionalData.get(DirectionsRequest.TIME));
			date = getDateReadable(new DateTime((dateSeconds)* 1000));
		}	else 	{
			date = getDateReadable(results.get(route).legs[0].departureTime);
		}
		return date;
	}
	
	public String getArrivalTimeInSecondsForRoute(int route)	{
		return additionalData.get(DirectionsRequest.ARRIVAL_TIME_IN_SECONDS);
	}
	
	public String getDepartureTimeInSecondsForRoute(int route)	{
		return additionalData.get(DirectionsRequest.DEPARTURE_TIME_IN_SECONDS);
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
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	private String getTimeReadable(DateTime dt)	{
		return addMissingZero(dt.getHourOfDay()) + COLON + addMissingZero(dt.getMinuteOfHour());
	}
	
	private String getDateReadable(DateTime dt)	{
		return addMissingZero(dt.getDayOfMonth()) + DASH + addMissingZero(dt.getMonthOfYear()) + DASH + dt.getYear();
	}
	
	private String addMissingZero(int time)    {
        String timeString = String.valueOf(time);
        if(time < 10 && time > -1)  {
            timeString = "0" + timeString;
        }
        return timeString;
    }
	
}
