package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isOverloadedOnParameter;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
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

		if (castExpression.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			MethodInvocation mi = (MethodInvocation) castExpression.getParent();
			ITypeBinding formalParamType = findFormalParameterType(castExpression, mi).orElse(null);
			if (formalParamType == null) {
				return true;
			}
			ITypeBinding expressionTypeBinding = expression.resolveTypeBinding();
			boolean compatible = expressionTypeBinding.isAssignmentCompatible(formalParamType);
			if (!compatible) {
				return true;
			}

			if (hasAmbiguousOverloads(mi, castExpression)) {
				return true;
			}
		}
		ITypeBinding typeFrom = expression.resolveTypeBinding();
		ITypeBinding typeTo = castExpression.getType()
			.resolveBinding();

		if (typeTo.isIntersectionType()) {
			return true;
		}

		if (containsWildCardTypeArgument(typeTo)) {
			return true;
		}

		if (ClassRelationUtil.compareITypeBinding(typeFrom, typeTo)) {
			applyRule(castExpression);
		}
		return true;
	}

	private boolean hasAmbiguousOverloads(MethodInvocation mi, CastExpression castExpression) {
		List<IMethodBinding> overloads = ClassRelationUtil.findOverloadedMethods(mi);
		IMethodBinding methodBinding = mi.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}

		@SuppressWarnings("rawtypes")
		List arguments = mi.arguments();
		final int actualIndex = arguments.indexOf(castExpression);
		if (actualIndex < 0) {
			return false;
		}

		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		final int paramLength = parameterTypes.length;
		int formalIndex = methodBinding.isVarargs() && actualIndex >= paramLength - 1 ? paramLength - 1 : actualIndex;

		return overloads.stream()
			.filter(overload -> overload.getParameterTypes().length == paramLength)
			.anyMatch(overload -> areAssignmentCompatibleParameters(parameterTypes, formalIndex, overload));
	}

	private boolean areAssignmentCompatibleParameters(ITypeBinding[] parameterTypes, int formalIndex,
			IMethodBinding overload) {
		ITypeBinding[] overloadParams = overload.getParameterTypes();
		if (overloadParams.length != parameterTypes.length) {
			return false;
		}
		for (int i = 0; i < overloadParams.length; i++) {
			ITypeBinding paramType = parameterTypes[i];
			ITypeBinding overloadParamType = overloadParams[i];
			if (i != formalIndex && !paramType.isAssignmentCompatible(overloadParamType)) {
				return false;
			}
		}
		return true;
	}

	private boolean containsWildCardTypeArgument(ITypeBinding typeBinding) {
		ITypeBinding[] typeArguments = typeBinding.getTypeArguments();
		if (typeArguments == null) {
			return false;
		}
		if (typeArguments.length == 0) {
			return false;
		}
		for (ITypeBinding typeArgument : typeArguments) {
			if (typeArgument.isWildcardType()) {
				return true;
			}
			if (containsWildCardTypeArgument(typeArgument)) {
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
		IMethodBinding iMethodInvocationBinding = parent.resolveMethodBinding();
		IMethodBinding iMethodBinding = iMethodInvocationBinding.getMethodDeclaration();

		List<IMethodBinding> overloadedMethods = ClassRelationUtil.findOverloadedMethods(parent);
		boolean isOverloaded = overloadedMethods.stream()
			.anyMatch(method -> isOverloadedOnParameter(iMethodBinding, method, castParamIndex));
		if (isOverloaded) {
			return false;
		}

		ITypeBinding formalType = findFormalParameterType(castExpression, parent).orElse(null);
		if (formalType == null) {
			return false;
		}

		return formalType.getFunctionalInterfaceMethod() != null
				&& !containsUndefinedTypeParameters(formalType,
						castExpression);
	}

	private boolean containsUndefinedTypeParameters(ITypeBinding formalParameter, CastExpression castExpression) {
		Expression expression = castExpression.getExpression();
		if (expression.getNodeType() != ASTNode.LAMBDA_EXPRESSION) {
			return false;
		}
		LambdaExpression lambda = (LambdaExpression) expression;
		List<VariableDeclarationFragment> inferedTypeParams = ASTNodeUtil.convertToTypedList(lambda.parameters(),
				VariableDeclarationFragment.class);
		if (inferedTypeParams.isEmpty()) {
			return false;
		}
		IMethodBinding fiMethod = formalParameter.getFunctionalInterfaceMethod();
		ITypeBinding[] fiParameterTypes = fiMethod.getParameterTypes();
		for (ITypeBinding fiParameterType : fiParameterTypes) {
			if (containsWildCardTypeArgument(fiParameterType)) {
				return true;
			}
		}
		return false;
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
		int typeCastParentNodeType = typeCastParent.getNodeType();
		if (typeCastParentNodeType != ASTNode.VARIABLE_DECLARATION_FRAGMENT
				&& typeCastParentNodeType != ASTNode.ASSIGNMENT) {
			return expressionToBeCasted;
		}

		return ASTNodeUtil.unwrapParenthesizedExpression(expressionToBeCasted);
	}

	private Optional<ITypeBinding> findFormalParameterType(Expression argument, MethodInvocation methodInvocation) {
		IMethodBinding miMethodBinding = methodInvocation.resolveMethodBinding();
		IMethodBinding declaration = miMethodBinding.getMethodDeclaration();
		ITypeBinding[] formalParameterTypes = declaration.getParameterTypes();
		int formalParamLength = formalParameterTypes.length;
		if (formalParamLength == 0) {
			return Optional.empty();
		}
		int lastIndex = formalParamLength - 1;
		@SuppressWarnings("unchecked")
		List<Expression> arguments = methodInvocation.arguments();
		int castParamIndex = arguments.indexOf(argument);
		if (castParamIndex < 0) {
			return Optional.empty();
		}

		ITypeBinding expecteFormalType;
		if (castParamIndex >= lastIndex && declaration.isVarargs()) {
			ITypeBinding varArgType = formalParameterTypes[lastIndex];
			expecteFormalType = varArgType.getComponentType();
		} else {
			expecteFormalType = formalParameterTypes[castParamIndex];
		}
		return Optional.of(expecteFormalType);
	}

}
