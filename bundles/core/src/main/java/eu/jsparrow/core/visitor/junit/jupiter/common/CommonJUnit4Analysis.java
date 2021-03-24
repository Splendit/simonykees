package eu.jsparrow.core.visitor.junit.jupiter.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

/**
 * Helper class with static members which my be used by various classes within
 * the child packages of {@link eu.jsparrow.core.visitor.junit.jupiter}.
 * 
 * @since 3.29.0
 *
 */
public class CommonJUnit4Analysis {

	public static final Map<String, String> JUNIT4_TO_JUPITER_TEST_ANNOTATIONS_MAP;

	static {

		Map<String, String> tmpMap = new HashMap<>();
		tmpMap.put("org.junit.Ignore", "org.junit.jupiter.api.Disabled"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("org.junit.Test", "org.junit.jupiter.api.Test"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("org.junit.After", "org.junit.jupiter.api.AfterEach"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("org.junit.AfterClass", "org.junit.jupiter.api.AfterAll"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("org.junit.Before", "org.junit.jupiter.api.BeforeEach"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("org.junit.BeforeClass", "org.junit.jupiter.api.BeforeAll"); //$NON-NLS-1$//$NON-NLS-2$

		JUNIT4_TO_JUPITER_TEST_ANNOTATIONS_MAP = Collections.unmodifiableMap(tmpMap);

	}

	private CommonJUnit4Analysis() {
		// private constructor hiding implicit public one
	}

	/**
	 * @return true if the type of the given {@link Expression} can be related
	 *         to a type declaration unequivocally.
	 */
	public static boolean isArgumentWithUnambiguousType(Expression expression) {
		if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation methodInvocation = (MethodInvocation) expression;
			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			return methodBinding != null && !(methodBinding.isParameterizedMethod() && methodInvocation.typeArguments()
				.isEmpty());
		}
		if (expression.getNodeType() == ASTNode.SUPER_METHOD_INVOCATION) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) expression;
			IMethodBinding superMethodBinding = superMethodInvocation.resolveMethodBinding();
			return superMethodBinding != null
					&& !(superMethodBinding.isParameterizedMethod() && superMethodInvocation.typeArguments()
						.isEmpty());
		}
		return true;
	}
}