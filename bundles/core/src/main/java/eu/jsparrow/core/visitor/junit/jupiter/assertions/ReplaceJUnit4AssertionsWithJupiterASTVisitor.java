package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

	private final Set<String> potentialMethodNameReplacements;

	public ReplaceJUnit4AssertionsWithJupiterASTVisitor() {
		super("org.junit.jupiter.api.Assertions"); //$NON-NLS-1$
		Set<String> tmp = new HashSet<>();
		tmp.add("assertArrayEquals"); //$NON-NLS-1$
		potentialMethodNameReplacements = Collections.unmodifiableSet(tmp);

	}

	@Override
	protected boolean isSupportedJUnit4Method(IMethodBinding methodBinding) {
		return isContentOfType(methodBinding.getDeclaringClass(), "org.junit.Assert") //$NON-NLS-1$
				&& !methodBinding.getName()
					.equals("assertThat"); //$NON-NLS-1$
	}

	@Override
	protected Set<String> getSupportedMethodNameReplacements() {
		return potentialMethodNameReplacements;
	}
}