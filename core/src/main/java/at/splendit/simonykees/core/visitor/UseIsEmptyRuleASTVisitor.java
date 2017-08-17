package at.splendit.simonykees.core.visitor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * Finds all instantiations of {@link String} with no input parameter (new
 * String()) and all instantiations of {@link String} with a {@link String}
 * parameter (new String("foo")) and replaces those occurrences empty String
 * ("") or a String literal ("foo") respectively.
 * 
 * @author Martin Huter, Hans-Jörg Schrödl
 * @since 0.9.2
 */
public class UseIsEmptyRuleASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String STRING_FULLY_QUALLIFIED_NAME = java.lang.String.class.getName();
	private static final String COLLECTION_FULLY_QUALLIFIED_NAME = java.util.Collection.class.getName();
	private static final String MAP_FULLY_QUALLIFIED_NAME = java.util.Map.class.getName();
	private static final String LENGTH = "length";
	private static final String SIZE = "size";
	private static final String ZERO = "0";

	public boolean visit(MethodInvocation methodInvocation) {
		if (!methodInvocation.arguments().isEmpty()
				|| ASTNode.INFIX_EXPRESSION != methodInvocation.getParent().getNodeType()
				|| methodInvocation.getExpression() == null) {
			return false;
		}
		InfixExpression parent = (InfixExpression) methodInvocation.getParent();
		if (!parent.extendedOperands().isEmpty() || InfixExpression.Operator.EQUALS != parent.getOperator()) {
			return false;
		}
		if (!onStringOrMapType(methodInvocation)) {
			return false;
		}

		Expression varExpression = methodInvocation.getExpression();
		Expression otherOperand = getOtherOperand(methodInvocation, parent);
		NumberLiteral nl = tryParseOtherOperand(otherOperand);
		if (nl == null){
			return false;
		}
		
		if (!isZero(nl)) {
			return false;
		}

		SimpleName isEmptyMethod = methodInvocation.getAST().newSimpleName("isEmpty");
		MethodInvocation replaceNode = NodeBuilder.newMethodInvocation(methodInvocation.getAST(),
				(Expression) astRewrite.createMoveTarget(varExpression), isEmptyMethod);
		astRewrite.replace(parent, replaceNode, null);
		return true;
	}

	private NumberLiteral tryParseOtherOperand(Expression otherOperand) {
		if (isNumber(otherOperand)) {
			return (NumberLiteral) otherOperand;
		}
		// If its a prefix throw away the prefix
		else if (isPrefixNumber(otherOperand)){
			return (NumberLiteral) ((PrefixExpression) otherOperand).getOperand();
		}
		return null;
	}
	
	private boolean isNumber(Expression otherOperand){
		if(otherOperand.getNodeType() == ASTNode.NUMBER_LITERAL){
			return true;
		}
		return false;
	}
	
	private boolean isPrefixNumber(Expression otherOperand){
		if(otherOperand.getNodeType() == ASTNode.PREFIX_EXPRESSION){
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
		List<String> fullyQualifiedStringName = generateFullyQuallifiedNameList(STRING_FULLY_QUALLIFIED_NAME);
		List<String> fullyQualifiedDataStuctures = generateFullyQuallifiedNameList(COLLECTION_FULLY_QUALLIFIED_NAME,
				MAP_FULLY_QUALLIFIED_NAME);
		boolean isStringType = StringUtils.equals(LENGTH, node.getName().getFullyQualifiedName()) && ClassRelationUtil
				.isContentOfTypes(node.getExpression().resolveTypeBinding(), fullyQualifiedStringName);
		boolean isListOrMapType = StringUtils.equals(SIZE, node.getName().getFullyQualifiedName()) && ClassRelationUtil
				.isContentOfTypes(node.getExpression().resolveTypeBinding(), fullyQualifiedDataStuctures);
		if (isStringType || isListOrMapType) {
			return true;
		}
		return false;
	}
}
