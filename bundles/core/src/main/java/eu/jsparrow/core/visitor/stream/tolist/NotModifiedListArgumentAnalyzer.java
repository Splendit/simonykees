package eu.jsparrow.core.visitor.stream.tolist;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * 
 * @since 4.4.0
 *
 */
@SuppressWarnings("nls")
public class NotModifiedListArgumentAnalyzer {

	private static final List<String> SAFE_COLLECTIONS_METHOD_NAMES = Collections.unmodifiableList(Arrays.asList(
			"min", "max", "frequency", "disjoint", "indexOfSubList", "lastIndexOfSubList", "unmodifiableList",
			"unmodifiableCollection"));

	private static final List<String> SAFE_COLLECTION_METHOD_NAMES = Collections.unmodifiableList(Arrays.asList(
			"addAll", "containsAll", "removeAll", "retainAll"));

	private NotModifiedListArgumentAnalyzer() {
		// hiding implicit default constructor
	}

	static boolean keepsListArgumentUnmodified(MethodInvocation invocationAcceptingList) {
		IMethodBinding methodBinding = invocationAcceptingList.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}
		String methodName = methodBinding.getName();
		if (methodName.equals("equals")) {
			ITypeBinding[] parameterTypes = methodBinding.getMethodDeclaration()
				.getParameterTypes();
			return parameterTypes.length == 1
					&& ClassRelationUtil.isContentOfType(parameterTypes[0], Object.class.getName());
		}

		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		boolean isListOrSet = ClassRelationUtil.isContentOfTypes(declaringClass,
				Arrays.asList(java.util.List.class.getName(), java.util.Set.class.getName()));
		if (methodName.equals("copyOf") && isListOrSet) {
			IMethodBinding methodDeclaration = methodBinding.getMethodDeclaration();
			ITypeBinding[] parameterTypes = methodDeclaration.getParameterTypes();
			return parameterTypes.length == 1
					&& ClassRelationUtil.isContentOfType(parameterTypes[0], Collection.class.getName());
		}

		if (ClassRelationUtil.isContentOfType(declaringClass, java.util.Collections.class.getName())) {
			return SAFE_COLLECTIONS_METHOD_NAMES.contains(methodName);
		}

		if (ClassRelationUtil.isInheritingContentOfTypes(declaringClass,
				Collections.singletonList(java.util.Collection.class.getName()))) {
			return SAFE_COLLECTION_METHOD_NAMES.contains(methodName);
		}

		return false;
	}
}
