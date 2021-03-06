package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;

import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.TravelMode;

/*
 * Contains list of routes, price information and additional data
 * generated by the request. Also holds status of the request and 
 * error message if errors occured. This class is used to as an interface
 * for easily extracting information to be parsed into the XML response.
 * 
 * @author jg404
 */
public class DirectionsResults {
	
	public static final String TRAIN = "Train";
	public static final String BUS = "Bus";
	public static final String DRIVE = "Drive";
	
	public static final String DAYS_SHORT = "d";
	public static final String HOURS_SHORT = "h";
	public static final String MINUTES_SHORT = "m";
	public static final String SECONDS_SHORT = "s";
	
	public static final String CURRENCY_POUND = "�";
	public static final String COLON = ":";
	public static final String DASH = "-";
	
	private List<DirectionsRoute> results;
	private HashMap<String, String> additionalData;
	private HashMap<String, Integer> priceData;
	private int status;
	private String errorMessage;
	
	/*
	 * Constructor for the successful response.
	 */
	public DirectionsResults(final int status, final HashMap<String, String> pAdditionalData)	{
		setStatus(status);
		additionalData = pAdditionalData;
	}
	
	/*
	 * Constructor for the error response.
	 */
	public DirectionsResults(final int status, final String pErrorMessage)	{
		setStatus(status);
		errorMessage = pErrorMessage;
	}
	
