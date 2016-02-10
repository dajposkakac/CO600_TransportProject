package transportapp.co600.googledirectionstest;

import com.google.maps.model.TravelMode;

/**
 * Created by daj on 02/12/2015.
 */
public class Request {
    private String destination;
    private String origin;
    private TravelMode transitMode;

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

    public TravelMode getTransitMode() {
        return transitMode;
    }

    public void setTransitMode(TravelMode transitMode) {
        this.transitMode = transitMode;
    }
}
