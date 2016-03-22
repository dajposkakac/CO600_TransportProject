package main;

import com.google.maps.errors.NotFoundException;

public class RouteNotFoundException extends NotFoundException{

	public RouteNotFoundException(String errorMessage) {
		super(errorMessage);
	}

}
