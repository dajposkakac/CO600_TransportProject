package main;

/*
 * Exception thrown when date specified is in the past.
 * 
 * @author jg404
 */
public class DateInPastException extends StatusException{
	
	public DateInPastException(int pStatus, String pErrorMessage) {
		super(pStatus, pErrorMessage);
	}
}
