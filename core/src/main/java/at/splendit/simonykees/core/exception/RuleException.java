package at.splendit.simonykees.core.exception;

public class RuleException extends SimonykeesException {
	private static final long serialVersionUID = 226771914518618608L;

	public RuleException() {
		super();
	}

	public RuleException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RuleException(String message, Throwable cause) {
		super(message, cause);
	}

	public RuleException(String message) {
		super(message);
	}

	public RuleException(Throwable cause) {
		super(cause);
	}

	public RuleException(String message, String uiMessage, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, uiMessage, cause, enableSuppression, writableStackTrace);
	}

	public RuleException(String message, String uiMessage, Throwable cause) {
		super(message, uiMessage, cause);
	}

	public RuleException(String message, String uiMessage) {
		super(message, uiMessage);
	}
}
