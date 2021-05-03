package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class SupportedJUnit4InvocationData {

	private final MethodInvocation methodInvocation;
	private final IMethodBinding methodBinding;
	private final List<Expression> arguments;
	private final boolean transformable;

	SupportedJUnit4InvocationData(MethodInvocation methodInvocation, IMethodBinding methodBinding,
			List<Expression> arguments, boolean isTransformable) {
		this.methodInvocation = methodInvocation;
		this.methodBinding = methodBinding;
		this.arguments = arguments;
		this.transformable = isTransformable;
	}

	MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	public IMethodBinding getMethodBinding() {
		return methodBinding;
	}

	public List<Expression> getArguments() {
		return arguments;
	}

	public boolean isTransformable() {
		return transformable;
	}

}
