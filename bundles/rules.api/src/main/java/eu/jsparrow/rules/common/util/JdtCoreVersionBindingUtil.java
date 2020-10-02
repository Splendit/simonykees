package eu.jsparrow.rules.common.util;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.dom.AST;
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
public class JdtCoreVersionBindingUtil {

	private static final String ORG_ECLIPSE_JDT_CORE = "org.eclipse.jdt.core"; //$NON-NLS-1$
	private static final String JDT_JAVA_14_SUPPORT = "3.22.0"; //$NON-NLS-1$
	private static final String JDT_JAVA_13_SUPPORT = "3.20.0"; //$NON-NLS-1$
	private static final String JDT_JAVA_12_SUPPORT = "3.18.0"; //$NON-NLS-1$
	private static final String JDT_JAVA_11_SUPPORT = "3.16.0"; //$NON-NLS-1$
	private static final String JDT_JAVA_10_SUPPORT = "3.14.0"; //$NON-NLS-1$
	private static final String JDT_JAVA_9_SUPPORT = "3.13.2"; //$NON-NLS-1$
	private static final String JDT_LEAST_SUPPORTED = "3.12.0"; //$NON-NLS-1$
	private static Bundle jdtBundle;

	private JdtCoreVersionBindingUtil() {
		/*
		 * Hiding default constructor
		 */
	}

	/**
	 * Finds the version of the {@value #ORG_ECLIPSE_JDT_CORE} bundle on the current
	 * {@link Platform}.
	 * 
	 * @return the {@link Version} of {@value #ORG_ECLIPSE_JDT_CORE} bundle on the
	 *         current platform or {@value #JDT_LEAST_SUPPORTED} if none is
	 *         found since it corresponds to the least eclipse version that we
	 *         support.
	 */
	public static Version findCurrentJDTCoreVersion() {
		if (jdtBundle == null) {
			Bundle bundle = Platform.getBundle(ORG_ECLIPSE_JDT_CORE);
			if (bundle == null) {
				return Version.parseVersion(JDT_LEAST_SUPPORTED);
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
	 * @return
	 *         <ul>
	 *         <li>{@link AST#JLS14} if the JDT version corresponds to 3.22.0
	 *         (2020-06);</li>
	 *         <li>{@link AST#JLS13} if the JDT version corresponds to 3.20.0
	 *         (2019-12);</li>
	 *         <li>{@link AST#JLS12} if the JDT version corresponds to 3.18.0
	 *         (2019-06);</li>
	 *         <li>{@link AST#JLS11} if the JDT version corresponds to 3.16.0
	 *         (2018-12);</li>
	 *         <li>{@link AST#JLS10} if the JDT version corresponds to
	 *         Photon;</li>
	 *         <li>{@link AST#JLS9} if the version corresponds to Oxygen;
	 *         or</li>
	 *         <li>{@link AST#JLS8} if the version corresponds to Neon.</li>
	 *         </ul>
	 */
	@SuppressWarnings("deprecation")
	public static int findJLSLevel(Version jdtVersion) {
		if (isJava14Supported(jdtVersion)) {
			/*
			 * @since 3.22.0 -> 20-06
			 */
			return AST.JLS14;
		} else if (isJava13Supported(jdtVersion)) {
			/*
			 * @since 3.20.0 -> 19-12
			 */
			return AST.JLS13;
		} else if (isJava12Supported(jdtVersion)) {
			/*
			 * @since 3.19.0 -> 2019-09
			 * 
			 * @since 3.18.0 -> 2019-06
			 */
			return AST.JLS12;
		} else if (isJava11Supported(jdtVersion)) {
			/*
			 * @since 3.17.0 -> 2019-03
			 * 
			 * @since 3.16.0 -> 2018-12
			 */
			return AST.JLS11;
		} else if (isJava10Supported(jdtVersion)) {
			return AST.JLS10;
		} else if (isJava9Supported(jdtVersion)) {
			return AST.JLS9;
		}
		return AST.JLS8;
	}

	/**
	 * Finds the resources property descriptor for the {@link TryStatement}
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
		if (isJava9Supported(jdtVersion)) {
			return TryStatement.RESOURCES2_PROPERTY;
		}
		return TryStatement.RESOURCES_PROPERTY;
	}

	private static boolean isJava14Supported(Version jdtVersion) {
		return jdtVersion.compareTo(Version.parseVersion(JDT_JAVA_14_SUPPORT)) >= 0;
	}

	private static boolean isJava13Supported(Version jdtVersion) {
		return jdtVersion.compareTo(Version.parseVersion(JDT_JAVA_13_SUPPORT)) >= 0;
	}

	private static boolean isJava12Supported(Version jdtVersion) {
		return jdtVersion.compareTo(Version.parseVersion(JDT_JAVA_12_SUPPORT)) >= 0;
	}

	private static boolean isJava11Supported(Version jdtVersion) {
		return jdtVersion.compareTo(Version.parseVersion(JDT_JAVA_11_SUPPORT)) >= 0;
	}

	private static boolean isJava10Supported(Version jdtVersion) {
		return jdtVersion.compareTo(Version.parseVersion(JDT_JAVA_10_SUPPORT)) >= 0;
	}

	private static boolean isJava9Supported(Version jdtVersion) {
		return jdtVersion.compareTo(Version.parseVersion(JDT_JAVA_9_SUPPORT)) >= 0;
	}
}
