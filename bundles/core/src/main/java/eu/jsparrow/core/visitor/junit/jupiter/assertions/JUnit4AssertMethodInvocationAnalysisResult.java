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
	private final boolean messageAsFirstParameter;
	private final boolean transformableInvocation;
	
	JUnit4AssertMethodInvocationAnalysisResult(MethodInvocation methodInvocation,
			Type throwingRunnableTypeToReplace, boolean messageAsFirstParameter,
			boolean transformableInvocation) {
		this.methodInvocation = methodInvocation;
		this.methodName = methodInvocation.getName()
			.getIdentifier();
		this.deprecatedMethodNameReplacement = null;
		this.throwingRunnableTypeToReplace = throwingRunnableTypeToReplace;
		this.messageAsFirstParameter = messageAsFirstParameter;
		this.transformableInvocation = transformableInvocation;
	}

	JUnit4AssertMethodInvocationAnalysisResult(MethodInvocation methodInvocation,
			String deprecatedMethodNameReplacement, boolean messageAsFirstParameter,
			boolean transformableInvocation) {
		this.methodInvocation = methodInvocation;
		this.methodName = methodInvocation.getName()
			.getIdentifier();
		this.deprecatedMethodNameReplacement = deprecatedMethodNameReplacement;
		this.throwingRunnableTypeToReplace = null;
		this.messageAsFirstParameter = messageAsFirstParameter;
		this.transformableInvocation = transformableInvocation;
	}

	JUnit4AssertMethodInvocationAnalysisResult(MethodInvocation methodInvocation,
			boolean messageAsFirstParameter,
			boolean transformableInvocation) {
		this.methodInvocation = methodInvocation;
		this.methodName = methodInvocation.getName()
			.getIdentifier();
		this.deprecatedMethodNameReplacement = null;
		this.throwingRunnableTypeToReplace = null;
		this.messageAsFirstParameter = messageAsFirstParameter;
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

	boolean isMessageAsFirstParameter() {
		return messageAsFirstParameter;
	}

	boolean isTransformableInvocation() {
		return transformableInvocation;
	}
}
