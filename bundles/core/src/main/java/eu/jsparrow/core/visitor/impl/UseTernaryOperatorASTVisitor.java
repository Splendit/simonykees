package eu.jsparrow.core.visitor.impl;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * A visitor that searches for nested {@link IfStatement} and collapses them to
 * a single one if possible. Introduces a boolean variable to store the
 * condition if it contains more than 2 components.
 * 
 * @since 4.18.0
 *
 */
public class UseTernaryOperatorASTVisitor extends AbstractASTRewriteASTVisitor {

	private final ASTMatcher matcher = new ASTMatcher();

	@Override
	public boolean visit(IfStatement ifStatement) {
		TransformationData data = findTransformationData(ifStatement).orElse(null);
		if (data instanceof ReturnTernaryExpressionData) {
			replaceIfStatementByTernary(ifStatement, (ReturnTernaryExpressionData) data);
			return false;
		}
		if (data instanceof AssignTernaryExpressionData) {
			replaceIfStatementByTernary(ifStatement, (AssignTernaryExpressionData) data);
			return false;
		}
		return true;

	}

	Optional<TransformationData> findTransformationData(IfStatement ifStatement) {

		Statement unwrappedThenStatement = unwrapToSingleStatement(ifStatement.getThenStatement()).orElse(null);
		if (unwrappedThenStatement == null) {
			return Optional.empty();
		}
		Statement elseStatement = ifStatement.getElseStatement();

		if (elseStatement == null) {
			if (unwrappedThenStatement.getNodeType() != ASTNode.RETURN_STATEMENT) {
				return Optional.empty();
			}
			ReturnStatement returnStatementWhenFalse = findReturnStatementFollowingIf(ifStatement).orElse(null);
			if (returnStatementWhenFalse == null) {
				return Optional.empty();
			}
			ReturnStatement returnStatementWhenTrue = (ReturnStatement) unwrappedThenStatement;

			Expression expressionWhenTrue = returnStatementWhenTrue.getExpression();
			Expression expressionWhenFalse = returnStatementWhenFalse.getExpression();
			if (checkTypes(expressionWhenTrue, expressionWhenFalse)) {
				Expression ifCondition = ifStatement.getExpression();
				return Optional
					.of(new ReturnTernaryExpressionData(ifCondition, expressionWhenTrue, expressionWhenFalse,
							returnStatementWhenFalse));
			}
			return Optional.empty();
		}

		Statement unwrappedElseStatement = unwrapToSingleStatement(elseStatement).orElse(null);
		if (unwrappedElseStatement == null) {
			return Optional.empty();
		}

		if (unwrappedThenStatement.getNodeType() == ASTNode.RETURN_STATEMENT
				&& unwrappedElseStatement.getNodeType() == ASTNode.RETURN_STATEMENT) {

			ReturnStatement returnStatementWhenTrue = (ReturnStatement) unwrappedThenStatement;
			ReturnStatement returnStatementWhenFalse = (ReturnStatement) unwrappedElseStatement;

			Expression expressionWhenTrue = returnStatementWhenTrue.getExpression();
			Expression expressionWhenFalse = returnStatementWhenFalse.getExpression();
			if (checkTypes(expressionWhenTrue, expressionWhenFalse)) {
				Expression ifCondition = ifStatement.getExpression();
				return Optional
					.of(new ReturnTernaryExpressionData(ifCondition, expressionWhenTrue, expressionWhenFalse));
			}
			return Optional.empty();
		}

		Assignment assignmentWhenTrue = extractAssignment(unwrappedThenStatement).orElse(null);
		if (assignmentWhenTrue == null) {
			return Optional.empty();
		}

		Assignment assignmentWhenFalse = extractAssignment(unwrappedElseStatement).orElse(null);
		if (assignmentWhenFalse == null) {
			return Optional.empty();
		}
		Expression leftHandSideWhenTrue = assignmentWhenTrue.getLeftHandSide();
		Expression leftHandSideWhenFalse = assignmentWhenFalse.getLeftHandSide();
		if (!leftHandSideWhenTrue.subtreeMatch(matcher, leftHandSideWhenFalse)) {
			return Optional.empty();
		}
		if (isVariableWithSideEffect(leftHandSideWhenTrue)) {
			return Optional.empty();
		}
		Expression expressionWhenTrue = assignmentWhenTrue.getRightHandSide();
		Expression expressionWhenFalse = assignmentWhenFalse.getRightHandSide();
		if (checkTypes(expressionWhenTrue, expressionWhenFalse)) {
			Expression ifCondition = ifStatement.getExpression();
			return Optional
				.of(new AssignTernaryExpressionData(leftHandSideWhenTrue,
						ifCondition, expressionWhenTrue, expressionWhenFalse));
		}
		return Optional.empty();
	}

	Optional<Statement> unwrapToSingleStatement(Statement statement) {
		if (statement.getNodeType() == ASTNode.BLOCK) {
			Block block = (Block) statement;
			if (block.statements()
				.size() != 1) {
				return Optional.empty();
			}
			return Optional.of((Statement) block.statements()
				.get(0));

		}
		return Optional.of(statement);
	}

