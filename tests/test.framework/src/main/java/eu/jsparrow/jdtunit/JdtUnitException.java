package eu.jsparrow.jdtunit;

/**
 * 
 * @author Hans-Jörg Schnedlitz
 * @since 2.5.0
 */
public class JdtUnitException extends Exception {

	private static final long serialVersionUID = -9217858914225093699L;

	public JdtUnitException(String string) {
		super(string);
	}

	public JdtUnitException(String string, Exception e) {
		super(string, e);
	}

}
