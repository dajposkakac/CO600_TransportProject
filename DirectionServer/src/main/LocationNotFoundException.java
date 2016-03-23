package main;

import com.google.maps.errors.NotFoundException;

public class LocationNotFoundException extends StatusException{

	public LocationNotFoundException(int pStatus, String pErrorMessage) {
		super(pStatus, pErrorMessage);
	}

}
