package eu.jsparrow.core.visitor.assertj;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class SupportedAssertJAssertions {

	private static final List<String> SUPPORTED_ASSERTION_PREFIXES = Collections.unmodifiableList(Arrays.asList(
			"is", //$NON-NLS-1$
			"has", //$NON-NLS-1$
			"contains", //$NON-NLS-1$
			"can", //$NON-NLS-1$
			"doesNot", //$NON-NLS-1$
			"startsWith", //$NON-NLS-1$
			"endsWith", //$NON-NLS-1$
			"matches", //$NON-NLS-1$
			"allSatisfy", //$NON-NLS-1$
			"anySatisfy", //$NON-NLS-1$
			"noneSatisfy", //$NON-NLS-1$
			"allMatch", //$NON-NLS-1$
			"anyMatch", //$NON-NLS-1$
			"noneMatch", //$NON-NLS-1$
			"exists", //$NON-NLS-1$
			"satisfies", //$NON-NLS-1$
			"are", //$NON-NLS-1$
			"have"//$NON-NLS-1$
	));

	public static boolean isSupportedAssertJAssertionMethodName(String methodName) {
		return SUPPORTED_ASSERTION_PREFIXES.stream()
			.anyMatch(methodName::startsWith);
	}

	public static boolean isSupportedAssertJAssertion(MethodInvocation invocation) {
		String methodName = invocation.getName()
			.getIdentifier();
		if (isSupportedAssertJAssertionMethodName(methodName)) {
			IMethodBinding methodBinding = invocation.resolveMethodBinding();
			if (methodBinding != null) {
				String returnTypeQualifiedName = methodBinding.getMethodDeclaration()
					.getReturnType()
					.getQualifiedName();
				return "SELF".equals(returnTypeQualifiedName); //$NON-NLS-1$
			}
		}
		return false;
	}

	private SupportedAssertJAssertions() {
		// hiding implicit public constructor of utility class
	}
}
