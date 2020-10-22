package eu.jsparrow.standalone.xml;

/**
 * Exception that is thrown whenever Eclipse formatting goes wrong.
 * 
 * @since 3.23.0
 */
public class FormatterXmlParserException extends Exception {

	private static final long serialVersionUID = 1480096625757014731L;

	public FormatterXmlParserException(String message) {
		super(message);
	}

	public FormatterXmlParserException(String message, Throwable cause) {
		super(message, cause);
	}

}
