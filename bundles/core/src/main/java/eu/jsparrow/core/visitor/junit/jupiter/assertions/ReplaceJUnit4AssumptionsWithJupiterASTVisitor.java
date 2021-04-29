package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import org.eclipse.jdt.core.dom.IMethodBinding;

/**
 * Replaces the JUnit 4 method invocations {@code org.junit.Assume.assumeFalse}
 * and {@code org.junit.Assume.assumeTrue} by invocations of the corresponding
 * methods of the JUnit Jupiter class {@code org.junit.jupiter.api.Assumptions}.
 * 
 * @since 3.30.0
 * 
 */
public class ReplaceJUnit4AssumptionsWithJupiterASTVisitor extends AbstractJUnit4MethodInvocationToJupiterASTVisitor {

	public ReplaceJUnit4AssumptionsWithJupiterASTVisitor() {
		super("org.junit.Assume", "org.junit.jupiter.api.Assumptions"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected boolean isSupportedJUnit4Method(IMethodBinding methodBinding) {
		if (isContentOfType(methodBinding.getDeclaringClass(), "org.junit.Assume")) { //$NON-NLS-1$
			String methodName = methodBinding.getName();
			return methodName.equals("assumeFalse") || //$NON-NLS-1$
					methodName.equals("assumeTrue"); //$NON-NLS-1$
		}
		return false;
	}
}