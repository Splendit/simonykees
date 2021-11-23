package eu.jsparrow.core.visitor.assertj;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;

class InvocationChainElement {
	private final String methodName;
	private final List<Expression> arguments;

	InvocationChainElement(String methodName, List<Expression> arguments) {
		this.methodName = methodName;
		this.arguments = arguments;
	}

	String getMethodName() {
		return methodName;
	}

	List<Expression> getArguments() {
		return arguments;
	}
}
