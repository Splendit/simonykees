package eu.jsparrow.rules.common.util;

import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.TryStatement;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * Contains functionality for finding the current JDT version.
 * 
 * @since 2.6.0
 *
 */
public class JdtVersionBindingUtil {

	private static final String ORG_ECLIPSE_JDT = "org.eclipse.jdt"; //$NON-NLS-1$
	private static final String PHOTON_JDT_VERSION = "3.14.0"; //$NON-NLS-1$
	private static final String OXYGEN_JDT_VERSION = "3.13.0"; //$NON-NLS-1$
	private static final String NEON_JDT_VERSION = "3.12.0"; //$NON-NLS-1$
	private static Bundle jdtBundle;

	private JdtVersionBindingUtil() {
		/*
		 * Hiding default constructor
		 */
	}

	/**
	 * Finds the version of the {@value #ORG_ECLIPSE_JDT} bundle on the current
	 * {@link Platform}.
	 * 
	 * @return the {@link Version} of {@value #ORG_ECLIPSE_JDT} bundle on the
	 *         current platform or {@value #NEON_JDT_VERSION} if none is found
	 *         since it corresponds to the least eclipse version that we
	 *         support.
	 */
	public static Version findCurrentJDTVersion() {
		if (jdtBundle == null) {
			Bundle bundle = Platform.getBundle(ORG_ECLIPSE_JDT);
			if (bundle == null) {
				return Version.parseVersion(NEON_JDT_VERSION);
			}
			jdtBundle = bundle;
		}
		return jdtBundle.getVersion();
	}

	/**
	 * Finds the appropriate JLS level for the given JDT version.
	 * 
	 * @param jdtVersion
	 *            the JDT version on the current platform
	 * @return {@link AST#JLS10} if the JDT version corresponds to Photon;
	 *         {@link AST#JLS9} if the version corresponds to Oxygen; or
	 *         {@link AST#JLS8} if the version corresponds to Neon.
	 */
	@SuppressWarnings("deprecation")
	public static int findJLSLevel(Version jdtVersion) {
		if (isAtLeastPhoton(jdtVersion)) {
			return AST.JLS10;
		} else if (isAtLeastOxygen(jdtVersion)) {
			return AST.JLS9;
		}
		return AST.JLS8;
	}

	/**
	 * Finds the compiler options for the {@link ASTParser} based on the given
	 * JDT version.
	 * 
	 * @param jdtVersion
	 *            the JDT version of the current {@link Platform}.
	 * @return options corresponding to {@link JavaCore#VERSION_10} if the JDT
	 *         version corresponds to Photon; options corresponding to
	 *         {@link JavaCore#VERSION_9} if the JDT version corresponds to
	 *         Oxygen; or options corresponding to {@link JavaCore#VERSION_8} if
	 *         the JDT version corresponds to Neon.
	 */
	public static Map<String, String> findCompilerOptions(Version jdtVersion) {
		String javaVersion = JavaCore.VERSION_1_8;
		if (isAtLeastPhoton(jdtVersion)) {
			javaVersion = JavaCore.VERSION_10;
		} else if (isAtLeastOxygen(jdtVersion)) {
			javaVersion = JavaCore.VERSION_9;
		}
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, javaVersion);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, javaVersion);
		options.put(JavaCore.COMPILER_SOURCE, javaVersion);
		return options;
	}

	/**
	 * Finds the resources property descriptior for the {@link TryStatement}
	 * statement.
	 * 
	 * @param jdtVersion
	 *            the JDT version on the current {@link Platform}.
	 * @return {@link TryStatement#RESOURCES2_PROPERTY} if the provided JDT
	 *         version corresponds to at least Oxygen, or
	 *         {@link TryStatement#RESOURCES_PROPERTY} otherwise.
	 */
	@SuppressWarnings("deprecation")
	public static ChildListPropertyDescriptor findTryWithResourcesProperty(Version jdtVersion) {
		if (isAtLeastOxygen(jdtVersion)) {
			return TryStatement.RESOURCES2_PROPERTY;
		}
		return TryStatement.RESOURCES_PROPERTY;
	}

	private static boolean isAtLeastPhoton(Version jdtVersion) {
		return jdtVersion.compareTo(Version.parseVersion(PHOTON_JDT_VERSION)) >= 0;
	}

	private static boolean isAtLeastOxygen(Version jdtVersion) {
		return jdtVersion.compareTo(Version.parseVersion(OXYGEN_JDT_VERSION)) >= 0;
	}

}
