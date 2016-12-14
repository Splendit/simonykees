package at.splendit.simonykees.core.visitor;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.constants.ReservedNames;

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
 * TODO primitive Variables
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class PrimitiveBoxedForStringASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static Integer STRING_KEY = 1;
	private static String STRING_FULLY_QUALLIFIED_NAME = "java.lang.String"; //$NON-NLS-1$

	public PrimitiveBoxedForStringASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(STRING_KEY, generateFullyQuallifiedNameList(STRING_FULLY_QUALLIFIED_NAME));
	}

	@Override
	public boolean visit(MethodInvocation node) {

		/*
		 * checks if method invocation is toString. the invocation need to have
		 * zero arguments the expressions type where the toString is used on
		 * needs to be a String or a StringLiteral
		 */
		if (StringUtils.equals(ReservedNames.MI_TO_STRING, node.getName().getFullyQualifiedName())) {
			/**
			 * First case: Integer.valueOf(myInt).toString()
			 */
			if (node.getExpression() == null) {
				return true;
			}

			Expression refactorCandidateExpression = null;
			SimpleName refactorPrimitiveType = null;

			if (ASTNode.METHOD_INVOCATION == node.getExpression().getNodeType()) {
				MethodInvocation expetedValueOf = (MethodInvocation) node.getExpression();
				if (StringUtils.equals(ReservedNames.MI_VALUE_OF, expetedValueOf.getName().getFullyQualifiedName())
						&& null != expetedValueOf.getExpression()
						&& ASTNode.SIMPLE_NAME == expetedValueOf.getExpression().getNodeType()
						&& 1 == expetedValueOf.arguments().size()) {
					refactorPrimitiveType = (SimpleName) expetedValueOf.getExpression();
					refactorCandidateExpression = (Expression) expetedValueOf.arguments().get(0);
				}
			}
			/**
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

					/**
					 * new Float(4D).toString() is not transformable to
					 * Float.toString(4D) because toString only allows
					 * primitives that are implicit cast-able to float. doubles
					 * do not have this property
					 */
					if (ReservedNames.FLOAT.equals(refactorPrimitiveType.getIdentifier())
							&& ASTNode.NUMBER_LITERAL == refactorCandidateExpression.getNodeType()
							&& ((NumberLiteral) refactorCandidateExpression).getToken()
									.contains(ReservedNames.DOUBLE_LITERAL)) {
						refactorPrimitiveType = null;
						refactorCandidateExpression = null;
					}
				}
			}
			if (null != refactorPrimitiveType && isPrimitiveNumberClass(refactorPrimitiveType)
					&& null != refactorCandidateExpression) {
				if (ASTNode.NUMBER_LITERAL == refactorCandidateExpression.getNodeType()) {
					NumberLiteral moveTarget = (NumberLiteral) astRewrite.createMoveTarget(refactorCandidateExpression);
					astRewrite.getListRewrite(node, MethodInvocation.ARGUMENTS_PROPERTY).insertLast(moveTarget, null);
					astRewrite.set(node, MethodInvocation.EXPRESSION_PROPERTY, refactorPrimitiveType, null);
				}

			}

		}

		return true;
	}

	private boolean isPrimitiveNumberClass(SimpleName simpleName) {
		switch (simpleName.getIdentifier()) {
		case ReservedNames.INTEGER:
		case ReservedNames.FLOAT:
		case ReservedNames.DOUBLE:
		case ReservedNames.LONG:
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean visit(StringLiteral node) {
		/**
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
				if (ASTNode.NUMBER_LITERAL == otherSide.getNodeType() && otherSideTypeBinding != null
						&& otherSideTypeBinding.isPrimitive()) {
					String primitiveClassName;
					switch (otherSideTypeBinding.getName()) {
					case ReservedNames.INTEGER_PRIMITIVE:
						primitiveClassName = ReservedNames.INTEGER;
						break;
					case ReservedNames.DOUBLE_PRIMITIVE:
						primitiveClassName = ReservedNames.DOUBLE;
						break;
					case ReservedNames.LONG_PRIMITIVE:
						primitiveClassName = ReservedNames.LONG;
						break;
					case ReservedNames.FLOAT_PRIMITIVE:
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
