package main;

/*
 * Exception thrown when date specified is in the past.
 * 
 * @author jg404
 */
public class DateInPastException extends StatusException{
	
	public DateInPastException(final int pStatus, final String pErrorMessage) {
		super(pStatus, pErrorMessage);
	}
}
