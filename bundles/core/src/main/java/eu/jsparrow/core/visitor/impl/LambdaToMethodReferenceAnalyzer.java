package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class LambdaToMethodReferenceAnalyzer {

	void debugBreakpoint(Object o) {

	}

	boolean analyzeTypeInference(LambdaExpression lambdaExpressionNode) {
		if (lambdaExpressionNode.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			MethodInvocation methodInvocation = (MethodInvocation) lambdaExpressionNode.getParent();
			int argumentIndex = methodInvocation.arguments()
				.indexOf(lambdaExpressionNode);
			ITypeBinding[] parameterTypes = methodInvocation.resolveMethodBinding()
				.getParameterTypes();
			int argumentTypeIndex = Math.min(argumentIndex, parameterTypes.length - 1); // consider
																						// varargs!

			ITypeBinding argumentType = parameterTypes[argumentTypeIndex];
			return analyzeTypeBinding(argumentType);

		} else if (lambdaExpressionNode.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) lambdaExpressionNode.getParent();
			ITypeBinding leftHandSideTypeBinding = assignment.getLeftHandSide()
				.resolveTypeBinding();
			return analyzeTypeBinding(leftHandSideTypeBinding);
		}
		return true;
	}

	boolean analyzeTypeBinding(ITypeBinding typeBinding) {
		ITypeBinding[] argumentTypeTypeParameters = typeBinding.getTypeParameters();
		debugBreakpoint(argumentTypeTypeParameters);

		ITypeBinding[] argumentTypeTypeArguments = typeBinding.getTypeArguments();
		debugBreakpoint(argumentTypeTypeArguments);
		for(ITypeBinding t : argumentTypeTypeArguments) {
			if(t.isWildcardType()) {
				debugBreakpoint(t);
			}
		}

		return true;
	}

}
