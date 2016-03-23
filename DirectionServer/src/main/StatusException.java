package main;

public abstract class StatusException extends Exception	{
	private final int status;
	private final String message;
	
	public StatusException(int pStatus, String pMessage){
		status = pStatus;
		message = pMessage;
	}

	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
	
}
