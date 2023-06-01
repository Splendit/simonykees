package eu.jsparrow.core.visitor.impl.ternary;

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
		Runnable transformer = findTransformer(ifStatement).orElse(null);
		if (transformer != null) {
			transformer.run();
			onRewrite();
			return false;
		}
		return true;
	}

	private Optional<Runnable> findTransformer(IfStatement ifStatement) {
		Statement unwrappedThenStatement = unwrapToSingleStatement(ifStatement.getThenStatement()).orElse(null);
		if (unwrappedThenStatement == null) {
			return Optional.empty();
		}
		if (unwrappedThenStatement.getNodeType() == ASTNode.RETURN_STATEMENT) {
			ReturnStatement returnStatementWhenTrue = (ReturnStatement) unwrappedThenStatement;
			return findTransformerToReturnOfTernaryExpression(returnStatementWhenTrue, ifStatement);
		}
		return findTransformerToAssignmentOfTernaryExpression(unwrappedThenStatement, ifStatement);
	}

	private Optional<Runnable> findTransformerToReturnOfTernaryExpression(ReturnStatement returnStatementWhenTrue,
			IfStatement ifStatement) {
		Expression ifCondition = ifStatement.getExpression();
		Expression expressionWhenTrue = returnStatementWhenTrue.getExpression();
		Expression expressionWhenFalse;
		ReturnStatement returnStatementToRemove;
		Statement elseStatement = ifStatement.getElseStatement();

		if (elseStatement == null) {
			returnStatementToRemove = findReturnStatementFollowingIf(ifStatement).orElse(null);
			if (returnStatementToRemove == null) {
				return Optional.empty();
			}
			expressionWhenFalse = returnStatementToRemove.getExpression();
		} else {
			returnStatementToRemove = null;
			Statement unwrappedElseStatement = unwrapToSingleStatement(elseStatement).orElse(null);
			if (unwrappedElseStatement == null) {
				return Optional.empty();
			}
			if (unwrappedElseStatement.getNodeType() != ASTNode.RETURN_STATEMENT) {
				return Optional.empty();
			}
			expressionWhenFalse = ((ReturnStatement) unwrappedElseStatement).getExpression();
		}
		if (!checkTypes(expressionWhenTrue, expressionWhenFalse)) {
			return Optional.empty();
		}
		if (returnStatementToRemove != null) {
			return Optional.of(() -> {
				ConditionalExpression conditionalExpression = newConditionalExpression(ifCondition,
						expressionWhenTrue, expressionWhenFalse);
				replaceIfStatementByReturnTernary(ifStatement, conditionalExpression, returnStatementToRemove);
			});
		}
		return Optional.of(() -> {
			ConditionalExpression conditionalExpression = newConditionalExpression(ifCondition,
					expressionWhenTrue, expressionWhenFalse);
			replaceIfStatementByReturnTernary(ifStatement, conditionalExpression);
		});
	}

	private Optional<Runnable> findTransformerToAssignmentOfTernaryExpression(
			Statement unwrappedThenStatement,
			IfStatement ifStatement) {

		Statement elseStatement = ifStatement.getElseStatement();
		if (elseStatement == null) {
			return Optional.empty();
		}

		Statement unwrappedElseStatement = unwrapToSingleStatement(elseStatement).orElse(null);
		if (unwrappedElseStatement == null) {
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
		if (!checkTypes(expressionWhenTrue, expressionWhenFalse)) {
			return Optional.empty();
		}

		Expression ifCondition = ifStatement.getExpression();
		return Optional.of(() -> {
			ConditionalExpression conditionalExpression = newConditionalExpression(ifCondition, expressionWhenTrue,
					expressionWhenFalse);
			replaceIfStatementByAssignmentOfTernary(ifStatement, leftHandSideWhenTrue, conditionalExpression);
		});
	}

	private Optional<Statement> unwrapToSingleStatement(Statement statement) {
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

	private void replaceIfStatementByReturnTernary(IfStatement ifStatement,
			ConditionalExpression conditionalExpression) {
		AST ast = astRewrite.getAST();
		ReturnStatement returnStatement = ast.newReturnStatement();
		returnStatement.setExpression(conditionalExpression);
		astRewrite.replace(ifStatement, returnStatement, null);
	}

	private void replaceIfStatementByReturnTernary(IfStatement ifStatement,
			ConditionalExpression conditionalExpression, ReturnStatement returnStatementToRemove) {
		replaceIfStatementByReturnTernary(ifStatement, conditionalExpression);
		astRewrite.remove(returnStatementToRemove, null);
	}

	private void replaceIfStatementByAssignmentOfTernary(IfStatement ifStatement, Expression leftHandSide,
			ConditionalExpression conditionalExpression) {
		AST ast = astRewrite.getAST();
		Assignment assignment = ast.newAssignment();
		ASTNode leftHandSideCopyTarget = astRewrite.createCopyTarget(leftHandSide);
		assignment.setLeftHandSide((Expression) leftHandSideCopyTarget);
		assignment.setRightHandSide(conditionalExpression);
		astRewrite.replace(ifStatement, ast.newExpressionStatement(assignment), null);
	}

	private ConditionalExpression newConditionalExpression(Expression condition, Expression thenExpression,
			Expression elseExpression) {
		AST ast = astRewrite.getAST();
		ConditionalExpression conditionalExpression = ast.newConditionalExpression();
		ASTNode conditionCopyTarget = astRewrite.createCopyTarget(condition);
		conditionalExpression.setExpression((Expression) conditionCopyTarget);
		ASTNode thenExpressionCopyTarget = astRewrite.createCopyTarget(thenExpression);
		conditionalExpression.setThenExpression((Expression) thenExpressionCopyTarget);
		ASTNode elseExpressionCopyTarget = astRewrite.createCopyTarget(elseExpression);
		conditionalExpression.setElseExpression((Expression) elseExpressionCopyTarget);
		return conditionalExpression;
	}
}