package at.splendit.simonykees.core.exception;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter
 * @since 0.9
 * 
 */
public class ReconcileException extends SimonykeesException {
	private static final long serialVersionUID = 226771914518618608L;

	public ReconcileException() {
		super();
	}

	public ReconcileException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ReconcileException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReconcileException(String message) {
		super(message);
	}

	public ReconcileException(Throwable cause) {
		super(cause);
	}

	public ReconcileException(String message, String uiMessage, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, uiMessage, cause, enableSuppression, writableStackTrace);
	}

	public ReconcileException(String message, String uiMessage, Throwable cause) {
		super(message, uiMessage, cause);
	}

	public ReconcileException(String message, String uiMessage) {
		super(message, uiMessage);
	}
}
