package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * @since 3.2.0
 *
 */
public class CollapseIfStatementsASTVisitor extends AbstractASTRewriteASTVisitor {

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
		/*
		 * TODO: create a local variable to store the condition if it is too
		 * long (? decide what too long means)
		 */
		Expression conditionConjunction = createConditionConjunction(ifStatement, innerifStatementStatements);

		astRewrite.replace(ifStatement.getExpression(), conditionConjunction, null);
		astRewrite.replace(ifStatement.getThenStatement(), newBodyStatement, null);
		onRewrite();

		return true;
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
		if(conjuncts.isEmpty()) {
			return left;
		}
		InfixExpression infixExpression = ast.newInfixExpression();
		infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
		infixExpression.setLeftOperand(left);
		Expression rightOperand = conjuncts.remove(0);
		infixExpression.setRightOperand(rightOperand);
		infixExpression.extendedOperands().addAll(conjuncts);
		return infixExpression;
	}

	private Expression parenthesizeInfixExpression(Expression expression) {
		Expression copy = (Expression) astRewrite.createCopyTarget(expression);
		if (expression.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			AST ast = expression.getAST();
			ParenthesizedExpression parenthesizedExpression = ast.newParenthesizedExpression();
			parenthesizedExpression.setExpression(copy);
			return parenthesizedExpression;
		}
		return copy;
	}

	private List<IfStatement> findInnerIfStatements(Statement thenStatement) {

		IfStatement innerIfStatement = null;
		if(thenStatement.getNodeType() == ASTNode.IF_STATEMENT) {
			innerIfStatement = (IfStatement) thenStatement;
		} else if(thenStatement.getNodeType() == ASTNode.BLOCK) {
			Block block = (Block)thenStatement;
			List<Statement> statements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
			if(statements.size() != 1) {
				return Collections.emptyList();
			}
			Statement singleBodyStatement = statements.get(0);
			if(singleBodyStatement.getNodeType() != ASTNode.IF_STATEMENT) {
				return Collections.emptyList();
			}
			innerIfStatement = (IfStatement) singleBodyStatement;
		}
		if(innerIfStatement == null) {
			return Collections.emptyList();
		}
		
		Statement elseStatement = innerIfStatement.getElseStatement();
		if(elseStatement != null) {
			return Collections.emptyList();
		}
		
		Statement innerThenStatement = innerIfStatement.getThenStatement();
		List<IfStatement> innerIfStatements = new ArrayList<>();
		innerIfStatements.add(innerIfStatement);
		innerIfStatements.addAll(findInnerIfStatements(innerThenStatement));
		return innerIfStatements;
	}

}