	/*
	 * Adds more routes and their prices.
	 */
	public void addResults(final DirectionsResult directionsResult, final HashMap<String, Integer> pPriceData)	{
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
	
	/*
	 * Returns destination address for specified route.
	 */
	public String getDestinationForRoute(final int route)	{
		return results.get(route).legs[0].endAddress;
	}
	
	/*
	 * Returns origin address for specified route.
	 */
	public String getOriginForRoute(final int route)	{
		return results.get(route).legs[0].startAddress;
	}
	
	/*
	 * Returns origin display name.
	 */
	public String getOriginDisplayName()	{
		return additionalData.get(DirectionsRequest.ORIGIN_DISPLAY);
	}
	
	/*
	 * Returns destination display name.
	 */
	public String getDestinationDisplayName()	{
		return additionalData.get(DirectionsRequest.DESTINATION_DISPLAY);
	}
	
	/*
	 * Returns distance in human readable form for specified route.
	 */
	public String getDistanceForRoute(final int route)	{
		return results.get(route).legs[0].distance.humanReadable;
	}
	
	/*
	 * Returns distance in human readable for for specified route.
	 */
	public String getDurationForRoute(final int route)	{
		long seconds = results.get(route).legs[0].duration.inSeconds;
		final long days = TimeUnit.SECONDS.toDays(seconds);
		seconds -= TimeUnit.DAYS.toSeconds(days);
		final long hours = TimeUnit.SECONDS.toHours(seconds);
		seconds -= TimeUnit.HOURS.toSeconds(hours);
		final long minutes = TimeUnit.SECONDS.toMinutes(seconds);
		seconds -= TimeUnit.MINUTES.toSeconds(minutes);
		final StringBuilder builder = new StringBuilder();
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
	
	/*
	 * Returns arrival time in human readable form for specified route.
	 */
	public String getArrivalTimeForRoute(final int route)	{
		String time = null;
		final String tm = getTransitModeForRoute(route);
		if(!tm.equalsIgnoreCase(TRAIN)
				&& !tm.equalsIgnoreCase(TRAIN))	{
			long timeSeconds = Integer.valueOf(additionalData.get(DirectionsRequest.TIME));
			if(additionalData.get(DirectionsRequest.DEPARTURE_OPTION).startsWith("Depart"))	{	
				final long durationSeconds = results.get(route).legs[0].duration.inSeconds;
				timeSeconds = (timeSeconds + durationSeconds);
			}
			additionalData.put(DirectionsRequest.ARRIVAL_TIME_IN_SECONDS, String.valueOf(timeSeconds));
			time = getTimeReadable(new DateTime(timeSeconds  * 1000));
		}	else 	{
			final DateTime dt = results.get(route).legs[0].arrivalTime;
			additionalData.put(DirectionsRequest.ARRIVAL_TIME_IN_SECONDS, String.valueOf(dt.getMillis() / 1000));
			time = getTimeReadable(dt);
		}
		return time;
	}
	
	/*
	 * Returns departure time in human readable form for specified route.
	 */
	public String getDepartureTimeForRoute(final int route)	{
		String time = null;
		final String tm = getTransitModeForRoute(route);
		if(!tm.equalsIgnoreCase(TRAIN)
				&& !tm.equalsIgnoreCase(TRAIN))	{
			long timeSeconds = Integer.valueOf(additionalData.get(DirectionsRequest.TIME));
			if(additionalData.get(DirectionsRequest.DEPARTURE_OPTION).startsWith("Arrive"))	{	
				final long durationSeconds = results.get(route).legs[0].duration.inSeconds;
				timeSeconds = (timeSeconds - durationSeconds);
			}
			additionalData.put(DirectionsRequest.DEPARTURE_TIME_IN_SECONDS, String.valueOf(timeSeconds));
			time = getTimeReadable(new DateTime(timeSeconds * 1000));
		}	else	{
			final DateTime dt = results.get(route).legs[0].departureTime;
			additionalData.put(DirectionsRequest.DEPARTURE_TIME_IN_SECONDS, String.valueOf(dt.getMillis() / 1000));
			time = getTimeReadable(results.get(route).legs[0].departureTime);
		}
		return time;
	}
	
	/*
	 * Returns arrival date in human readable form for specified route.
	 */
	public String getArrivalDateForRoute(final int route)	{
		String date = null;
		final String tm = getTransitModeForRoute(route);
		if(!tm.equalsIgnoreCase(TRAIN)
				&& !tm.equalsIgnoreCase(TRAIN))	{
			long dateSeconds = Integer.valueOf(additionalData.get(DirectionsRequest.TIME));
			if(additionalData.get(DirectionsRequest.DEPARTURE_OPTION).startsWith("Depart"))	{	
				final long durationSeconds = results.get(route).legs[0].duration.inSeconds;
				dateSeconds = (dateSeconds + durationSeconds);
			}
			date = getDateReadable(new DateTime(dateSeconds * 1000));
		}	else 	{
			date = getDateReadable(results.get(route).legs[0].arrivalTime);
		}
		return date;
	}
	
	/*
	 * Returns departure date in human readable form for specified route.
	 */
	public String getDepartureDateForRoute(final int route)	{
		String date = null;
		final String tm = getTransitModeForRoute(route);
		if(!tm.equalsIgnoreCase(TRAIN)
				&& !tm.equalsIgnoreCase(TRAIN))	{
			long dateSeconds = Integer.valueOf(additionalData.get(DirectionsRequest.TIME));
			if(additionalData.get(DirectionsRequest.DEPARTURE_OPTION).startsWith("Arrive"))	{	
				final long durationSeconds = results.get(route).legs[0].duration.inSeconds;
				dateSeconds = (dateSeconds - durationSeconds);
			}
			date = getDateReadable(new DateTime((dateSeconds)* 1000));
		}	else 	{
			date = getDateReadable(results.get(route).legs[0].departureTime);
		}
		return date;
	}
	
	/*
	 * Returns arrival time in seconds for specified route.
	 */
	public String getArrivalTimeInSecondsForRoute(final int route)	{
		return additionalData.get(DirectionsRequest.ARRIVAL_TIME_IN_SECONDS);
	}
	
	/*
	 * Returns arrival time in seconds for specified route.
	 */
	public String getDepartureTimeInSecondsForRoute(final int route)	{
		return additionalData.get(DirectionsRequest.DEPARTURE_TIME_IN_SECONDS);
	}
	
	/*
	 * Returns decoded and detailed polyline for sspecified route
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
	 * Returns encoded and smoothed polyline for specified route
	 */
	public String getPolylineForRoute(final int route)	{
		return results.get(route).overviewPolyline.getEncodedPath();
	}
	
	/*
	 * Returns encoded and detailed polyline for specified route
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
	
	/*
	 * Returns price for specified route.
	 */
	public String getPriceForRoute(final int route)	{
		final String transitMode = getTransitModeForRoute(route);
		int price = -1;
		if(transitMode.equals(TRAIN) || transitMode.equals(BUS))	{
			if(priceData.containsKey(transitMode))	{
				price = priceData.get(transitMode);
			}
		}	else if(transitMode.equalsIgnoreCase(TravelMode.DRIVING.toString()))	{
			if(priceData.containsKey(DRIVE))	{
				price = priceData.get(DRIVE);
			}
		}
		return String.valueOf(price);
	}
	
	/*
	 * Returns number of routes found.
	 */
	public int getNumberOfRoutes()	{
		return results.size();
	}

	/*
	 * Returns transit mode for the specified route
	 */
	public String getTransitModeForRoute(final int route) {
		String travelMode = "unknown";
		final DirectionsStep[] steps = results.get(route).legs[0].steps;
		int i = 0;
		boolean found = false;
		while(i < steps.length && !found)	{
			final TravelMode stepTravelMode = steps[i].travelMode;
			if(stepTravelMode == TravelMode.DRIVING  || stepTravelMode == (TravelMode.BICYCLING))	{
				travelMode = stepTravelMode.toString();
				found = true;
			}	else if(stepTravelMode == TravelMode.TRANSIT)	{
				final String vehicleName = steps[i].transitDetails.line.vehicle.name;
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
	
	/*
	 * Returns departure option specified by client
	 */
	public String getDepartureOption()	{
		return additionalData.get(DirectionsRequest.DEPARTURE_OPTION);
	}

	/*
	 * Returns origin coordinates
	 */
	public String getOriginLatLng() {
		return additionalData.get(DirectionsRequest.ORIGIN_LATLNG);
	}
	
	/*
	 * Returns destination coordinates
	 */
	public String getDestinationLatLng() {
		return additionalData.get(DirectionsRequest.DESTINATION_LATLNG);
	}
	
	/*
	 * Returns sorting preference specified by client
	 */
	public String getSortingPreference()	{
		return additionalData.get(DirectionsRequest.SORTING_PREFERENCE);
	}

	/*
	 * Returns status of the request.
	 */
	public int getStatus() {
		return status;
	}

	/*
	 * Updates value of the status of the request.
	 */
	public void setStatus(final int status) {
		this.status = status;
	}
	
	/*
	 * Returns error message if one occurs.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/*
	 * Updates value of the error message.
	 */
	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/*
	 * Converts DateTime object into a human readable time.
	 */
	private String getTimeReadable(final DateTime dt)	{
		return addMissingZero(dt.getHourOfDay()) + COLON + addMissingZero(dt.getMinuteOfHour());
	}
	
	/*
	 * Converts DateTime object into a human readable date.
	 */
	private String getDateReadable(final DateTime dt)	{
		return addMissingZero(dt.getDayOfMonth()) + DASH + addMissingZero(dt.getMonthOfYear()) + DASH + dt.getYear();
	}
	
	/*
	 * Adds leading 0s to single digits.
	 */
	private String addMissingZero(final int time)    {
        String timeString = String.valueOf(time);
        if(time < 10 && time > -1)  {
            timeString = "0" + timeString;
        }
        return timeString;
    }
	
}
