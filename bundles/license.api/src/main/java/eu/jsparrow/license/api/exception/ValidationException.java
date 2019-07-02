package eu.jsparrow.license.api.exception;

import eu.jsparrow.license.api.LicenseService;

/**
 * The exception is thrown by the {@link LicenseService}.
 */
public class ValidationException extends Exception{
	
	private static final long serialVersionUID = -5463841225580702458L;

	public ValidationException(Throwable e) {
		super(e);
	}
	
	public ValidationException(String message) {
		super(message);
	}
	
	public ValidationException(String message, Throwable e) {
		super(message,e);
	}

}
