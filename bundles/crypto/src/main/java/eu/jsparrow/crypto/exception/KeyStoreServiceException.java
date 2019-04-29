package eu.jsparrow.crypto.exception;

import eu.jsparrow.crypto.service.KeyStoreService;

/**
 * This {@link Exception} is used for potential {@link KeyStoreService} problems
 * 
 * @since 3.5.0
 */
public class KeyStoreServiceException extends Exception {

	private static final long serialVersionUID = 2764814363815811777L;

	public KeyStoreServiceException() {
		super();
	}

	public KeyStoreServiceException(String message) {
		super(message);
	}

	public KeyStoreServiceException(String message, Throwable t) {
		super(message, t);
	}
}
