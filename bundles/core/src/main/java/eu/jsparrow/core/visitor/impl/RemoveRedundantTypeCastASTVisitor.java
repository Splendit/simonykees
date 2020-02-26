package eu.jsparrow.core.visitor.impl;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * The visitor first searches the next type cast operation. If the expression is
 * casted to a type which already is exactly the type of the expression, then
 * the type casting prefix is removed. Additionally, also parentheses will be
 * removed if they are not necessary any more. <br>
 * This rule regards two types as exactly the same only when both have also
 * exactly the same generic arguments.
 * <p>
 * Example:
 * <p>
 * {@code ((String)"HelloWorld").charAt(0);}<br>
 * is transformed to: <br>
 * {@code "HelloWorld".charAt(0);} <br>
 * 
 * @since 3.14.0
 *
 */
public class RemoveRedundantTypeCastASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(CastExpression castExpression) {
		Expression expression = castExpression.getExpression();
		if(expression.getNodeType() == ASTNode.LAMBDA_EXPRESSION || expression.getNodeType() == ASTNode.EXPRESSION_METHOD_REFERENCE) {
			StructuralPropertyDescriptor structuralProperty = castExpression.getLocationInParent();
//			if(structuralProperty != Assignment.RIGHT_HAND_SIDE_PROPERTY && structuralProperty != VariableDeclarationFragment.INITIALIZER_PROPERTY) {
//				return false;
//			}
			
			if(castExpression.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
				MethodInvocation parent = (MethodInvocation)castExpression.getParent();
				List<Expression> arguments = ASTNodeUtil.convertToTypedList(parent.arguments(), Expression.class);
				int castParamIndex = arguments.indexOf(castExpression);
				//TODO: take care of negative index even though in theory would never happen. 
				
				
				IMethodBinding iMethodBinding = parent.resolveMethodBinding();
				ITypeBinding[] formalParameterTypes = iMethodBinding.getParameterTypes();
				if(formalParameterTypes.length < arguments.size()) {
					/*
					 * Then the method must contain a varargs parameter e.g. void foo(Foo... args)
					 * TODO: We should come up with a way to find out the formal parameter type
					 */
				} else {
					ITypeBinding expectedFormalParameter = formalParameterTypes[castParamIndex];
					if(expectedFormalParameter.getFunctionalInterfaceMethod() == null) {
						// TODO: return. We can NOT remove the type casting. 
					}
					
				}
			} 
			/*
			 * TODO: do something similar with assignments and variable declaration fragments. 
			 * TODO: let's not do anything if the parent is not either: assignment, initialization or parameter of a method invocation. 
			 */
		}
		ITypeBinding typeFrom = castExpression.getExpression()
			.resolveTypeBinding();
		ITypeBinding typeTo = castExpression.getType()
			.resolveBinding();

		if (ClassRelationUtil.compareITypeBinding(typeFrom, typeTo)) {
			applyRule(castExpression);
		}
		return true;
	}

	private void applyRule(CastExpression typeCast) {
		ASTNode nodeToBeReplaced = getASTNodeToBeReplaced(typeCast);
		ASTNode replacement = astRewrite.createCopyTarget(getASTNodeReplacement(typeCast));

		astRewrite.replace(nodeToBeReplaced, replacement, null);
		onRewrite();

	}

	private static ASTNode getASTNodeToBeReplaced(CastExpression typeCast) {

		ASTNode nodeToBeReplaced = typeCast;
		while (nodeToBeReplaced.getParent()
			.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION) {
			nodeToBeReplaced = nodeToBeReplaced.getParent();
		}
		return nodeToBeReplaced;
	}

	private static ASTNode getASTNodeReplacement(CastExpression typeCast) {
		Expression expressionToBeCasted = typeCast.getExpression();
		int typeCastArgumentNodeType = expressionToBeCasted.getNodeType();
		if (typeCastArgumentNodeType != ASTNode.PARENTHESIZED_EXPRESSION) {
			return expressionToBeCasted;
		}
		ASTNode typeCastParent = typeCast.getParent();
		while (typeCastParent.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION) {
			typeCastParent = typeCastParent.getParent();
		}
		int typeCastParentNodeType = typeCastParent
			.getNodeType();
		if (typeCastParentNodeType != ASTNode.VARIABLE_DECLARATION_FRAGMENT
				&& typeCastParentNodeType != ASTNode.ASSIGNMENT) {
			return expressionToBeCasted;
		}

		return ASTNodeUtil.unwrapParenthesizedExpression(expressionToBeCasted);
	}

}
