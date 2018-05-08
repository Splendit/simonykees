package eu.jsparrow.standalone.exceptions;

import eu.jsparrow.rules.common.exception.SimonykeesException;

/**
 * A generic exception indicating that the refactoring in the standalone is
 * interrupted due to a checked exception.
 *
 */
public class StandAloneException extends SimonykeesException {

	private static final long serialVersionUID = 5168002168077506405L;

	public StandAloneException(String message, Throwable cause) {
		super(message, cause);
	}

}
