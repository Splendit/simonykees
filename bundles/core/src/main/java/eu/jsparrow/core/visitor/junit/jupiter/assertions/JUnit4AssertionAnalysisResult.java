package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Optional;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;

/**
 * Immutable class storing all necessary informations about the given invocation
 * of a static method declared in a JUnit 4 class like {@code org.junit.Assert}
 * or {@code org.junit.Assume} which may be transformed to an invocation of a
 * the corresponding JUnit Jupiter method.
 * 
 * @since 3.28.0
 *
 */
class JUnit4AssertionAnalysisResult extends JUnit4MethodInvocationAnalysisResult {

	private Type throwingRunnableTypeToReplace;

	JUnit4AssertionAnalysisResult(MethodInvocation methodInvocation, IMethodBinding methodBinding,
			Type throwingRunnableTypeToReplace, boolean transformableInvocation) {
		this(methodInvocation, methodBinding, transformableInvocation);
		this.throwingRunnableTypeToReplace = throwingRunnableTypeToReplace;
	}

	JUnit4AssertionAnalysisResult(MethodInvocation methodInvocation, IMethodBinding methodBinding,
			boolean transformableInvocation) {
		super(methodInvocation, methodBinding, transformableInvocation);
	}

	Optional<Type> getThrowingRunnableTypeToReplace() {
		return Optional.ofNullable(throwingRunnableTypeToReplace);
	}
}
