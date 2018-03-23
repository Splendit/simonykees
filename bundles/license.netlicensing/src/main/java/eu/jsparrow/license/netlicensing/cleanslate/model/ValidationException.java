package eu.jsparrow.license.netlicensing.cleanslate.model;

public class ValidationException extends Exception{
	
	public ValidationException(Exception e) {
		super(e);
	}
	
	public ValidationException(String message, Exception e) {
		super(message,e);
	}

}
