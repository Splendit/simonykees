package at.splendit.simonykees.core.visitor;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;

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
		if (StringUtils.equals("toString", node.getName().getFullyQualifiedName())) { //$NON-NLS-1$
			/**
			 * First case: Integer.valueOf(myInt).toString()
			 */
			if (node.getExpression() != null && ASTNode.METHOD_INVOCATION == node.getExpression().getNodeType()) {

			}
			/**
			 * Second case: new Integer(myInt).toString()
			 */
			else if (true) {

			}
			astRewrite.replace(node, (Expression) astRewrite.createMoveTarget(node.getExpression()), null);
		}

		return true;
	}

	@Override
	public boolean visit(StringLiteral node) {
		/**i
		 * Third case: 4 + ""
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
					PrimitiveType.Code code;
					switch (otherSideTypeBinding.getName()) {
					case "Integer":
						code = PrimitiveType.INT;
						break;
					case "Double":
						code = PrimitiveType.DOUBLE;
						break;
					case "Long":
						code = PrimitiveType.LONG;
						break;
					case "Float":
						code = PrimitiveType.FLOAT;
						break;
					default:
						return true;
					}
					TypeLiteral typeLiteral = node.getAST().newTypeLiteral();
					PrimitiveType newTargetClass = node.getAST().newPrimitiveType(code);
					typeLiteral.setType(newTargetClass);
					//TODO use method invocation instead of StringLiteral as node ....
					astRewrite.set(node, MethodInvocation.EXPRESSION_PROPERTY, typeLiteral, null);
					ASTNode valueParameter = astRewrite.createMoveTarget(otherSide);
					astRewrite.getListRewrite(node, MethodInvocation.ARGUMENTS_PROPERTY).insertLast(valueParameter,
							null);
				}
			}
		}
		return true;
	}
}
