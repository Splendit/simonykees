package eu.jsparrow.core.visitor.utils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;

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
	
	

}
