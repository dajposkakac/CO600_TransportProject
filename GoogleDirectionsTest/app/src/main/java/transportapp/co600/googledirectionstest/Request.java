package transportapp.co600.googledirectionstest;

/**
 * Created by daj on 02/12/2015.
 */
public class Request {
    private String destination;
    private String origin;
    private String transitMode;

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
        return transitMode;
    }

    public void setTransitMode(String transitMode) {
        this.transitMode = transitMode;
    }
}
