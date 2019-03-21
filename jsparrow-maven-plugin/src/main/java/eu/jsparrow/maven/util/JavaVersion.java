package eu.jsparrow.maven.util;

public class JavaVersion {
	
	private JavaVersion() {
		/* Hide constructor */
	}

	
	public static final String PROPERTY_CONSTANT = "java.version"; //$NON-NLS-1$
	public static final String V_1_8 = "1.8"; //$NON-NLS-1$
	public static final String V_11 = "11"; //$NON-NLS-1$
	
	public static boolean isJava8or11() {
		String javaVersion = System.getProperty(JavaVersion.PROPERTY_CONSTANT);
		return javaVersion.startsWith(JavaVersion.V_1_8) || javaVersion.startsWith(JavaVersion.V_11);
	}
	
}
