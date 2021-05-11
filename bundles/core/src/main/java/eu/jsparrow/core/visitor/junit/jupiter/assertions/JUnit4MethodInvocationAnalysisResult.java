package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;

public class JUnit4MethodInvocationAnalysisResult {

	private final MethodInvocation methodInvocation;
	private final IMethodBinding methodBinding;
	private final List<Expression> arguments;
	private AssumptionThatEveryItemNotNull assumptionThatEveryItemNotNull;
	private Type typeOfThrowingRunnableToReplace;

	JUnit4MethodInvocationAnalysisResult(MethodInvocation methodInvocation, IMethodBinding methodBinding,
			List<Expression> arguments) {
		this.methodInvocation = methodInvocation;
		this.methodBinding = methodBinding;
		this.arguments = arguments;
	}

	JUnit4MethodInvocationAnalysisResult(MethodInvocation methodInvocation, IMethodBinding methodBinding,
			List<Expression> arguments, AssumptionThatEveryItemNotNull assumptionThatEveryItemNotNull) {
		this(methodInvocation, methodBinding, arguments);
		this.assumptionThatEveryItemNotNull = assumptionThatEveryItemNotNull;
	}

	JUnit4MethodInvocationAnalysisResult(MethodInvocation methodInvocation, IMethodBinding methodBinding,
			List<Expression> arguments, Type typeOfThrowingRunnableToReplace) {
		this(methodInvocation, methodBinding, arguments);
		this.typeOfThrowingRunnableToReplace = typeOfThrowingRunnableToReplace;
	}

	MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	IMethodBinding getMethodBinding() {
		return methodBinding;
	}

	List<Expression> getArguments() {
		return arguments;
	}

	Optional<AssumptionThatEveryItemNotNull> getAssumptionThatEveryItemNotNull() {
		return Optional.ofNullable(assumptionThatEveryItemNotNull);
	}

	Optional<Type> getTypeOfThrowingRunnableToReplace() {
		return Optional.ofNullable(typeOfThrowingRunnableToReplace);
	}
}
