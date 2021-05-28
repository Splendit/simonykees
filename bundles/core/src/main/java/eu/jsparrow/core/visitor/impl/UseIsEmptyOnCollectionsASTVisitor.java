package eu.jsparrow.core.visitor.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.markers.common.UseIsEmptyOnCollectionsEvent;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Looks for Collections, Maps and String length operation that are compared to
 * 0.
 * <p>
 * Those accesses can be replaced with an isEmpty() clause.
 * <ul>
 * <li>Collection: since 1.2, ex.: myCollection.size() == 0 ->
 * myCollection.isEmpty()</li>
 * <li>Map: since 1.2, ex.: myMap.size() == 0 -> myMap.isEmpty()</li>
 * <li>Collection: since 1.6, ex.: myString.length() == 0 ->
 * myString.isEmpty()</li>
 * </ul>
 * 
 * @author Martin Huter, Hans-Jörg Schrödl
 * @since 2.1.0
 */
public class UseIsEmptyOnCollectionsASTVisitor extends AbstractASTRewriteASTVisitor implements UseIsEmptyOnCollectionsEvent {

	private static final String STRING_FULLY_QUALIFIED_NAME = java.lang.String.class.getName();
	private static final String COLLECTION_FULLY_QUALIFIED_NAME = java.util.Collection.class.getName();
	private static final String MAP_FULLY_QUALIFIED_NAME = java.util.Map.class.getName();
	private static final String LENGTH = "length"; //$NON-NLS-1$
	private static final String SIZE = "size"; //$NON-NLS-1$

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (!methodInvocation.arguments()
			.isEmpty()
				|| ASTNode.INFIX_EXPRESSION != methodInvocation.getParent()
					.getNodeType()
				|| methodInvocation.getExpression() == null) {
			return true;
		}
		InfixExpression parent = (InfixExpression) methodInvocation.getParent();
		if (!parent.extendedOperands()
			.isEmpty() || InfixExpression.Operator.EQUALS != parent.getOperator()) {
			return true;
		}
		if (!onStringOrMapType(methodInvocation)) {
			return true;
		}

		Expression varExpression = methodInvocation.getExpression();
		Expression otherOperand = getOtherOperand(methodInvocation, parent);
		NumberLiteral nl = tryParseOtherOperand(otherOperand);
		if (nl == null) {
			return true;
		}

		if (!isZero(nl)) {
			return true;
		}

		SimpleName isEmptyMethod = methodInvocation.getAST()
			.newSimpleName("isEmpty"); //$NON-NLS-1$
		MethodInvocation replaceNode = NodeBuilder.newMethodInvocation(methodInvocation.getAST(),
				(Expression) astRewrite.createMoveTarget(varExpression), isEmptyMethod);
		astRewrite.replace(parent, replaceNode, null);
		getCommentRewriter().saveCommentsInParentStatement(parent);
		onRewrite();
		addMarkerEvent(parent, varExpression);
		return true;
	}

	private NumberLiteral tryParseOtherOperand(Expression otherOperand) {
		if (isNumber(otherOperand)) {
			return (NumberLiteral) otherOperand;
		}
		// If its a prefix throw away the prefix
		else if (isPrefixNumber(otherOperand)) {
			return (NumberLiteral) ((PrefixExpression) otherOperand).getOperand();
		}
		return null;
	}

	private boolean isNumber(Expression otherOperand) {
		return otherOperand.getNodeType() == ASTNode.NUMBER_LITERAL;
	}

	private boolean isPrefixNumber(Expression otherOperand) {
		if (otherOperand.getNodeType() == ASTNode.PREFIX_EXPRESSION) {
			PrefixExpression prefix = (PrefixExpression) otherOperand;
			Expression prefixExpression = prefix.getOperand();
			return isNumber(prefixExpression);
		}
		return false;
	}

	private boolean isZero(NumberLiteral nl) {
		Double tokenAsDouble = Double.parseDouble(nl.getToken());
		return tokenAsDouble == 0;
	}

	private Expression getOtherOperand(MethodInvocation methodInvocation, InfixExpression parent) {
		Expression otherOperand = null;
		if (InfixExpression.LEFT_OPERAND_PROPERTY == methodInvocation.getLocationInParent()) {
			otherOperand = parent.getRightOperand();
		} else {
			otherOperand = parent.getLeftOperand();
		}
		return otherOperand;
	}

	private boolean onStringOrMapType(MethodInvocation node) {
		// check type of variable and get name
		List<String> fullyQualifiedStringName = generateFullyQualifiedNameList(STRING_FULLY_QUALIFIED_NAME);
		List<String> fullyQualifiedDataStuctures = generateFullyQualifiedNameList(COLLECTION_FULLY_QUALIFIED_NAME,
				MAP_FULLY_QUALIFIED_NAME);
		boolean isStringType = StringUtils.equals(LENGTH, node.getName()
			.getFullyQualifiedName()) && ClassRelationUtil.isContentOfTypes(
					node.getExpression()
						.resolveTypeBinding(),
					fullyQualifiedStringName);
		boolean isListOrMapType = StringUtils.equals(SIZE, node.getName()
			.getFullyQualifiedName()) && ClassRelationUtil.isContentOfTypes(
					node.getExpression()
						.resolveTypeBinding(),
					fullyQualifiedDataStuctures);
		return isStringType || isListOrMapType;
	}
}
