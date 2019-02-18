package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.visitor.sub.LiveVariableScope;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.OperatorUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * @since 3.2.0
 *
 */
public class CollapseIfStatementsASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String CONDITION_NAME = "condition"; //$NON-NLS-1$

	private LiveVariableScope aliveVariableScope = new LiveVariableScope();

	@Override
	public boolean visit(IfStatement ifStatement) {

		Statement elseStatement = ifStatement.getElseStatement();
		if (elseStatement != null) {
			return true;
		}

		Statement thenStatement = ifStatement.getThenStatement();
		List<IfStatement> innerifStatementStatements = findInnerIfStatements(thenStatement);
		if (innerifStatementStatements.isEmpty()) {
			return true;
		}

		IfStatement innestIf = innerifStatementStatements.get(innerifStatementStatements.size() - 1);
		Statement newBodyStatement = innestIf.getThenStatement();
		Expression conditionConjunction = createConditionConjunction(ifStatement, innerifStatementStatements);

		if (doCreateLocalVariable(ifStatement, innerifStatementStatements)) {
			ASTNode enclosingScope = this.aliveVariableScope.findEnclosingScope(ifStatement)
				.orElse(null);
			aliveVariableScope.lazyLoadScopeNames(enclosingScope);
			String conditionName = createConditionName();
			AST ast = ifStatement.getAST();
			VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
			fragment.setName(ast.newSimpleName(conditionName));
			fragment.setInitializer(conditionConjunction);
			VariableDeclarationStatement conditionDeclaration = ast.newVariableDeclarationStatement(fragment);
			conditionDeclaration.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN));

			Block block = ASTNodeUtil.getSpecificAncestor(ifStatement, Block.class);
			ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
			int positionInBlock = findPositionInBlock(block, ifStatement);
			positionInBlock = positionInBlock >= 0 ? positionInBlock : 0;
			listRewrite.insertAt(conditionDeclaration, positionInBlock, null);
			SimpleName newIfExpression = ast.newSimpleName(conditionName);
			astRewrite.replace(ifStatement.getExpression(), newIfExpression, null);
			aliveVariableScope.addName(enclosingScope, conditionName);

		} else {
			astRewrite.replace(ifStatement.getExpression(), conditionConjunction, null);
		}

		astRewrite.replace(ifStatement.getThenStatement(), (Block) astRewrite.createCopyTarget(newBodyStatement), null);
		onRewrite();

		return true;
	}

	private boolean doCreateLocalVariable(IfStatement ifStatement, List<IfStatement> innerifStatementStatements) {
		if (innerifStatementStatements.size() > 1) {
			return true;
		}
		if (!OperatorUtil.isSimpleExpression(ifStatement.getExpression())) {
			return true;
		}
		return !innerifStatementStatements.stream()
			.map(IfStatement::getExpression)
			.allMatch(OperatorUtil::isSimpleExpression);
	}

	private int findPositionInBlock(Block block, IfStatement ifStatement) {
		List<Statement> statements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
		Statement statement = ifStatement;
		int index = -1;
		while (statement != null && (index = statements.indexOf(statement)) < 0) {
			statement = ASTNodeUtil.getSpecificAncestor(statement, Statement.class);
		}
		return index;
	}

	@Override
	public void endVisit(TypeDeclaration typeDeclaration) {
		aliveVariableScope.clearLocalVariablesScope(typeDeclaration);
		aliveVariableScope.clearFieldScope(typeDeclaration);
	}

	@Override
	public void endVisit(MethodDeclaration methodDeclaration) {
		aliveVariableScope.clearLocalVariablesScope(methodDeclaration);
	}

	@Override
	public void endVisit(FieldDeclaration fieldDeclaration) {
		aliveVariableScope.clearLocalVariablesScope(fieldDeclaration);
	}

	@Override
	public void endVisit(Initializer initializer) {
		aliveVariableScope.clearLocalVariablesScope(initializer);
	}

	private String createConditionName() {
		String name = CONDITION_NAME;
		int suffix = 1;
		while (aliveVariableScope.isInScope(name)) {
			name = CONDITION_NAME + suffix;
			suffix++;
		}
		return name;
	}

	private Expression createConditionConjunction(IfStatement ifStatement,
			List<IfStatement> innerifStatementStatements) {
		Expression expression = ifStatement.getExpression();
		List<Expression> conjuncts = new ArrayList<>();

		conjuncts.add(parenthesizeInfixExpression(expression));
		conjuncts.addAll(innerifStatementStatements.stream()
			.map(IfStatement::getExpression)
			.map(this::parenthesizeInfixExpression)
			.collect(Collectors.toList()));
		AST ast = ifStatement.getAST();
		return createInfixExpressionConjunction(ast, conjuncts);
	}

	@SuppressWarnings("unchecked")
	private Expression createInfixExpressionConjunction(AST ast, List<Expression> conjuncts) {
		Expression left = conjuncts.remove(0);
		if (conjuncts.isEmpty()) {
			return left;
		}
		InfixExpression infixExpression = ast.newInfixExpression();
		infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
		infixExpression.setLeftOperand(left);
		Expression rightOperand = conjuncts.remove(0);
		infixExpression.setRightOperand(rightOperand);
		infixExpression.extendedOperands()
			.addAll(conjuncts);
		return infixExpression;
	}

	private Expression parenthesizeInfixExpression(Expression expression) {
		Expression copy = (Expression) astRewrite.createCopyTarget(expression);
		if (expression.getNodeType() != ASTNode.INFIX_EXPRESSION) {
			return copy;
		}
		InfixExpression infixExpression = (InfixExpression) expression;
		if (InfixExpression.Operator.CONDITIONAL_OR != infixExpression.getOperator()) {
			return copy;
		}
		AST ast = expression.getAST();
		ParenthesizedExpression parenthesizedExpression = ast.newParenthesizedExpression();
		parenthesizedExpression.setExpression(copy);
		return parenthesizedExpression;

	}

	private List<IfStatement> findInnerIfStatements(Statement thenStatement) {

		IfStatement innerIfStatement = null;
		if (thenStatement.getNodeType() == ASTNode.IF_STATEMENT) {
			innerIfStatement = (IfStatement) thenStatement;
		} else if (thenStatement.getNodeType() == ASTNode.BLOCK) {
			Block block = (Block) thenStatement;
			List<Statement> statements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
			if (statements.size() != 1) {
				return Collections.emptyList();
			}
			Statement singleBodyStatement = statements.get(0);
			if (singleBodyStatement.getNodeType() != ASTNode.IF_STATEMENT) {
				return Collections.emptyList();
			}
			innerIfStatement = (IfStatement) singleBodyStatement;
		}
		if (innerIfStatement == null) {
			return Collections.emptyList();
		}

		Statement elseStatement = innerIfStatement.getElseStatement();
		if (elseStatement != null) {
			return Collections.emptyList();
		}

		Statement innerThenStatement = innerIfStatement.getThenStatement();
		List<IfStatement> innerIfStatements = new ArrayList<>();
		innerIfStatements.add(innerIfStatement);
		innerIfStatements.addAll(findInnerIfStatements(innerThenStatement));
		return innerIfStatements;
	}

}
