package eu.jsparrow.license.api.exception;

import eu.jsparrow.license.api.LicensePersistenceService;

/**
 * This exception is thrown by {@link LicensePersistenceService}.
 */
public class PersistenceException extends Exception {

	private static final long serialVersionUID = 1827616726796668359L;

	public PersistenceException(Exception e) {
		super(e);
	}

	public PersistenceException(String message, Exception e) {
		super(message, e);
	}

	public PersistenceException(String message) {
		super(message);
	}

}
