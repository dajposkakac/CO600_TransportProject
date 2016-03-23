package main;

public class DateInPastException extends StatusException{
	
	public DateInPastException(int pStatus, String pErrorMessage) {
		super(pStatus, pErrorMessage);
	}
}
