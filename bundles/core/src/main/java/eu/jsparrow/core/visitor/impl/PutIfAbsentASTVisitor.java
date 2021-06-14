package eu.jsparrow.core.visitor.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.markers.common.PutIfAbsentEvent;
import eu.jsparrow.core.rule.impl.PutIfAbsentRule;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Looks for occurrences of map.put(key, value) where the following conditions
 * are met:
 * <ul>
 * <li>The statement is surrounded by an if-statement, allowing for at most one
 * block in between.</li>
 * <li>There are no other statements in this if-statement, neither in the then
 * nor else branch.</li>
 * <li>The condition of the if-statement is a invocation of
 * map.contains(..).</li>
 * <li>The expressions and first argument of both invocations match.</li>
 * <p>
 * 
 * If all conditions are met the entire if-statement is replaced with a call to
 * map.putIfAbsent(..) where the expression matches the previous expression and
 * the arguments match the previous argument.
 * 
 * Used in PutIfAbsentRule.
 * 
 * @see PutIfAbsentRule
 * 
 * @author Hans-Jörg Schrödl
 *
 */
public class PutIfAbsentASTVisitor extends AbstractASTRewriteASTVisitor implements PutIfAbsentEvent {

	private static final String MAP_FULLY_QUALIFIED_NAME = java.util.Map.class.getName();

	private static final String PUT = "put"; //$NON-NLS-1$
	private static final String CONTAINS_KEY = "containsKey"; //$NON-NLS-1$

	protected static final String PUT_IF_ABSENT = "putIfAbsent"; //$NON-NLS-1$

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (!isPutMethod(methodInvocation)) {
			return true;
		}
		boolean hasExpressionStatementParent = methodInvocation.getParent()
			.getNodeType() == ASTNode.EXPRESSION_STATEMENT;
		if (!hasExpressionStatementParent) {
			return true;
		}
		ExpressionStatement putStatement = (ExpressionStatement) methodInvocation.getParent();
		IfStatement ifStatement = getIfStatementIfExists(putStatement);
		if (ifStatement == null) {
			return true;
		}
		if (ifStatement.getElseStatement() != null) {
			return true;
		}
		if (!conditionIsContains(ifStatement)) {
			return true;
		}

		MethodInvocation containsMethod = (MethodInvocation) ((PrefixExpression) ifStatement.getExpression())
			.getOperand();
		if (!methodExpressionsAndArgumentsMatch(methodInvocation, containsMethod)) {
			return true;
		}

		ExpressionStatement statement = createPutIfAbsent(methodInvocation);
		astRewrite.replace(ifStatement, statement, null);
		getCommentRewriter().saveRelatedComments(ifStatement);
		onRewrite();
		addMarkerEvent(methodInvocation);
		return true;
	}

	private boolean methodExpressionsAndArgumentsMatch(MethodInvocation methodInvocation,
			MethodInvocation containsMethod) {
		ASTMatcher astMatcher = new ASTMatcher();
		boolean expressionsMatch = astMatcher.safeSubtreeMatch(containsMethod.getExpression(),
				methodInvocation.getExpression());
		boolean argumentsMatch = astMatcher.safeSubtreeMatch(containsMethod.arguments()
			.get(0),
				methodInvocation.arguments()
					.get(0));
		return expressionsMatch && argumentsMatch;
	}

	private IfStatement getIfStatementIfExists(ExpressionStatement putStatement) {
		IfStatement ifStatement = null;
		if (isSingleLineInIfStatement(putStatement)) {
			ifStatement = (IfStatement) putStatement.getParent();
		} else if (isInBlockSurroundedByIf(putStatement)) {
			ifStatement = (IfStatement) putStatement.getParent()
				.getParent();
		}
		return ifStatement;
	}

	private boolean conditionIsContains(IfStatement ifStatement) {
		Expression ifExpression = ifStatement.getExpression();
		if (ifExpression.getNodeType() != ASTNode.PREFIX_EXPRESSION) {
			return false;
		}
		PrefixExpression ifPrefixExpression = (PrefixExpression) ifExpression;
		if (ifPrefixExpression.getOperator() != PrefixExpression.Operator.NOT) {
			return false;
		}
		if (ifPrefixExpression.getOperand()
			.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return false;
		}
		MethodInvocation containsMethodInvocation = (MethodInvocation) ifPrefixExpression.getOperand();
		if (containsMethodInvocation.arguments()
			.isEmpty() || containsMethodInvocation.getExpression() == null) {
			return false;
		}
		return hasRightTypeAndName(containsMethodInvocation, MAP_FULLY_QUALIFIED_NAME, CONTAINS_KEY);
	}

	private boolean isPutMethod(MethodInvocation methodInvocation) {
		if (methodInvocation.arguments()
			.isEmpty() || methodInvocation.getExpression() == null) {
			return false;
		}
		return hasRightTypeAndName(methodInvocation, MAP_FULLY_QUALIFIED_NAME, PUT);
	}

	private boolean isSingleLineInIfStatement(ExpressionStatement expressionStatement) {
		return expressionStatement.getParent()
			.getNodeType() == ASTNode.IF_STATEMENT;
	}

	private boolean isInBlockSurroundedByIf(ExpressionStatement expressionStatement) {
		boolean isInBlock = expressionStatement.getParent()
			.getNodeType() == ASTNode.BLOCK;
		if (!isInBlock) {
			return false;
		}
		Block block = (Block) expressionStatement.getParent();
		if (block.statements()
			.size() != 1) {
			return false;
		}
		return block.getParent()
			.getNodeType() == ASTNode.IF_STATEMENT;
	}

	private ExpressionStatement createPutIfAbsent(MethodInvocation methodInvocation) {
		SimpleName putIfAbsentName = methodInvocation.getAST()
			.newSimpleName(PUT_IF_ABSENT);
		Expression firstArgument = (Expression) astRewrite.createMoveTarget((Expression) methodInvocation.arguments()
			.get(0));
		Expression secondArgument = (Expression) astRewrite.createMoveTarget((Expression) methodInvocation.arguments()
			.get(1));
		MethodInvocation putIfAbsent = NodeBuilder.newMethodInvocation(methodInvocation.getAST(),
				(Expression) astRewrite.createMoveTarget(methodInvocation.getExpression()), putIfAbsentName,
				Arrays.asList(firstArgument, secondArgument));

		return NodeBuilder.newExpressionStatement(methodInvocation.getAST(), putIfAbsent);
	}

	private Boolean hasRightTypeAndName(MethodInvocation methodInvocation, String type, String name) {
		List<String> fullyQualifiedMapName = generateFullyQualifiedNameList(type);
		Boolean epxressionTypeMatches = ClassRelationUtil.isContentOfTypes(methodInvocation.getExpression()
			.resolveTypeBinding(), fullyQualifiedMapName);
		Boolean methodNameMatches = StringUtils.equals(name, methodInvocation.getName()
			.getFullyQualifiedName());
		return epxressionTypeMatches && methodNameMatches;
	}

}
