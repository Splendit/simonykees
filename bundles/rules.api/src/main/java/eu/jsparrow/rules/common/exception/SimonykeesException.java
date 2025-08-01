package eu.jsparrow.rules.common.exception;

import eu.jsparrow.i18n.ExceptionMessages;

/**
 * Generic exception used in all of jSparrow. 
 * 
 * @author Martin Huter
 * @since 0.9
 */
public class SimonykeesException extends Exception {
	private static final long serialVersionUID = 4654641184128521329L;

	private final String uiMessage;

	public SimonykeesException() {
		super();
		this.uiMessage = null;
	}

	public SimonykeesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.uiMessage = null;
	}

	public SimonykeesException(String message, Throwable cause) {
		super(message, cause);
		this.uiMessage = null;
	}

	public SimonykeesException(String message) {
		super(message);
		this.uiMessage = null;
	}

	public SimonykeesException(Throwable cause) {
		super(cause);
		this.uiMessage = null;
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
