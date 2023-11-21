package eu.jsparrow.standalone.exceptions;

/**
 * Exception, which indicates that the import of maven projects has failed
 * 
 * @since 3.3.0
 */
public class MavenImportException extends Exception {

	private static final long serialVersionUID = 6884147961417884355L;

	public MavenImportException() {
		super();
	}

	public MavenImportException(String msg) {
		super(msg);
	}

	public MavenImportException(String msg, Throwable t) {
		super(msg, t);
	}
}
