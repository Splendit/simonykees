package eu.jsparrow.core.visitor.junit.junit3;

/**
 * TODOO: move this functionalities to visitor
 *
 * 
 */
public class JUnit3Utilities {
	static boolean isJUnit3PackageName(String qualifiedName) {

		return "junit.extensions".equals(qualifiedName) || //$NON-NLS-1$
				"junit.framework".equals(qualifiedName); //$NON-NLS-1$
	}

	static boolean isQualifiedNameInsideJUnit3(String qualifiedName) {

		return qualifiedName.startsWith("junit.extensions.") || //$NON-NLS-1$
				qualifiedName.startsWith("junit.framework."); //$NON-NLS-1$
	}

}
