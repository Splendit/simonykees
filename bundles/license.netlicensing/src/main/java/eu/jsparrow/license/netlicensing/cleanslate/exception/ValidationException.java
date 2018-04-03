package eu.jsparrow.license.netlicensing.cleanslate.exception;

public class ValidationException extends Exception{
	
	public ValidationException(Exception e) {
		super(e);
	}
	
	public ValidationException(String message) {
		super(message);
	}
	
	public ValidationException(String message, Exception e) {
		super(message,e);
	}

}
