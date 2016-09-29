package at.splendit.simonykees.core.exception;

import at.splendit.simonykees.core.i18n.ExceptionMessages;

public class SimonykeesException extends Exception {
	private static final long serialVersionUID = 4654641184128521329L;

	private String uiMessage = null;

	public SimonykeesException() {
		super();
	}

	public SimonykeesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SimonykeesException(String message, Throwable cause) {
		super(message, cause);
	}

	public SimonykeesException(String message) {
		super(message);
	}

	public SimonykeesException(Throwable cause) {
		super(cause);
	}

	public SimonykeesException(String message, String uiMessage, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.uiMessage = uiMessage;
	}

	public SimonykeesException(String message, String uiMessage, Throwable cause) {
		super(message, cause);
		this.uiMessage = uiMessage;
	}

	public SimonykeesException(String message, String uiMessage) {
		super(message);
		this.uiMessage = uiMessage;
	}

	public String getUiMessage() {
		return (uiMessage == null) ? ExceptionMessages.SimonykeesException_default_ui_message + super.getMessage()
				: uiMessage;
	}
}
