package eu.jsparrow.core.exception;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter
 * @since 0.9
 */
public class MalformedInputException extends SimonykeesException {
	private static final long serialVersionUID = 226771914518618608L;

	public MalformedInputException() {
		super();
	}

	public MalformedInputException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MalformedInputException(String message, Throwable cause) {
		super(message, cause);
	}

	public MalformedInputException(String message) {
		super(message);
	}

	public MalformedInputException(Throwable cause) {
		super(cause);
	}

	public MalformedInputException(String message, String uiMessage, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, uiMessage, cause, enableSuppression, writableStackTrace);
	}

	public MalformedInputException(String message, String uiMessage, Throwable cause) {
		super(message, uiMessage, cause);
	}

	public MalformedInputException(String message, String uiMessage) {
		super(message, uiMessage);
	}
}
