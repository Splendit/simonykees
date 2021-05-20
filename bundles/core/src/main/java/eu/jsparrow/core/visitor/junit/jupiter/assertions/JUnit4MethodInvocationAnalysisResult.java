package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Optional;

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
class JUnit4MethodInvocationAnalysisResult {

	private final MethodInvocation methodInvocation;
	private final String originalMethodName;
	private final String newMethodName;
	private final boolean messageMovingToLastPosition;
	private final boolean transformableInvocation;
	private Type throwingRunnableTypeToReplace;

	JUnit4MethodInvocationAnalysisResult(MethodInvocation methodInvocation, String newMethodName,
			boolean messageMovingToLastPosition, Type throwingRunnableTypeToReplace, boolean transformableInvocation) {
		this(methodInvocation, newMethodName, messageMovingToLastPosition, transformableInvocation);
		this.throwingRunnableTypeToReplace = throwingRunnableTypeToReplace;
	}

	JUnit4MethodInvocationAnalysisResult(MethodInvocation methodInvocation, String newMethodName,
			boolean messageMovingToLastPosition,
			boolean transformableInvocation) {
		this.methodInvocation = methodInvocation;
		this.originalMethodName = methodInvocation.getName()
			.getIdentifier();
		this.newMethodName = newMethodName;
		this.messageMovingToLastPosition = messageMovingToLastPosition;
		this.transformableInvocation = transformableInvocation;
	}

	MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	String getOriginalMethodName() {
		return originalMethodName;
	}

	String getNewMethodName() {
		return newMethodName;
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
