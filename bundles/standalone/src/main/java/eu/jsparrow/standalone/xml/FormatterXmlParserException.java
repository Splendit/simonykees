package eu.jsparrow.standalone.xml;

import eu.jsparrow.standalone.exceptions.StandaloneException;

/**
 * Exception that is thrown whenever Eclipse formatting goes wrong.
 * 
 * @since 3.23.0
 */
public class FormatterXmlParserException extends StandaloneException {

	private static final long serialVersionUID = 1480096625757014731L;

	public FormatterXmlParserException(String message) {
		super(message);
	}

	public FormatterXmlParserException(String message, Throwable cause) {
		super(message, cause);
	}

}
