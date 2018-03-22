package eu.jsparrow.core.config;

/**
 * this exception is thrown if something goes wrong while reading, writing or
 * applying configuration
 * 
 * @author Matthias Webhofer
 * @since 2.2.2
 */
public class YAMLConfigException extends Exception {

	private static final long serialVersionUID = 7746159986872341549L;

	public YAMLConfigException() {
		super();
	}

	public YAMLConfigException(String message) {
		super(message);
	}

	public YAMLConfigException(Throwable e) {
		super(e);
	}

	public YAMLConfigException(String message, Throwable e) {
		super(message, e);
	}
}
