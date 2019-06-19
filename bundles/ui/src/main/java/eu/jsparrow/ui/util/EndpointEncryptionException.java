package eu.jsparrow.ui.util;

/**
 * This {@link Exception} is thrown when an error occurs in
 * {@link EndpointEncryption}.
 *
 * @since 3.5.0
 */
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
