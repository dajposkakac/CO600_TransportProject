package main;

/*
 * Exception thrown when location specified was not found.
 * 
 * @author jg404
 */
public class LocationNotFoundException extends StatusException{

	public LocationNotFoundException(final int pStatus, final String pErrorMessage) {
		super(pStatus, pErrorMessage);
	}

}
