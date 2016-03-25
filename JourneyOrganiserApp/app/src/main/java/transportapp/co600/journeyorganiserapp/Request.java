package transportapp.co600.journeyorganiserapp;

/**
 * A representation of the user input, used to create XML response.
 */
public class Request {
    private String destination;
    private String origin;
    private String transitMode;
    private String time;
    private String date;
    private String departureOption;
    private String sortingPreference;

    /**
     * Returns origin location specified by the user.
     * @return origin location
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Sets the origin location field.
     * @param origin
     */
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /**
     * Returns destination location specified by the user.
     * @return destination location
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Sets the destination location field.
     * @param destination
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * Returns travel mode specified by the user.
     * @return travel mode
     */
    public String getTransitMode() {
        if(transitMode.equalsIgnoreCase("unknown")) {
            transitMode = "driving,transit,bicycling,walking";
        }
        return transitMode;
    }

    /**
     * Sets the travel mode field.
     * @param transitMode
     */
    public void setTransitMode(String transitMode) {
        this.transitMode = transitMode;
    }

    /**
     * Returns the time specified by the user.
     * @return time
     */
    public String getTime() {return time;}

    /**
     * Sets the time field.
     * @param time
     */
    public void setTime(String time) {this.time = time;}

    /**
     * Returns the date specified by the user.
     * @return time
     */
    public String getDate() {return date;}

    /**
     * Sets the date field.
     * @param date
     */
    public void setDate(String date) {this.date = date;}

    /**
     * Returns departure option specified by the user.
     * @return departureOption
     */
    public String getDepartureOption() {return departureOption;}

    /**
     * Sets the departureOption field.
     * @param departureOption
     */
    public void setDepartureOption(String departureOption) {this.departureOption = departureOption;}

    /**
     * Returns the sorting preference specified by the user.
     * @return sortingPreference
     */
    public String getSortingPreference() {return sortingPreference;}

    /**
     * Sets the sortingPreference field.
     * @param sortingPreference
     */
    public void setSortingPreference(String sortingPreference) {this.sortingPreference = sortingPreference;}
}
