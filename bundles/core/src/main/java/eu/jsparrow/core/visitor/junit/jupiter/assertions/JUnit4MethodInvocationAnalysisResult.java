package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class JUnit4MethodInvocationAnalysisResult {

	private final MethodInvocation methodInvocation;
	private final IMethodBinding methodBinding;
	private final List<Expression> arguments;
	private String methodNameReplacement;
	private final boolean transformable;

	JUnit4MethodInvocationAnalysisResult(MethodInvocation methodInvocation, IMethodBinding methodBinding,
			List<Expression> arguments, String methodNameReplacement, boolean isTransformable) {
		this(methodInvocation, methodBinding, arguments, isTransformable);
		this.methodNameReplacement = methodNameReplacement;
	}

	JUnit4MethodInvocationAnalysisResult(MethodInvocation methodInvocation, IMethodBinding methodBinding,
			List<Expression> arguments, boolean isTransformable) {
		this.methodInvocation = methodInvocation;
		this.methodBinding = methodBinding;
		this.arguments = arguments;
		this.transformable = isTransformable;
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

	String getNewMethodName() {
		return methodNameReplacement != null ? methodNameReplacement : methodBinding.getName();
	}
}
