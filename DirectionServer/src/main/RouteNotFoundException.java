package main;

import com.google.maps.errors.NotFoundException;

public class RouteNotFoundException extends StatusException{

	public RouteNotFoundException(int pStatus, String errorMessage) {
		super(pStatus, errorMessage);
	}

}
