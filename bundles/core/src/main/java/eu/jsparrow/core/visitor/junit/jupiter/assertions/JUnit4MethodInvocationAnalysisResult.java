package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;

public class JUnit4MethodInvocationAnalysisResult {

	private final MethodInvocation methodInvocation;
	private final String originalMethodName;
	private final String newMethodName;
	private final Expression messageMovingToLastPosition;
	private final List<Expression> arguments;
	private final AssumeNotNullWithNullableArray assumptionThatEveryItemNotNull;
	private final Type typeOfThrowingRunnableToReplace;

	JUnit4MethodInvocationAnalysisResult(JUnit4InvocationReplacementAnalyzer analyzer) {
		methodInvocation = analyzer.getMethodInvocation();
		originalMethodName = analyzer.getOriginalMethodName();
		newMethodName = analyzer.getNewMethodName();
		messageMovingToLastPosition = analyzer.getMessageMovedToLastPosition()
			.orElse(null);
		arguments = analyzer.getArguments();
		assumptionThatEveryItemNotNull = analyzer.getAssumeNotNullWithNullableArray()
			.orElse(null);
		typeOfThrowingRunnableToReplace = analyzer.getTypeOfThrowingRunnableToReplace()
			.orElse(null);
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

	Optional<Expression> getMessageMovingToLastPosition() {
		return Optional.ofNullable(messageMovingToLastPosition);
	}

	List<Expression> getArguments() {
		return arguments;
	}

	Optional<AssumeNotNullWithNullableArray> getAssumptionThatEveryItemNotNull() {
		return Optional.ofNullable(assumptionThatEveryItemNotNull);
	}

	Optional<Type> getTypeOfThrowingRunnableToReplace() {
		return Optional.ofNullable(typeOfThrowingRunnableToReplace);
	}
}