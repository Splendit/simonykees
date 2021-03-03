package eu.jsparrow.core.visitor.utils;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class MethodDeclarationUtils {

	private MethodDeclarationUtils() {
		/*
		 * Hide default constructor.
		 */
	}

	/**
	 * Searches for the method signature or the type of the lambda expression
	 * wrapping the given return statement, and from there derives the expected
	 * return type.
	 * 
	 * Note that sometimes the type of the returned expression can be a subtype
	 * of the expected return type, or can be implicitly casted to the expected
	 * return type.
	 * 
	 * @param returnStatement
	 *            return statement to be checked
	 * @return the expected return type if the method signature or the lambda
	 *         expression wrapping the return statement can be found, or the
	 *         type of the expression of the return statement otherwise.
	 */
	public static ITypeBinding findExpectedReturnType(ReturnStatement returnStatement) {
		ASTNode parent = returnStatement.getParent();
		Expression returnExpression = returnStatement.getExpression();
		ITypeBinding returnExpBinding = returnExpression.resolveTypeBinding();

		do {
			if (ASTNode.METHOD_DECLARATION == parent.getNodeType()) {
				MethodDeclaration methodDecl = (MethodDeclaration) parent;
				IMethodBinding methodBinding = methodDecl.resolveBinding();
				return methodBinding.getReturnType();
			} else if (ASTNode.LAMBDA_EXPRESSION == parent.getNodeType()) {
				LambdaExpression lambdaExpression = (LambdaExpression) parent;
				IMethodBinding methodBinding = lambdaExpression.resolveMethodBinding();
				return methodBinding.getReturnType();
			}
			parent = parent.getParent();
		} while (parent != null);

		return returnExpBinding;
	}

	/**
	 * Finds the formal type of a parameter on the given index.
	 * 
	 * @param methodInvocation
	 *            a method invocation
	 * @param index
	 *            the index of the parameter to find the expected type
	 * @return the formal type of the parameter if one is found. An empty
	 *         {@link Optional} otherwise.
	 * 
	 */
	public static Optional<ITypeBinding> findFormalParameterType(MethodInvocation methodInvocation, int index) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return Optional.empty();
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (index >= arguments.size()) {
			return Optional.empty();
		}

		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		if (methodBinding.isVarargs() && index >= parameterTypes.length - 1) {
			ITypeBinding vargArgParam = parameterTypes[parameterTypes.length - 1];
			if (vargArgParam.isArray()) {
				return Optional.of(vargArgParam.getComponentType());
			}
		} else if (index < parameterTypes.length) {
			ITypeBinding parameterType = parameterTypes[index];
			return Optional.of(parameterType);
		}
		return Optional.empty();
	}
}
