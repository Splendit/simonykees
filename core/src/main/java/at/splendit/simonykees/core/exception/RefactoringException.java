package at.splendit.simonykees.core.exception;

public class RefactoringException extends SimonykeesException {
	private static final long serialVersionUID = 226771914518618608L;

	public RefactoringException() {
		super();
	}

	public RefactoringException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RefactoringException(String message, Throwable cause) {
		super(message, cause);
	}

	public RefactoringException(String message) {
		super(message);
	}

	public RefactoringException(Throwable cause) {
		super(cause);
	}

	public RefactoringException(String message, String uiMessage, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, uiMessage, cause, enableSuppression, writableStackTrace);
	}

	public RefactoringException(String message, String uiMessage, Throwable cause) {
		super(message, uiMessage, cause);
	}

	public RefactoringException(String message, String uiMessage) {
		super(message, uiMessage);
	}
}
