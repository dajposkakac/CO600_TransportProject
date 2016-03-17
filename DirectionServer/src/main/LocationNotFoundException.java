package main;

import com.google.maps.errors.NotFoundException;

public class LocationNotFoundException extends NotFoundException{

	public LocationNotFoundException(String errorMessage) {
		super(errorMessage);
	}

}
