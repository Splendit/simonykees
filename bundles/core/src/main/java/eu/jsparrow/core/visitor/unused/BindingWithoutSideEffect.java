package eu.jsparrow.core.visitor.unused;

import java.util.Arrays;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class BindingWithoutSideEffect {
	private static final String JAVA_UTIL = "java.util"; //$NON-NLS-1$

	static boolean isSupportedConstructorType(ITypeBinding typeBinding) {
		if (ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
				Arrays.asList(java.lang.Iterable.class.getName(), java.util.Map.class.getName()))) {
			IPackageBinding packageBinding = typeBinding.getPackage();
			return packageBinding.getName()
				.equals(JAVA_UTIL);
		}
		return false;
	}

	static boolean isSupportedMethod(IMethodBinding methodBinding) {
		return false;
	}
}
