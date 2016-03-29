package main;

/*
 * Abstract class specifying implementation of Status Exceptions.
 * All exceptions need to have a status and an error message. 
 * 
 * @author jg404
 */
public abstract class StatusException extends Exception	{
	private final int status;
	private final String message;
	
	public StatusException(final int pStatus, final String pMessage){
		status = pStatus;
		message = pMessage;
	}

	/*
	 * Returns status code of the error.
	 */
	public int getStatus() {
		return status;
	}

	/*
	 * Returns error message of the error.
	 */
	@Override
	public String getMessage() {
		return message;
	}
	
}
