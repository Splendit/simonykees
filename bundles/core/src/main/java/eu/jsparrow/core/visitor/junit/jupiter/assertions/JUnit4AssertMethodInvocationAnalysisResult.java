package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Optional;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;

/**
 * Immutable class storing all necessary informations about the given invocation
 * of a static method of the class {@code org.junit.Assert} which may be
 * replaced by an invocation of the corresponding method of
 * {@code org.junit.jupiter.api.Assertions}.
 * 
 * @since 3.28.0
 *
 */
class JUnit4AssertMethodInvocationAnalysisResult {

	private final MethodInvocation methodInvocation;
	private final String methodName;
	private final boolean messageMovingToLastPosition;
	private final boolean transformableInvocation;
	private String deprecatedMethodNameReplacement;
	private Type throwingRunnableTypeToReplace;

	JUnit4AssertMethodInvocationAnalysisResult(MethodInvocation methodInvocation,
			boolean messageMovingToLastPosition,
			boolean transformableInvocation, Type throwingRunnableTypeToReplace) {
		this(methodInvocation, messageMovingToLastPosition, transformableInvocation);
		this.throwingRunnableTypeToReplace = throwingRunnableTypeToReplace;
	}

	JUnit4AssertMethodInvocationAnalysisResult(MethodInvocation methodInvocation,
			boolean messageMovingToLastPosition,
			boolean transformableInvocation, String deprecatedMethodNameReplacement) {
		this(methodInvocation, messageMovingToLastPosition, transformableInvocation);
		this.deprecatedMethodNameReplacement = deprecatedMethodNameReplacement;
	}

	JUnit4AssertMethodInvocationAnalysisResult(MethodInvocation methodInvocation,
			boolean messageMovingToLastPosition,
			boolean transformableInvocation) {
		this.methodInvocation = methodInvocation;
		this.methodName = methodInvocation.getName()
			.getIdentifier();
		this.messageMovingToLastPosition = messageMovingToLastPosition;
		this.transformableInvocation = transformableInvocation;
	}

	MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	String getMethodName() {
		return methodName;
	}

	Optional<String> getDeprecatedMethodNameReplacement() {
		return Optional.ofNullable(deprecatedMethodNameReplacement);
	}

	Optional<Type> getThrowingRunnableTypeToReplace() {
		return Optional.ofNullable(throwingRunnableTypeToReplace);
	}

	boolean isMessageMovingToLastPosition() {
		return messageMovingToLastPosition;
	}

	boolean isTransformableInvocation() {
		return transformableInvocation;
	}
}
