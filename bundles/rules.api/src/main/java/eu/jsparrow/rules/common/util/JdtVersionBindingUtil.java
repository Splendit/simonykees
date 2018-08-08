package eu.jsparrow.rules.common.util;

import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.TryStatement;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * Contains functionality for finding the current jdt version.
 * 
 * @since 2.6.0
 *
 */
public class JdtVersionBindingUtil {
	
	private static final String ORG_ECLIPSE_JDT = "org.eclipse.jdt"; //$NON-NLS-1$
	private static final String PHOTON_JDT_VERSION = "3.14.0"; //$NON-NLS-1$
	private static final String OXYGEN_JDT_VERSION = "3.13.4"; //$NON-NLS-1$
	private static Bundle jdtBundle;

	private JdtVersionBindingUtil() {
		/*
		 * Hiding default constructor
		 */
	}

	public static Version findCurrentJDTVersion() {
		if(jdtBundle == null) {
			 Bundle bundle = Platform.getBundle(ORG_ECLIPSE_JDT);
			 if(bundle == null) {
				 return Version.parseVersion(OXYGEN_JDT_VERSION);
			 }
			 jdtBundle = bundle;
		}
		
		return jdtBundle.getVersion();
	}

	@SuppressWarnings("deprecation")
	public static int findJLSLevel(Version jdtVersion) {
		if (isAtLeastPhoton(jdtVersion)) {
			return AST.JLS10;
		}
		return AST.JLS8;
	}

	public static Map<String, String> findCompilerOptions(Version jdtVersion) {
		String javaVersion = JavaCore.VERSION_1_8;
		if (isAtLeastPhoton(jdtVersion)) {
			javaVersion = JavaCore.VERSION_10;
		}
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, javaVersion);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, javaVersion);
		options.put(JavaCore.COMPILER_SOURCE, javaVersion);
		return options;
	}

	@SuppressWarnings("deprecation")
	public static ChildListPropertyDescriptor findTryWithResourcesProperty(Version jdtVersion) {
		if (isAtLeastPhoton(jdtVersion)) {
			return TryStatement.RESOURCES2_PROPERTY;
		}
		return TryStatement.RESOURCES_PROPERTY;
	}

	private static boolean isAtLeastPhoton(Version jdtVersion) {
		return jdtVersion.compareTo(Version.parseVersion(PHOTON_JDT_VERSION)) >= 0;
	}

}
