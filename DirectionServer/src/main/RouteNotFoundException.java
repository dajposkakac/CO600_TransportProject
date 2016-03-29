package main;

/*
 * Exception thrown when no routes were found between specified locations.
 * 
 * @author jg404
 */
public class RouteNotFoundException extends StatusException{

	public RouteNotFoundException(final int pStatus, final String errorMessage) {
		super(pStatus, errorMessage);
	}

}
