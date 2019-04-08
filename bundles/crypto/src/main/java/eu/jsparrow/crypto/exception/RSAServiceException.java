package eu.jsparrow.crypto.exception;

public class RSAServiceException extends Exception {

	private static final long serialVersionUID = 6148629667426307741L;

	public RSAServiceException() {
		super();
	}

	public RSAServiceException(String message) {
		super(message);
	}

	public RSAServiceException(String message, Throwable t) {
		super(message, t);
	}
}