	private Optional<ReturnStatement> findReturnStatementFollowingIf(IfStatement ifStatement) {
		if (ifStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return Optional.empty();
		}
		Block block = (Block) ifStatement.getParent();
		List<Statement> blockStatements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
		int ifStatementIndex = blockStatements.indexOf(ifStatement);
		int followingStatementIndex = ifStatementIndex + 1;
		if (followingStatementIndex >= blockStatements.size()) {
			return Optional.empty();
		}
		Statement statementFollowingIf = blockStatements.get(followingStatementIndex);
		if (statementFollowingIf.getNodeType() != ASTNode.RETURN_STATEMENT) {
			return Optional.empty();
		}
		return Optional.of((ReturnStatement) statementFollowingIf);
	}

	private Optional<Assignment> extractAssignment(Statement statement) {
		if (statement.getNodeType() != ASTNode.EXPRESSION_STATEMENT) {
			return Optional.empty();
		}
		Expression expression = ((ExpressionStatement) statement).getExpression();
		if (expression.getNodeType() != ASTNode.ASSIGNMENT) {
			return Optional.empty();
		}

		return Optional.of((Assignment) expression);
	}

	private boolean isVariableWithSideEffect(Expression expression) {
		// TODO Auto-generated method stub
		// use VariableWithoutSideEffect
		return false;
	}

	/**
	 * 
	 * @return true if either both expressions have a primitive type or both
	 *         expressions have a reference type, false if one of the
	 *         expressions is a primitive expression and the other is a
	 *         reference expression, and also false if no valid type binding can
	 *         be found.
	 */
	private boolean checkTypes(Expression expressionWhenTrue, Expression expressionWhenFalse) {
		// TODO Auto-generated method stub
		return true;
	}

	private void replaceIfStatementByTernary(IfStatement ifStatement, ReturnTernaryExpressionData data) {

		ConditionalExpression conditionalExpression = createConditionalExpression(data);

		AST ast = astRewrite.getAST();
		ReturnStatement returnStatement = ast.newReturnStatement();
		returnStatement.setExpression(conditionalExpression);
		astRewrite.replace(ifStatement, returnStatement, null);
		data.getReturnStatementToRemove()
			.ifPresent(returnStatementToRemove -> astRewrite.remove(returnStatementToRemove, null));

		onRewrite();

	}

	private void replaceIfStatementByTernary(IfStatement ifStatement, AssignTernaryExpressionData data) {
		ConditionalExpression conditionalExpression = createConditionalExpression(data);
		AST ast = astRewrite.getAST();
		Assignment assignment = ast.newAssignment();
		ASTNode leftHandSideCopyTarget = astRewrite.createCopyTarget(data.getAssignmentLHS());
		assignment.setLeftHandSide((Expression) leftHandSideCopyTarget);
		assignment.setRightHandSide(conditionalExpression);
		astRewrite.replace(ifStatement, ast.newExpressionStatement(assignment), null);
		onRewrite();
	}

	private ConditionalExpression createConditionalExpression(TransformationData data) {
		AST ast = astRewrite.getAST();
		ConditionalExpression conditionalExpression = ast.newConditionalExpression();
		ASTNode expressionCopyTarget = astRewrite.createCopyTarget(data.getCondition());
		conditionalExpression.setExpression((Expression) expressionCopyTarget);
		ASTNode thenExpressionCopyTarget = astRewrite.createCopyTarget(data.getResultIf());
		conditionalExpression.setThenExpression((Expression) thenExpressionCopyTarget);
		ASTNode elseExpressionCopyTarget = astRewrite.createCopyTarget(data.getResultElse());
		conditionalExpression.setElseExpression((Expression) elseExpressionCopyTarget);
		return conditionalExpression;
	}

	abstract class TransformationData {
		private final Expression condition;
		// Problems can arise if one expression is primitive and the other is a
		// reference type, for example int and Integer
		// say, in cases where there is a boxing and unboxing
		private final Expression resultIf;
		private final Expression resultElse;

		protected TransformationData(Expression condition, Expression resultIf, Expression resultElse) {
			this.condition = condition;
			this.resultIf = resultIf;
			this.resultElse = resultElse;
		}

		Expression getCondition() {
			return condition;
		}

		Expression getResultIf() {
			return resultIf;
		}

		Expression getResultElse() {
			return resultElse;
		}
	}

	class AssignTernaryExpressionData extends TransformationData {
		private final Expression assignmentLHS;

		AssignTernaryExpressionData(Expression assignmentLHS, Expression condition, Expression resultIf,
				Expression resultElse) {
			super(condition, resultIf, resultElse);
			this.assignmentLHS = assignmentLHS;
		}

		Expression getAssignmentLHS() {
			return assignmentLHS;
		}

	}

	class ReturnTernaryExpressionData extends TransformationData {
		private ReturnStatement returnStatementToRemove;

		ReturnTernaryExpressionData(Expression condition, Expression resultIf, Expression resultElse,
				ReturnStatement returnStatementToRemove) {
			super(condition, resultIf, resultElse);
			this.returnStatementToRemove = returnStatementToRemove;
		}

		protected ReturnTernaryExpressionData(Expression condition, Expression resultIf, Expression resultElse) {
			super(condition, resultIf, resultElse);
		}

		Optional<ReturnStatement> getReturnStatementToRemove() {
			return Optional.ofNullable(returnStatementToRemove);
		}
	}
}