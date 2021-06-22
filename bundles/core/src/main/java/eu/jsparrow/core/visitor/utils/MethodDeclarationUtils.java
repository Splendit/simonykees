package eu.jsparrow.core.visitor.utils;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

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

	public static boolean isMainMethod(MethodDeclaration methodDeclaration) {
		if (!"main".equals(methodDeclaration.getName() //$NON-NLS-1$
			.getIdentifier())) {
			return false;
		}

		@SuppressWarnings("rawtypes")
		List modifiers = methodDeclaration.modifiers();
		if (!ASTNodeUtil.hasModifier(modifiers, Modifier::isStatic)
				|| !ASTNodeUtil.hasModifier(modifiers, Modifier::isPublic)) {
			return false;
		}

		Type t = methodDeclaration.getReturnType2();
		ITypeBinding returnTypeBinding = t.resolveBinding();
		if (!"void".equals(returnTypeBinding.getName())) { //$NON-NLS-1$
			return false;
		}

		List<VariableDeclaration> params = ASTNodeUtil.convertToTypedList(methodDeclaration.parameters(),
				VariableDeclaration.class);

		if (params.size() != 1) {
			return false;
		}

		VariableDeclaration param = params.get(0);
		IVariableBinding paramVariableBinding = param.resolveBinding();
		ITypeBinding paramTypeBinding = paramVariableBinding.getType();

		if (!paramTypeBinding.isArray()) {
			return false;
		}

		return ClassRelationUtil.isContentOfType(paramTypeBinding.getElementType(), java.lang.String.class.getName())
				&& paramTypeBinding.getDimensions() == 1;
	}

	public static boolean isJavaApplicationMainMethod(CompilationUnit compilationUnit,
			MethodDeclaration methodDeclaration) {
		if (!isMainMethod(methodDeclaration)) {
			return false;
		}
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		if (methodBinding == null) {
			return false;
		}
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (!declaringClass.isTopLevel()) {
			return false;
		}
		String declaringClassQualifiedName = declaringClass.getQualifiedName();
		String javaElementName = compilationUnit.getJavaElement()
			.getElementName();

		int indexOfFileExtension = javaElementName.lastIndexOf(".java"); //$NON-NLS-1$
		if (indexOfFileExtension > 0) {
			javaElementName = javaElementName.substring(0, indexOfFileExtension);
			String fullyQualifiedCompilationUnitPackageName = compilationUnit.getPackage()
				.getName()
				.getFullyQualifiedName();

			return declaringClassQualifiedName.equals(fullyQualifiedCompilationUnitPackageName + '.' + javaElementName);
		}

		return false;
	}
}