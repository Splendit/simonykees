package eu.jsparrow.standalone.exceptions;

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
