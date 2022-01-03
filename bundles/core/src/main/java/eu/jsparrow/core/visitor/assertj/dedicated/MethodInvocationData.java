package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Type;

public class MethodInvocationData {

	private final String methodName;
	private List<Type> typeArguments;
	private List<Expression> arguments;
	private Expression expression;

	public MethodInvocationData(String methodName) {
		this.methodName = methodName;
	}

	public void setTypeArguments(List<Type> typeArguments) {
		this.typeArguments = typeArguments;
	}

	public void setArguments(List<Expression> arguments) {
		this.arguments = arguments;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	public String getMethodName() {
		return methodName;
	}

	public List<Type> getTypeArguments() {
		if (typeArguments == null) {
			return Collections.emptyList();
		}
		return typeArguments;
	}

	public List<Expression> getArguments() {
		if (arguments == null) {
			return Collections.emptyList();
		}
		return arguments;
	}

	public Optional<Expression> getExpression() {
		return Optional.ofNullable(expression);
	}
}
