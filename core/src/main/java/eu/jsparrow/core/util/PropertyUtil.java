package eu.jsparrow.core.util;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.JavaCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to handle property conversion
 * 
 * Since 2.2.1
 * 
 * @author mgh
 *
 */
public class PropertyUtil {

	private static final Logger logger = LoggerFactory.getLogger(PropertyUtil.class);

	private PropertyUtil() {

	}

	/**
	 * Converts a String that is declared in the style of
	 * {@link JavaCore#COMPILER_COMPLIANCE} to the {@link JavaVersion}
	 * counterpart. Defaults to Java
	 * 
	 * @param version
	 *            the {@link JavaCore#COMPILER_COMPLIANCE} String representation
	 *            that is transformed
	 * @return the apache commons lang3 representation of that String
	 */
	public static JavaVersion stringToJavaVersion(String version) {
		if (version == null) {
			return JavaVersion.JAVA_1_1;
		}
		String enumRepresentation = convertCompilerComplianceToEnumRepresentation(version);
		JavaVersion usedJavaVersion = JavaVersion.JAVA_1_1;
		try {
			usedJavaVersion = JavaVersion.valueOf(enumRepresentation);
		} catch (IllegalArgumentException e) {
			/*
			 * SIM-844 HOTFIX to accept java 9: java 1 to 8 constants from
			 * eclipse can be translated to a commons.lang 3.1 version Java 9 is
			 * not accepted handle it as Java 1
			 */
			logger.error("Java Version could not be parsed by JavaVersion Enum", e); //$NON-NLS-1$
		}
		return usedJavaVersion;
	}

	private static String convertCompilerComplianceToEnumRepresentation(String compilerCompliance) {
		return "JAVA_" + compilerCompliance.replace(".", "_"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
