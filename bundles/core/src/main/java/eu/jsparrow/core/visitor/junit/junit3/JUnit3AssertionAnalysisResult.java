package eu.jsparrow.core.visitor.junit.junit3;

import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class JUnit3AssertionAnalysisResult {

	private final MethodInvocation methodInvocation;
	private final String classDeclaringMethodReplacement;
	private Expression messageMovingToLastPosition;

	JUnit3AssertionAnalysisResult(MethodInvocation methodInvocation, Expression messageMovingToLastPosition,
			String classDeclaringMethodReplacement) {
		this(methodInvocation, classDeclaringMethodReplacement);
		this.messageMovingToLastPosition = messageMovingToLastPosition;
	}

	JUnit3AssertionAnalysisResult(MethodInvocation methodInvocation, String classDeclaringMethodReplacement) {
		this.methodInvocation = methodInvocation;
		this.classDeclaringMethodReplacement = classDeclaringMethodReplacement;
	}

	MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	Optional<Expression> getMessageMovingToLastPosition() {
		return Optional.ofNullable(messageMovingToLastPosition);
	}

	String getClassDeclaringMethodReplacement() {
		return classDeclaringMethodReplacement;
	}

}