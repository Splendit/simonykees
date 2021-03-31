package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Type;

class ThrowingRunnableArgumentAnalyzer {
	private Type localVariableTypeToReplace;

	boolean analyze(List<Expression> arguments) {
		int throwingRunnableArgumentIndex = arguments.size() - 1;
		Expression throwingRunnableArgument = arguments.get(throwingRunnableArgumentIndex);
		return throwingRunnableArgument.getNodeType() == ASTNode.LAMBDA_EXPRESSION;
	}

	Optional<Type> getLocalVariableTypeToReplace() {
		return Optional.ofNullable(localVariableTypeToReplace);
	}
}