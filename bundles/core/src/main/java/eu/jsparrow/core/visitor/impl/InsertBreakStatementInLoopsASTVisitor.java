package eu.jsparrow.core.visitor.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class InsertBreakStatementInLoopsASTVisitor extends AbstractASTRewriteASTVisitor {

	private List<String> safeCollectionMethods = Collections
		.unmodifiableList(Arrays.asList("isEmpty", "contains", "containsKey", "containsValue"));

	@Override
	public boolean visit(EnhancedForStatement forStatement) {
		Statement body = forStatement.getBody();
		if (body.getNodeType() != ASTNode.BLOCK) {
			return true;
		}
		Block block = (Block) body;
		List<IfStatement> bodyStatements = ASTNodeUtil.returnTypedList(block.statements(), IfStatement.class);
		if (bodyStatements.size() != 1) {
			return true;
		}
		IfStatement ifStatement = bodyStatements.get(0);
		if (ifStatement.getElseStatement() != null) {
			return true;
		}

		boolean hasSideEffects = hasSideEffects(ifStatement.getExpression());
		if (hasSideEffects) {
			return false;
		}

		Statement thenStatement = ifStatement.getThenStatement();
		if (thenStatement.getNodeType() != ASTNode.BLOCK) {
			return true;
		}

		Block ifBodyBlock = (Block) thenStatement;
		List<ExpressionStatement> ifBodyStatements = ASTNodeUtil.returnTypedList(ifBodyBlock.statements(),
				ExpressionStatement.class);
		if (ifBodyStatements.size() != 1) {
			return true;
		}

		ExpressionStatement expressionStatement = ifBodyStatements.get(0);
		Expression expression = expressionStatement.getExpression();
		if (expression.getNodeType() != ASTNode.ASSIGNMENT) {
			return true;
		}

		Assignment assignment = ((Assignment) expression);
		Expression rhs = assignment.getRightHandSide();
		if (rhs.getNodeType() != ASTNode.BOOLEAN_LITERAL) {
			return true;
		}

		AST ast = forStatement.getAST();
		BreakStatement breakStatement = ast.newBreakStatement();
		ListRewrite listRewrite = astRewrite.getListRewrite(ifBodyBlock, Block.STATEMENTS_PROPERTY);
		listRewrite.insertLast(breakStatement, null);
		onRewrite();

		return true;
	}

	private boolean hasSideEffects(Expression expression) {

		if (isLiteral(expression) || expression.getNodeType() == ASTNode.SIMPLE_NAME) {
			return false;
		}

		if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation methodInvocation = (MethodInvocation) expression;
			return hasSideEffects(methodInvocation);

		}

		if (expression.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			InfixExpression infixExpression = (InfixExpression) expression;
			return hasSideEffects(infixExpression.getLeftOperand())
					|| hasSideEffects(infixExpression.getRightOperand());
		}

		if (expression.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION) {
			return hasSideEffects(((ParenthesizedExpression) expression).getExpression());
		}

		if (expression.getNodeType() == ASTNode.PREFIX_EXPRESSION) {
			PrefixExpression prefixExpression = (PrefixExpression) expression;
			return hasSideEffects(prefixExpression.getOperand());
		}
		return true;
	}

	private boolean hasSideEffects(MethodInvocation methodInvocation) {
		Expression miExpression = methodInvocation.getExpression();
		if (miExpression != null && ASTNode.SIMPLE_NAME != miExpression.getNodeType()) {
			return true;
		}
		
		boolean hasSafeArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.stream()
			.allMatch(argument -> isLiteral(argument) || argument.getNodeType() == ASTNode.SIMPLE_NAME);
		if (!hasSafeArguments) {
			return true;
		}

		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (ClassRelationUtil.isContentOfTypes(declaringClass,
				Arrays.asList(java.lang.Object.class.getName(), java.lang.String.class.getName()))) {
			return false;
		}

		if (ClassRelationUtil.isInheritingContentOfTypes(declaringClass,
				Collections.singletonList(java.util.Collection.class.getName()))
				|| ClassRelationUtil.isContentOfType(declaringClass, java.util.Collection.class.getName())) {
			String methodName = methodBinding.getName();
			return !safeCollectionMethods.contains(methodName);

		}
		return true;
	}

	private boolean isLiteral(Expression expression) {
		int nodeType = expression.getNodeType();
		return nodeType == ASTNode.BOOLEAN_LITERAL || nodeType == ASTNode.CHARACTER_LITERAL
				|| nodeType == ASTNode.NULL_LITERAL || nodeType == ASTNode.NUMBER_LITERAL
				|| nodeType == ASTNode.STRING_LITERAL;
	}

}
