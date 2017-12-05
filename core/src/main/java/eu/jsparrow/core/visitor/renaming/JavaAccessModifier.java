package eu.jsparrow.core.visitor.renaming;

/**
 * an enum representing the java access modifiers
 * 
 * @author Matthias Webhofer
 * @since 2.4.0
 */
public enum JavaAccessModifier {
	PUBLIC("public"), //$NON-NLS-1$
	PROTECTED("protected"), //$NON-NLS-1$
	PRIVATE("private"), //$NON-NLS-1$
	PACKAGE_PRIVATE("package-private"); //$NON-NLS-1$

	private final String modifier;

	private JavaAccessModifier(String s) {
		modifier = s;
	}

	@Override
	public String toString() {
		return this.modifier;
	}
}
