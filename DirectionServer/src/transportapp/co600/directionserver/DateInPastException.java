package transportapp.co600.directionserver;

public class DateInPastException extends Exception{
	
	public DateInPastException(String errorMessage) {
		super(errorMessage);
	}
}
