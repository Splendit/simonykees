package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import org.eclipse.jdt.core.dom.IMethodBinding;

/**
 * Replaces invocations of methods of the JUnit-4-class {@code org.junit.Assert}
 * by invocations of the corresponding methods of the JUnit-Jupiter-class
 * {@code org.junit.jupiter.api.Assertions}.
 * 
 * @since 3.28.0
 * 
 */
public class ReplaceJUnit4AssertionsWithJupiterASTVisitor extends AbstractJUnit4MethodInvocationToJupiterASTVisitor {

	public ReplaceJUnit4AssertionsWithJupiterASTVisitor() {
		super("org.junit.Assert", "org.junit.jupiter.api.Assertions"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected boolean isSupportedJUnit4Method(IMethodBinding methodBinding) {
		return isContentOfType(methodBinding.getDeclaringClass(), "org.junit.Assert") //$NON-NLS-1$
				&& !methodBinding.getName()
					.equals("assertThat"); //$NON-NLS-1$
	}
}