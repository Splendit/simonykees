package eu.jsparrow.core.visitor.impl;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;

import eu.jsparrow.core.builder.NodeBuilder;
import eu.jsparrow.core.constants.ReservedNames;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Primitives should not be boxed just for "String" conversion
 * 
 * Noncompliant Code Example
 * 
 * int myInt = 4; String myIntString = new Integer(myInt).toString();
 * myIntString = Integer.valueOf(myInt).toString(); myIntString = 4 + "";
 * 
 * Compliant Solution
 * 
 * int myInt = 4; String myIntString = Integer.toString(myInt);
 * 
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class PrimitiveBoxedForStringASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(MethodInvocation node) {

		/*
		 * checks if method invocation is toString. the invocation need to have
		 * zero arguments the expressions type where the toString is used on
		 * needs to be a String or a StringLiteral
		 */
		if (StringUtils.equals(ReservedNames.MI_TO_STRING, node.getName().getFullyQualifiedName())
				&& 1 >= node.arguments().size()) {

			/*
			 * First case: Integer.valueOf(myInt).toString()
			 */
			if (node.getExpression() == null) {
				return true;
			}

			Expression refactorCandidateExpression = null;
			SimpleName refactorPrimitiveType = null;
			ITypeBinding refactorCandidateTypeBinding = null;

			if (ASTNode.METHOD_INVOCATION == node.getExpression().getNodeType()) {
				MethodInvocation expetedValueOf = (MethodInvocation) node.getExpression();
				if (StringUtils.equals(ReservedNames.MI_VALUE_OF, expetedValueOf.getName().getFullyQualifiedName())
						&& null != expetedValueOf.getExpression()
						&& ASTNode.SIMPLE_NAME == expetedValueOf.getExpression().getNodeType()
						&& 1 == expetedValueOf.arguments().size()) {
					refactorPrimitiveType = (SimpleName) expetedValueOf.getExpression();
					refactorCandidateExpression = (Expression) expetedValueOf.arguments().get(0);
					refactorCandidateTypeBinding = refactorCandidateExpression.resolveTypeBinding();
				}
			}

			/*
			 * Second case: new Integer(myInt).toString()
			 */
			else if (ASTNode.CLASS_INSTANCE_CREATION == node.getExpression().getNodeType()) {
				ClassInstanceCreation expectedPrimitiveNumberClass = (ClassInstanceCreation) node.getExpression();
				if (ASTNode.SIMPLE_TYPE == expectedPrimitiveNumberClass.getType().getNodeType()
						&& ASTNode.SIMPLE_NAME == ((SimpleType) expectedPrimitiveNumberClass.getType()).getName()
								.getNodeType()
						&& 1 == expectedPrimitiveNumberClass.arguments().size()) {
					refactorPrimitiveType = (SimpleName) ((SimpleType) expectedPrimitiveNumberClass.getType())
							.getName();
					refactorCandidateExpression = (Expression) expectedPrimitiveNumberClass.arguments().get(0);
					refactorCandidateTypeBinding = refactorCandidateExpression.resolveTypeBinding();

					/*
					 * new Float(4D).toString() is not transformable to
					 * Float.toString(4D) because toString only allows
					 * primitives that are implicit cast-able to float. doubles
					 * do not have this property
					 */
					Predicate<ITypeBinding> isDoubleVariable = 
							binding -> (binding != null && (StringUtils.contains(binding.getName(), ReservedNames.DOUBLE_PRIMITIVE)
									|| (StringUtils.contains(binding.getName(), ReservedNames.DOUBLE))));

					if (ReservedNames.FLOAT.equals(refactorPrimitiveType.getIdentifier())
							&& isDoubleVariable.test(refactorCandidateTypeBinding)) {
						refactorPrimitiveType = null;
						refactorCandidateExpression = null;
						refactorCandidateTypeBinding = null;
					}
				}
			}
			if (refactorCandidateTypeBinding != null && node.arguments().isEmpty()
					&& isPrimitiveNumberClass(refactorPrimitiveType.getIdentifier())
					&& isPrimitiveNumberClass(refactorCandidateTypeBinding.getName())) {
				
				Expression moveTargetArgument = (Expression) astRewrite.createMoveTarget(refactorCandidateExpression);
				astRewrite.getListRewrite(node, MethodInvocation.ARGUMENTS_PROPERTY).insertLast(moveTargetArgument,
						null);
				SimpleName staticClassType = (SimpleName) astRewrite.createCopyTarget(refactorPrimitiveType);
				astRewrite.set(node, MethodInvocation.EXPRESSION_PROPERTY, staticClassType, null);
			}

		}

		return true;
	}

	private boolean isPrimitiveNumberClass(String simpleName) {
		switch (simpleName) {
		case ReservedNames.INTEGER:
		case ReservedNames.FLOAT:
		case ReservedNames.DOUBLE:
		case ReservedNames.LONG:
		case ReservedNames.INTEGER_PRIMITIVE:
		case ReservedNames.FLOAT_PRIMITIVE:
		case ReservedNames.DOUBLE_PRIMITIVE:
		case ReservedNames.LONG_PRIMITIVE:
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean visit(StringLiteral node) {

		/*
		 * i Third case: 4 + ""
		 */
		if ("".equals(node.getLiteralValue()) && ASTNode.INFIX_EXPRESSION == node.getParent().getNodeType()) { //$NON-NLS-1$
			InfixExpression infixExpression = (InfixExpression) node.getParent();
			if (InfixExpression.Operator.PLUS == infixExpression.getOperator()) {
				Expression otherSide;
				if (infixExpression.getLeftOperand().equals(node)) {
					otherSide = infixExpression.getRightOperand();
				} else {
					otherSide = infixExpression.getLeftOperand();
				}
				ITypeBinding otherSideTypeBinding = otherSide.resolveTypeBinding();
				if (otherSideTypeBinding != null && isPrimitiveNumberClass(otherSideTypeBinding.getName())) {
					String primitiveClassName;
					switch (otherSideTypeBinding.getName()) {
					case ReservedNames.INTEGER_PRIMITIVE:
					case ReservedNames.INTEGER:
						primitiveClassName = ReservedNames.INTEGER;
						break;
					case ReservedNames.DOUBLE_PRIMITIVE:
					case ReservedNames.DOUBLE:
						primitiveClassName = ReservedNames.DOUBLE;
						break;
					case ReservedNames.LONG_PRIMITIVE:
					case ReservedNames.LONG:
						primitiveClassName = ReservedNames.LONG;
						break;
					case ReservedNames.FLOAT_PRIMITIVE:
					case ReservedNames.FLOAT:
						primitiveClassName = ReservedNames.FLOAT;
						break;
					default:
						return true;
					}
					SimpleName typeName = NodeBuilder.newSimpleName(node.getAST(), primitiveClassName);

					SimpleName toStringSimpleName = NodeBuilder.newSimpleName(node.getAST(),
							ReservedNames.MI_TO_STRING);

					Expression valueParameter = (Expression) astRewrite.createMoveTarget(otherSide);

					MethodInvocation methodInvocation = NodeBuilder.newMethodInvocation(node.getAST(), typeName,
							toStringSimpleName, valueParameter);

					astRewrite.replace(node, methodInvocation, null);
				}
			}
		}
		return true;
	}
}
