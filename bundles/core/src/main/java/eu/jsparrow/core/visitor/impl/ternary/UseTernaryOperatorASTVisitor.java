package eu.jsparrow.core.visitor.impl.ternary;

import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.markers.common.UseTernaryOperatorEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.VariableDeclarationBeforeStatement;
import eu.jsparrow.rules.common.util.VariableWithoutSideEffect;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

/**
 * A visitor that searches for nested {@link IfStatement} and collapses them to
 * a single one if possible. Introduces a boolean variable to store the
 * condition if it contains more than 2 components.
 * 
 * @since 4.18.0
 *
 */
public class UseTernaryOperatorASTVisitor extends AbstractASTRewriteASTVisitor implements UseTernaryOperatorEvent {

	private static final int MAX_COMPLEXITY = 20;
	private final ASTMatcher matcher = new ASTMatcher();

	@Override
	public boolean visit(IfStatement ifStatement) {
		Runnable transformer = findTransformer(ifStatement).orElse(null);
		if (transformer != null) {
			transformer.run();
			onRewrite();
			addMarkerEvent(ifStatement);
			return false;
		}
		return true;
	}

	private Optional<Runnable> findTransformer(IfStatement ifStatement) {
		if (ifStatement.getLocationInParent() == IfStatement.ELSE_STATEMENT_PROPERTY) {
			return Optional.empty();
		}
		Statement thenStatement = ifStatement.getThenStatement();
		Assignment assignmentWhenTrue = extractSingleAssignment(thenStatement).orElse(null);
		if (assignmentWhenTrue != null) {
			return findTransformer(ifStatement, assignmentWhenTrue);
		}

		ReturnStatement returnStatementWhenTrue = extractSingleReturnStatement(thenStatement).orElse(null);
		if (returnStatementWhenTrue != null) {
			return findTransformer(ifStatement, returnStatementWhenTrue);
		}

		return Optional.empty();
	}

	private Optional<Runnable> findTransformer(IfStatement ifStatement, Assignment assignmentWhenTrue) {

		Statement elseStatement = ifStatement.getElseStatement();
		if (elseStatement == null) {
			return Optional.empty();
		}

		Assignment assignmentWhenFalse = extractSingleAssignment(elseStatement).orElse(null);
		if (assignmentWhenFalse == null) {
			return Optional.empty();
		}

		Operator operatorWhenTrue = assignmentWhenTrue.getOperator();
		if (operatorWhenTrue != assignmentWhenFalse.getOperator()) {
			return Optional.empty();
		}

		Expression leftHandSideWhenTrue = assignmentWhenTrue.getLeftHandSide();
		Expression leftHandSideWhenFalse = assignmentWhenFalse.getLeftHandSide();
		if (!leftHandSideWhenTrue.subtreeMatch(matcher, leftHandSideWhenFalse)) {
			return Optional.empty();
		}

		if (!VariableWithoutSideEffect.isVariableWithoutSideEffect(leftHandSideWhenTrue)) {
			return Optional.empty();
		}

		Expression expressionWhenTrue = assignmentWhenTrue.getRightHandSide();
		Expression expressionWhenFalse = assignmentWhenFalse.getRightHandSide();
		Supplier<ConditionalExpression> supplier = findNewConditionalExpressionSupplier(ifStatement, expressionWhenTrue,
				expressionWhenFalse).orElse(null);
		if (supplier == null) {
			return Optional.empty();
		}

		if (leftHandSideWhenTrue.getNodeType() == ASTNode.SIMPLE_NAME
				&& operatorWhenTrue == Assignment.Operator.ASSIGN) {
			SimpleName leftHandSideSimpleName = (SimpleName) leftHandSideWhenTrue;
			VariableDeclarationFragment declarationBeforeIf = VariableDeclarationBeforeStatement
				.findDeclaringFragment(leftHandSideSimpleName, ifStatement, getCompilationUnit())
				.orElse(null);

			if (declarationBeforeIf != null
					&& !isVariableUsedInIfCondition(leftHandSideSimpleName, ifStatement.getExpression())) {
				return Optional
					.of(() -> replaceByInitializationWithTernary(ifStatement, declarationBeforeIf, supplier));
			}
		}
		return Optional.of(() -> replaceIfStatementByAssignmentOfTernary(ifStatement, leftHandSideWhenTrue,
				operatorWhenTrue, supplier));
	}

	private Optional<Runnable> findTransformer(
			IfStatement ifStatement, ReturnStatement returnStatementWhenTrue) {
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
			expressionWhenFalse = extractSingleReturnStatement(elseStatement)
				.map(ReturnStatement::getExpression)
				.orElse(null);
			if (expressionWhenFalse == null) {
				return Optional.empty();
			}
		}
		Supplier<ConditionalExpression> supplier = findNewConditionalExpressionSupplier(ifStatement, expressionWhenTrue,
				expressionWhenFalse).orElse(null);

		if (supplier == null) {
			return Optional.empty();
		}

