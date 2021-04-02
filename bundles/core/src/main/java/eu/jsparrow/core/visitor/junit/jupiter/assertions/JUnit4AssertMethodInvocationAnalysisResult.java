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
	private final String deprecatedMethodNameReplacement;
	private final Type throwingRunnableTypeToReplace;
	private final boolean messageMovingToLastPosition;
	private final boolean transformableInvocation;
	
	JUnit4AssertMethodInvocationAnalysisResult(MethodInvocation methodInvocation,
			Type throwingRunnableTypeToReplace, boolean messageMovingToLastPosition,
			boolean transformableInvocation) {
		this.methodInvocation = methodInvocation;
		this.methodName = methodInvocation.getName()
			.getIdentifier();
		this.deprecatedMethodNameReplacement = null;
		this.throwingRunnableTypeToReplace = throwingRunnableTypeToReplace;
		this.messageMovingToLastPosition = messageMovingToLastPosition;
		this.transformableInvocation = transformableInvocation;
	}

	JUnit4AssertMethodInvocationAnalysisResult(MethodInvocation methodInvocation,
			String deprecatedMethodNameReplacement, boolean messageMovingToLastPosition,
			boolean transformableInvocation) {
		this.methodInvocation = methodInvocation;
		this.methodName = methodInvocation.getName()
			.getIdentifier();
		this.deprecatedMethodNameReplacement = deprecatedMethodNameReplacement;
		this.throwingRunnableTypeToReplace = null;
		this.messageMovingToLastPosition = messageMovingToLastPosition;
		this.transformableInvocation = transformableInvocation;
	}

	JUnit4AssertMethodInvocationAnalysisResult(MethodInvocation methodInvocation,
			boolean messageMovingToLastPosition,
			boolean transformableInvocation) {
		this.methodInvocation = methodInvocation;
		this.methodName = methodInvocation.getName()
			.getIdentifier();
		this.deprecatedMethodNameReplacement = null;
		this.throwingRunnableTypeToReplace = null;
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

	public Optional<Type> getThrowingRunnableTypeToReplace() {
		return Optional.ofNullable(throwingRunnableTypeToReplace);
	}

	boolean isMessageMovingToLastPosition() {
		return messageMovingToLastPosition;
	}

	boolean isTransformableInvocation() {
		return transformableInvocation;
	}
}
