package eu.jsparrow.core.visitor.impl;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

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
 * @since 3.15.0
 *
 */
public class RemoveRedundantTypeCastASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(CastExpression castExpression) {
		Expression expression = castExpression.getExpression();
		boolean isLambdaExpression = expression.getNodeType() == ASTNode.LAMBDA_EXPRESSION
				|| expression.getNodeType() == ASTNode.EXPRESSION_METHOD_REFERENCE;
		if (isLambdaExpression && !isRedundantLambdaTypeCast(castExpression)) {
			return true;
		}
		ITypeBinding typeFrom = castExpression.getExpression()
			.resolveTypeBinding();
		ITypeBinding typeTo = castExpression.getType()
			.resolveBinding();

		if (typeTo.isIntersectionType()) {
			return true;
		}
		
		if(containsWildCardTypeArgument(typeTo)) {
			return true;
		}		

		if (ClassRelationUtil.compareITypeBinding(typeFrom, typeTo)) {
			applyRule(castExpression);
		}
		return true;
	}
	
	private boolean containsWildCardTypeArgument(ITypeBinding typeBinding) {
		ITypeBinding[] typeArguments = typeBinding.getTypeArguments();		
		if(typeArguments == null) {
			return false;
		}
		if(typeArguments.length == 0) {
			return false;
		}
		for(ITypeBinding typeArgument : typeArguments) {
			if(typeArgument.isWildcardType()) {
				return true;
			}
			if(containsWildCardTypeArgument(typeArgument)) {
				return true;
			}
		}
		return false;		
	}

	private boolean isRedundantLambdaTypeCast(CastExpression castExpression) {
		StructuralPropertyDescriptor castLocationInParent = castExpression.getLocationInParent();
		if (castLocationInParent == MethodInvocation.ARGUMENTS_PROPERTY) {
			return analyzeLambdaAsMethodArgument(castExpression);

		} else if (castLocationInParent == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) castExpression.getParent();
			ITypeBinding declarationType = declarationFragment.resolveBinding()
				.getType();
			return declarationType.getFunctionalInterfaceMethod() != null;

		} else if (castLocationInParent == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) castExpression.getParent();
			ITypeBinding declarationType = assignment.getLeftHandSide()
				.resolveTypeBinding();
			return declarationType.getFunctionalInterfaceMethod() != null;

		}
		return false;
	}

	private boolean analyzeLambdaAsMethodArgument(CastExpression castExpression) {
		MethodInvocation parent = (MethodInvocation) castExpression.getParent();
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(parent.arguments(), Expression.class);
		int castParamIndex = arguments.indexOf(castExpression);
		IMethodBinding iMethodBinding = parent.resolveMethodBinding();
		ITypeBinding[] formalParameterTypes = iMethodBinding.getParameterTypes();

		List<IMethodBinding> overloadedMethods = ClassRelationUtil.findOverloadedMethods(parent);
		boolean isOverloaded = overloadedMethods.stream()
			.anyMatch(method -> isOverloadedOnParameter(iMethodBinding, method, castParamIndex));
		if (isOverloaded) {
			return false;
		}

		int lastParameterIndex = formalParameterTypes.length - 1;
		if (iMethodBinding.isVarargs() && castParamIndex >= lastParameterIndex) {
			ITypeBinding lastFormalParamType = formalParameterTypes[lastParameterIndex];
			boolean isArray = lastFormalParamType.isArray();
			if (!isArray) {
				return false;
			}
			ITypeBinding componentType = lastFormalParamType.getComponentType();
			return componentType.getFunctionalInterfaceMethod() != null;

		} else {
			return formalParameterTypes[castParamIndex].getFunctionalInterfaceMethod() != null;
		}
	}

	private void applyRule(CastExpression typeCast) {
		ASTNode nodeToBeReplaced = getASTNodeToBeReplaced(typeCast);
		ASTNode replacement = astRewrite.createCopyTarget(getASTNodeReplacement(typeCast));

		astRewrite.replace(nodeToBeReplaced, replacement, null);
		onRewrite();

	}

	private ASTNode getASTNodeToBeReplaced(CastExpression typeCast) {
		ASTNode nodeToBeReplaced = typeCast;
		while (nodeToBeReplaced.getParent()
			.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION) {
			nodeToBeReplaced = nodeToBeReplaced.getParent();
		}
		return nodeToBeReplaced;
	}

	private ASTNode getASTNodeReplacement(CastExpression typeCast) {
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
