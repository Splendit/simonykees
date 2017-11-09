package eu.jsparrow.core.visitor.impl;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.core.builder.NodeBuilder;
import eu.jsparrow.core.util.ClassRelationUtil;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

public class PutIfAbsentASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String MAP_FULLY_QUALIFIED_NAME = java.util.Map.class.getName();

	private static final String PUT = "put"; //$NON-NLS-1$
	private static final String CONTAINS_KEY = "containsKey";

	private static final String PUT_IF_ABSENT = "putIfAbsent"; //$NON-NLS-1$

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (methodInvocation.arguments()
			.isEmpty() || methodInvocation.getExpression() == null) {
			return true;
		}
		if (!methodInvocationMatches(methodInvocation, MAP_FULLY_QUALIFIED_NAME, PUT)) {
			return true;
		}

		if (methodInvocation.getParent()
			.getNodeType() != ASTNode.EXPRESSION_STATEMENT) {
			return true;
		}
		ExpressionStatement putStatement = (ExpressionStatement) methodInvocation.getParent();
		if (putStatement.getParent()
			.getNodeType() != ASTNode.IF_STATEMENT) {
			return true;
		}

		IfStatement ifStatement = (IfStatement) putStatement.getParent();
		if (ifStatement.getThenStatement() != putStatement) {
			return true;
		}

		if (ifStatement.getElseStatement() != null) {
			return true;
		}

		Expression ifExpression = ifStatement.getExpression();
		if (ifExpression.getNodeType() != ASTNode.PREFIX_EXPRESSION) {
			return true;
		}

		PrefixExpression ifPrefixExpression = (PrefixExpression) ifExpression;
		if (ifPrefixExpression.getOperator() != PrefixExpression.Operator.NOT) {
			return true;
		}
		if (ifPrefixExpression.getOperand()
			.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return true;
		}

		MethodInvocation ifMethodInvocation = (MethodInvocation) ifPrefixExpression.getOperand();
		if (ifMethodInvocation.arguments()
			.isEmpty() || ifMethodInvocation.getExpression() == null) {
			return true;
		}

		if (!methodInvocationMatches(ifMethodInvocation, MAP_FULLY_QUALIFIED_NAME, CONTAINS_KEY)) {
			return true;
		}

		SimpleName putIfAbsentName = methodInvocation.getAST()
			.newSimpleName(PUT_IF_ABSENT); // $NON-NLS-1$
		Expression firstArgument = (Expression) astRewrite.createMoveTarget((Expression) methodInvocation.arguments()
			.get(0));
		Expression secondArgument = (Expression) astRewrite.createMoveTarget((Expression) methodInvocation.arguments()
			.get(1));
		MethodInvocation putIfAbsent = NodeBuilder.newMethodInvocation(methodInvocation.getAST(),
				(Expression) astRewrite.createMoveTarget(methodInvocation.getExpression()), putIfAbsentName,
				Arrays.asList(firstArgument, secondArgument));

		ExpressionStatement statement = NodeBuilder.newExpressionStatement(methodInvocation.getAST(), putIfAbsent);
		astRewrite.replace(ifStatement, statement, null);

		return false;
	}

	private Boolean methodInvocationMatches(MethodInvocation methodInvocation, String type, String name) {
		List<String> fullyQualifiedMapName = generateFullyQualifiedNameList(type);
		Boolean epxressionTypeMatches = ClassRelationUtil.isContentOfTypes(methodInvocation.getExpression()
			.resolveTypeBinding(), fullyQualifiedMapName);
		Boolean methodNameMatches = StringUtils.equals(name, methodInvocation.getName()
			.getFullyQualifiedName());
		return epxressionTypeMatches && methodNameMatches;
	}

}
