package main;

public class DateInPastException extends Exception{
	
	public DateInPastException(String errorMessage) {
		super(errorMessage);
	}
}
