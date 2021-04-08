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
	private final String originalMethodName;
	private final String newMethodName;
	private final boolean messageMovingToLastPosition;
	private final boolean transformableInvocation;
	private Type throwingRunnableTypeToReplace;

	JUnit4AssertMethodInvocationAnalysisResult(MethodInvocation methodInvocation, String newMethodName,
			boolean messageMovingToLastPosition, Type throwingRunnableTypeToReplace) {
		this(methodInvocation, newMethodName, messageMovingToLastPosition, true);
		this.throwingRunnableTypeToReplace = throwingRunnableTypeToReplace;
	}

	JUnit4AssertMethodInvocationAnalysisResult(MethodInvocation methodInvocation, String newMethodName,
			boolean messageMovingToLastPosition) {
		this(methodInvocation, newMethodName, messageMovingToLastPosition, true);
	}

	JUnit4AssertMethodInvocationAnalysisResult(MethodInvocation methodInvocation) {
		this(methodInvocation,
				methodInvocation.getName()
					.getIdentifier(),
				false, false);
	}

	private JUnit4AssertMethodInvocationAnalysisResult(MethodInvocation methodInvocation, String newMethodName,
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

	public String getNewMethodName() {
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
