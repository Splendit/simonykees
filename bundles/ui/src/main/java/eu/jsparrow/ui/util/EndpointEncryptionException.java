package eu.jsparrow.ui.util;

public class EndpointEncryptionException extends Exception {

	private static final long serialVersionUID = 8593202424690399559L;

	public EndpointEncryptionException() {
		super();
	}
	
	public EndpointEncryptionException(String message) {
		super(message);
	}
	
	public EndpointEncryptionException(String message, Throwable t) {
		super(message, t);
	}
}
