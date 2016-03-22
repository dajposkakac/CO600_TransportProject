package transportapp.co600.journeyorganiserapp;

import com.google.maps.model.TravelMode;

/**
 * Created by daj on 02/12/2015.
 */
public class Request {
    private String destination;
    private String origin;
    private String transitMode;
    private String time;
    private String date;
    private String departureOption;

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

    public String getTransitMode() {
        if(transitMode.equalsIgnoreCase("unknown")) {
            transitMode = "driving,transit,bicycling,walking";
        }
        return transitMode;
    }

    public void setTransitMode(String transitMode) {
        this.transitMode = transitMode;
    }

    public String getTime() {return time;}

    public void setTime(String time) {this.time = time;}

    public String getDate() {return date;}

    public void setDate(String date) {this.date = date;}

    public String getDepartureOption() {return departureOption;}

    public void setDepartureOption(String departureOption) {this.departureOption = departureOption;}
}
