package eu.jsparrow.license.netlicensing.exception;

public class PersistenceException extends Exception {

	public PersistenceException(Exception e) {
		super(e);
	}
	
	public PersistenceException(String message, Exception e) {
		super(message,e);
	}

}