		if (returnStatementToRemove != null) {
			return Optional.of(() -> replaceIfStatementByReturnTernary(ifStatement, supplier, returnStatementToRemove));
		}
		return Optional.of(() -> replaceIfStatementByReturnTernary(ifStatement, supplier));
	}

	private Optional<Assignment> extractSingleAssignment(Statement statement) {
		return unwrapToSingleStatement(statement, ExpressionStatement.class)
			.map(ExpressionStatement::getExpression)
			.filter(Assignment.class::isInstance)
			.map(Assignment.class::cast);
	}

	private Optional<ReturnStatement> extractSingleReturnStatement(Statement statement) {
		return unwrapToSingleStatement(statement, ReturnStatement.class);
	}

	private <T extends Statement> Optional<T> unwrapToSingleStatement(Statement statement, Class<T> type) {
		if (statement.getNodeType() == ASTNode.BLOCK) {
			Block block = (Block) statement;
			return ASTNodeUtil.findSingleBlockStatement(block, type);
		}
		return Optional.of(statement)
			.filter(type::isInstance)
			.map(type::cast);
	}

	private Optional<ReturnStatement> findReturnStatementFollowingIf(IfStatement ifStatement) {
		if (ifStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return Optional.empty();
		}
		Block block = (Block) ifStatement.getParent();
		return ASTNodeUtil
			.findListElementAfter(block.statements(), ifStatement, ReturnStatement.class);
	}

	private Optional<Supplier<ConditionalExpression>> findNewConditionalExpressionSupplier(IfStatement ifStatement,
			Expression expressionWhenTrue, Expression expressionWhenFalse) {
		Expression ifCondition = ifStatement.getExpression();

		boolean supportedOperands = isSupportedTernaryOperand(ifCondition)
				&& isSupportedTernaryOperand(expressionWhenTrue)
				&& isSupportedTernaryOperand(expressionWhenFalse);

		if (!supportedOperands) {
			return Optional.empty();
		}

		if (!checkTypes(expressionWhenTrue, expressionWhenFalse)) {
			return Optional.empty();
		}

		return Optional.of(() -> newConditionalExpression(ifCondition, expressionWhenTrue, expressionWhenFalse));
	}

	static boolean isSupportedTernaryOperand(Expression expression) {
		SupportedExpressionComplexityVisitor lengthVisitor = new SupportedExpressionComplexityVisitor();
		expression.accept(lengthVisitor);
		return lengthVisitor.isSupportedExpression() && lengthVisitor.getTotalComplexity() <= MAX_COMPLEXITY;
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

		ITypeBinding typeBindingWhenTrue = expressionWhenTrue.resolveTypeBinding();
		if (typeBindingWhenTrue == null) {
			return false;
		}
		ITypeBinding typeBindingWhenFalse = expressionWhenFalse.resolveTypeBinding();
		if (typeBindingWhenFalse == null) {
			return false;
		}

		boolean primitiveWhenTrue = typeBindingWhenTrue.isPrimitive();
		boolean primitiveWhenFalse = typeBindingWhenFalse.isPrimitive();

		return primitiveWhenTrue == primitiveWhenFalse;
	}

	private boolean isVariableUsedInIfCondition(SimpleName variableName, Expression ifCondition) {
		LocalVariableUsagesVisitor visitor = new LocalVariableUsagesVisitor(variableName);
		ifCondition.accept(visitor);
		return !visitor.getUsages()
			.isEmpty();
	}

	private void replaceIfStatementByReturnTernary(IfStatement ifStatement,
			Supplier<ConditionalExpression> conditionalExpressionSupplier) {
		AST ast = astRewrite.getAST();
		ReturnStatement returnStatement = ast.newReturnStatement();
		returnStatement.setExpression(conditionalExpressionSupplier.get());
		astRewrite.replace(ifStatement, returnStatement, null);
	}

	private void replaceIfStatementByReturnTernary(IfStatement ifStatement,
			Supplier<ConditionalExpression> conditionalExpressionSupplier, ReturnStatement returnStatementToRemove) {
		replaceIfStatementByReturnTernary(ifStatement, conditionalExpressionSupplier);
		astRewrite.remove(returnStatementToRemove, null);
	}

	private void replaceIfStatementByAssignmentOfTernary(IfStatement ifStatement, Expression leftHandSide,
			Assignment.Operator assignmentOperator,
			Supplier<ConditionalExpression> conditionalExpressionSupplier) {
		AST ast = astRewrite.getAST();
		Assignment assignment = ast.newAssignment();
		ASTNode leftHandSideCopyTarget = astRewrite.createCopyTarget(leftHandSide);
		assignment.setLeftHandSide((Expression) leftHandSideCopyTarget);
		ConditionalExpression conditionalExpression = conditionalExpressionSupplier.get();
		assignment.setRightHandSide(conditionalExpression);
		assignment.setOperator(assignmentOperator);
		astRewrite.replace(ifStatement, ast.newExpressionStatement(assignment), null);
	}

	private void replaceByInitializationWithTernary(IfStatement ifStatement, VariableDeclarationFragment fragment,
			Supplier<ConditionalExpression> conditionalExpressionSupplier) {
		ConditionalExpression conditionalExpression = conditionalExpressionSupplier.get();
		astRewrite.set(fragment, VariableDeclarationFragment.INITIALIZER_PROPERTY, conditionalExpression, null);
		astRewrite.remove(ifStatement, null);
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