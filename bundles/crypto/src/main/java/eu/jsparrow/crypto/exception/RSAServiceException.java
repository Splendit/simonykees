package eu.jsparrow.crypto.exception;

import eu.jsparrow.crypto.service.RSAService;

/**
 * This {@link Exception} is used for potential {@link RSAService} problems.
 * 
 * @since 3.5.0
 */
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
