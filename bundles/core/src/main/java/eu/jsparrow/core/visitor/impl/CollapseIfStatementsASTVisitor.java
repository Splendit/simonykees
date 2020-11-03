package eu.jsparrow.core.visitor.impl;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.AND;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_AND;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_OR;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.OR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.OperatorUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;
import eu.jsparrow.rules.common.visitor.helper.LiveVariableScope;

/**
 * A visitor that searches for nested {@link IfStatement} and collapses them to
 * a single one if possible. Introduces a boolean variable to store the
 * condition if it contains more than 2 components.
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

		if (doCreateLocalVariable(ifStatement, innerifStatementStatements)) {
			Block block = ASTNodeUtil.getSpecificAncestor(ifStatement, Block.class);
			if (block != ifStatement.getParent()) {
				/*
				 * We should only insert a statement immediately before the
				 * current if
				 */
				return true;
			}
			ASTNode enclosingScope = this.aliveVariableScope.findEnclosingScope(ifStatement)
				.orElse(null);
			aliveVariableScope.lazyLoadScopeNames(enclosingScope);
			String conditionName = createConditionName();
			AST ast = ifStatement.getAST();
			Expression conditionConjunction = createConditionConjunction(ifStatement, innerifStatementStatements);
			VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
			fragment.setName(ast.newSimpleName(conditionName));
			fragment.setInitializer(conditionConjunction);
			VariableDeclarationStatement conditionDeclaration = ast.newVariableDeclarationStatement(fragment);
			conditionDeclaration.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN));

			ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
			listRewrite.insertBefore(conditionDeclaration, ifStatement, null);
			SimpleName newIfExpression = ast.newSimpleName(conditionName);
			astRewrite.replace(ifStatement.getExpression(), newIfExpression, null);
			aliveVariableScope.addName(enclosingScope, conditionName);

		} else {
			Expression conditionConjunction = createConditionConjunction(ifStatement, innerifStatementStatements);
			astRewrite.replace(ifStatement.getExpression(), conditionConjunction, null);
		}

		astRewrite.replace(ifStatement.getThenStatement(), astRewrite.createCopyTarget(newBodyStatement),
				null);
		saveComments(ifStatement, innerifStatementStatements, newBodyStatement);
		onRewrite();

		return true;
	}

	private void saveComments(IfStatement ifStatement, List<IfStatement> innerifStatementStatements,
			Statement newBodyStatement) {
		CommentRewriter commentRewriter = getCommentRewriter();
		List<Comment> internalComments = new ArrayList<>(
				commentRewriter.findRelatedComments(ifStatement.getThenStatement()));
		List<Comment> savedComments = new ArrayList<>();
		savedComments.addAll(commentRewriter.findRelatedComments(newBodyStatement));
		savedComments.addAll(commentRewriter.findRelatedComments(ifStatement.getExpression()));
		savedComments.addAll(innerifStatementStatements.stream()
			.map(IfStatement::getExpression)
			.map(commentRewriter::findRelatedComments)
			.flatMap(List::stream)
			.collect(Collectors.toList()));
		internalComments.removeAll(savedComments);
		commentRewriter.saveBeforeStatement(ifStatement, internalComments);
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
	
	@Override
	public void endVisit(CompilationUnit compilationUnit) {
		aliveVariableScope.clearCompilationUnitScope(compilationUnit);
		super.endVisit(compilationUnit);
	}

	@Override
	public void endVisit(TypeDeclaration typeDeclaration) {
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
		conjuncts.addAll(innerifStatementStatements.stream()
			.map(IfStatement::getExpression)
			.map(this::parenthesize)
			.collect(Collectors.toList()));
		AST ast = ifStatement.getAST();
		Expression left = parenthesize(expression);
		Expression right = conjuncts.remove(0);
		return NodeBuilder.newInfixExpression(ast, CONDITIONAL_AND, left, right, conjuncts);
	}

	private Expression parenthesize(Expression expression) {
		Expression copy = (Expression) astRewrite.createCopyTarget(expression);
		if (expression.getNodeType() != ASTNode.INFIX_EXPRESSION) {
			return copy;
		}
		InfixExpression infixExpression = (InfixExpression) expression;
		InfixExpression.Operator infixOperator = infixExpression.getOperator();
		boolean requiresParenthesis = CONDITIONAL_OR == infixOperator || OR == infixOperator || AND == infixOperator;
		if (!requiresParenthesis) {
			return copy;
		}
		return NodeBuilder.newParenthesizedExpression(expression.getAST(), copy);
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