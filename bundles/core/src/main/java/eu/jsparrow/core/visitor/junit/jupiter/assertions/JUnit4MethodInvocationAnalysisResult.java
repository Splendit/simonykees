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
	private final boolean transformable;
	private AssertThatEveryItemNotNullAnalysisResult assertThatEveryItemNotNullData;
	private Type typeOfThrowingRunnableToReplace;

	JUnit4MethodInvocationAnalysisResult(MethodInvocation methodInvocation, IMethodBinding methodBinding,
			List<Expression> arguments, boolean isTransformable) {
		this.methodInvocation = methodInvocation;
		this.methodBinding = methodBinding;
		this.arguments = arguments;
		this.transformable = isTransformable;
	}

	JUnit4MethodInvocationAnalysisResult(MethodInvocation methodInvocation, IMethodBinding methodBinding,
			List<Expression> arguments, AssertThatEveryItemNotNullAnalysisResult assertThatEveryItemNotNullData) {
		this(methodInvocation, methodBinding, arguments, true);
		this.assertThatEveryItemNotNullData = assertThatEveryItemNotNullData;
	}

	JUnit4MethodInvocationAnalysisResult(MethodInvocation methodInvocation, IMethodBinding methodBinding,
			List<Expression> arguments, Type typeOfThrowingRunnableToReplace) {
		this(methodInvocation, methodBinding, arguments, true);
		this.typeOfThrowingRunnableToReplace = typeOfThrowingRunnableToReplace;
	}

	protected JUnit4MethodInvocationAnalysisResult(JUnit4MethodInvocationAnalysisResult other) {
		this.methodInvocation = other.methodInvocation;
		this.methodBinding = other.methodBinding;
		this.arguments = other.arguments;
		this.transformable = other.transformable;
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

	boolean isTransformable() {
		return transformable;
	}

	Optional<AssertThatEveryItemNotNullAnalysisResult> getAssertThatEveryItemNotNullData() {
		return Optional.ofNullable(assertThatEveryItemNotNullData);
	}

	Optional<Type> getTypeOfThrowingRunnableToReplace() {
		return Optional.ofNullable(typeOfThrowingRunnableToReplace);
	}
}
