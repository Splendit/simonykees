package eu.jsparrow.license.api.exception;

public class PersistenceException extends Exception {

	private static final long serialVersionUID = 1827616726796668359L;

	public PersistenceException(Exception e) {
		super(e);
	}
	
	public PersistenceException(String message, Exception e) {
		super(message,e);
	}

}

