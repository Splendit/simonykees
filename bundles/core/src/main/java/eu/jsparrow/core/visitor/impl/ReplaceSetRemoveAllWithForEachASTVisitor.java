package eu.jsparrow.core.visitor.impl;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.markers.common.ReplaceSetRemoveAllWithForEachEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * @since 4.13.0
 * 
 */
public class ReplaceSetRemoveAllWithForEachASTVisitor extends AbstractASTRewriteASTVisitor
		implements ReplaceSetRemoveAllWithForEachEvent {

	private static final String REMOVE_ALL = "removeAll"; //$NON-NLS-1$

	@Override
	public boolean visit(ExpressionStatement expressionStatement) {
		findSetRemoveAllReplacementData(expressionStatement).ifPresent(data -> transform(expressionStatement, data));
		return true;
	}

	private Optional<SetRemoveAllReplacementData> findSetRemoveAllReplacementData(
			ExpressionStatement expressionStatement) {
		if (expressionStatement.getExpression()
			.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Optional.empty();
		}
		MethodInvocation methodInvocation = (MethodInvocation) expressionStatement.getExpression();
		String methodName = methodInvocation.getName()
			.getIdentifier();
		if (!methodName.equals(REMOVE_ALL)) {
			return Optional.empty();
		}

		Expression removeAllInvocationExpression = methodInvocation.getExpression();
		if (removeAllInvocationExpression == null) {
			return Optional.empty();
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.size() != 1) {
			return Optional.empty();
		}
		Expression removeAllInvocationArgumemnt = arguments.get(0);

		return Optional
			.of(new SetRemoveAllReplacementData(removeAllInvocationExpression, removeAllInvocationArgumemnt));
	}

	private void transform(ExpressionStatement expressionStatement, SetRemoveAllReplacementData replacementData) {
		// This method is not implemented yet !!!
		expressionStatement.toString();
		replacementData.toString();
	}

	static class SetRemoveAllReplacementData {
		private final Expression setExpression;
		private final Expression removeAllArgument;

		public SetRemoveAllReplacementData(Expression setExpression, Expression removeAllArgument) {
			this.setExpression = setExpression;
			this.removeAllArgument = removeAllArgument;
		}

		public Expression getSetExpression() {
			return setExpression;
		}

		public Expression getRemoveAllArgument() {
			return removeAllArgument;
		}
	}
}