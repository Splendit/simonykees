package eu.jsparrow.maven.util;

import org.apache.maven.artifact.versioning.ComparableVersion;

public class JavaVersion {
	
	private JavaVersion() {
		/* Hide constructor */
	}

	
	public static final String PROPERTY_CONSTANT = "java.version"; //$NON-NLS-1$
	public static final String V_1_8 = "1.8"; //$NON-NLS-1$
	public static final String V_11 = "11"; //$NON-NLS-1$
	private static final ComparableVersion VERSION_11 = new ComparableVersion("11"); //$NON-NLS-1$
	
	public static boolean isJava8or11() {
		String javaVersion = System.getProperty(JavaVersion.PROPERTY_CONSTANT);
		return javaVersion.startsWith(JavaVersion.V_1_8) || javaVersion.startsWith(JavaVersion.V_11);
	}

	public static boolean isAtLeastJava11() {
		String javaVersion = System.getProperty(JavaVersion.PROPERTY_CONSTANT);
		ComparableVersion currentVersion = new ComparableVersion(javaVersion);
		return currentVersion.compareTo(VERSION_11) >= 0;
	}
	
}
