package eu.jsparrow.rules.common.exception;

/**
 * Indicates that the version of a library cannot be parsed.
 * 
 * @since 3.27.0
 *
 */
public class InvalidLibraryVersionException extends SimonykeesException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8069247899589387351L;

	public InvalidLibraryVersionException(String message) {
		super(message);
	}

}
