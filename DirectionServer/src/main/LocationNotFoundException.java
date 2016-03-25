package main;

/*
 * Exception thrown when location specified was not found.
 * 
 * @author jg404
 */
public class LocationNotFoundException extends StatusException{

	public LocationNotFoundException(int pStatus, String pErrorMessage) {
		super(pStatus, pErrorMessage);
	}

}
