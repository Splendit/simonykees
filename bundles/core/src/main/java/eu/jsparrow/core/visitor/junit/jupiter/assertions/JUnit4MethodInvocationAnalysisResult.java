package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;

public class JUnit4MethodInvocationAnalysisResult {

	private final MethodInvocation methodInvocation;
	private final String originalMethodName;
	private final String newMethodName;
	private final IMethodBinding methodBinding;
	private final List<Expression> arguments;
	private final AssumeNotNullWithNullableArray assumptionThatEveryItemNotNull;
	private final Type typeOfThrowingRunnableToReplace;

	JUnit4MethodInvocationAnalysisResult(MethodInvocation methodInvocation, IMethodBinding methodBinding,
			List<Expression> arguments, JUnit4InvocationReplacementAnalyzer analyzer) {
		this.originalMethodName = analyzer.getOriginalMethodName();
		this.newMethodName = analyzer.getNewMethodName();
		this.methodInvocation = methodInvocation;
		this.methodBinding = methodBinding;
		this.arguments = arguments;
		assumptionThatEveryItemNotNull = analyzer.getAssumeNotNullWithNullableArray()
			.orElse(null);
		typeOfThrowingRunnableToReplace = analyzer.getTypeOfThrowingRunnableToReplace()
			.orElse(null);
	}

	MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	public String getOriginalMethodName() {
		return originalMethodName;
	}

	public String getNewMethodName() {
		return newMethodName;
	}

	IMethodBinding getMethodBinding() {
		return methodBinding;
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
