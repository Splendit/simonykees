package eu.jsparrow.core.visitor.impl.comparatormethods;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * This visitor looks for lambda expressions which represent a
 * {@link java.util.Comparator} and have a certain structure, for example<br>
 * {@code  (u1, u2) -> u1.compareTo(u2) }<br>
 * or <br>
 * {@code (u1, u2) -> u1.getSalary().compareTo(u2.getSalary())}.
 * <p>
 * These lambda expressions are transformed to static method invocations of
 * {@link java.util.Comparator}.
 * <p>
 * Simple examples:
 * <p>
 * {@code Comparator<Integer> comparator = (lhs, rhs) -> lhs.compareTo(rhs); }
 * <br>
 * is transformed to<br>
 * {@code Comparator<Integer> comparator = Comparator.naturalOrder();}
 * <p>
 * 
 * {@code Comparator<Integer> comparator = (lhs, rhs) -> rhs.compareTo(lhs); }
 * <br>
 * is transformed to<br>
 * {@code Comparator<Integer> comparator = Comparator.reverseOrder(); }
 * 
 * <p>
 * Example with transformations to method references:
 * <p>
 * 
 * {@code Comparator<Deque<Integer>> comparator = (lhs, rhs) -> lhs.getFirst().compareTo(rhs.getFirst()); }
 * <br>
 * is transformed to<br>
 * {@code Comparator<Deque<Integer>> comparator = Comparator.comparingInt(Deque::getFirst);}
 * <p>
 *
 * 
 * @since 3.23.0
 */
public class UseComparatorMethodsASTVisitor extends AbstractAddImportASTVisitor {
	static final String JAVA_LANG_COMPARABLE = java.lang.Comparable.class.getName();
	static final String JAVA_UTIL_COMPARATOR = java.util.Comparator.class.getName();

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (continueVisiting) {
			verifyImport(compilationUnit, JAVA_UTIL_COMPARATOR);
			verifyImport(compilationUnit, java.lang.Object.class.getName());
		}
		return continueVisiting;
	}

	@Override
	public boolean visit(LambdaExpression lambda) {

		new UseComparatorMethodsAnalyzer().analyze(lambda)
			.ifPresent(analysisResult -> {
				IMethodBinding comparisonKeyMethod = analysisResult.getComparisonKeyMethod()
					.orElse(null);

				MethodInvocation lambdaReplacement;
				if (comparisonKeyMethod == null) {
					lambdaReplacement = findSimpleLambdaReplacement(lambda, analysisResult);
				} else {
					lambdaReplacement = findLambdaReplacementWithComparisonKey(lambda, analysisResult,
							comparisonKeyMethod);
				}
				astRewrite.replace(lambda, lambdaReplacement, null);
				onRewrite();
			});
		return true;
	}

	private MethodInvocation findLambdaReplacementWithComparisonKey(LambdaExpression lambda,
			LambdaAnalysisResult analysisResult, IMethodBinding comparisonKeyMethod) {

		String comparatorMethodName = getComparatorMethodName(comparisonKeyMethod);
		MethodInvocation comparatorMethodInvocation = createComparatorMethodInvocation(comparatorMethodName);
		@SuppressWarnings("unchecked")
		List<Expression> arguments = comparatorMethodInvocation.arguments();
		Expression comparatorMethodArgument = createComparatorMethodArgument(lambda, analysisResult,
				comparisonKeyMethod);
		arguments.add(comparatorMethodArgument);
		if (analysisResult.isReversed()) {
			return reverseComparatorMethodInvocation(comparatorMethodInvocation);
		}
		return comparatorMethodInvocation;
	}

	private Expression createComparatorMethodArgument(LambdaExpression lambda, LambdaAnalysisResult analysisResult,
			IMethodBinding comparisonKeyMethod) {

		Type explicitLambdaParameterType = analysisResult.getExplicitLambdaParameterType()
			.orElse(null);
		String lambdaParameterIdentifier = analysisResult.getFirstLambdaParameterIdentifier();
		if (explicitLambdaParameterType != null
				&& isLambdaParameterTypeRequired(explicitLambdaParameterType, lambda)) {
			return createLambdaExpression(
					(Type) astRewrite.createCopyTarget(explicitLambdaParameterType), lambdaParameterIdentifier,
					comparisonKeyMethod.getName());
		}
		CastExpression parentCastExpression = analysisResult.getParentCastExpression()
			.orElse(null);
		if (parentCastExpression != null) {
			Type typeArgumentFromParentCastExpression = newTypeFromParentCastExpressionTypeArgument(
					parentCastExpression);
			return createLambdaExpression(typeArgumentFromParentCastExpression,
					lambdaParameterIdentifier, comparisonKeyMethod.getName());
		}

		ITypeBinding implicitLambdaParameterType = analysisResult.getImplicitLambdaParameterType();
		if (analysisResult.isReversed()) {
			Type newExplicitLambdaParameterType = createTypeWithOptionalArguments(implicitLambdaParameterType);
			return createLambdaExpression(newExplicitLambdaParameterType,
					lambdaParameterIdentifier, comparisonKeyMethod.getName());
		}
		return createExpressionMethodReference(implicitLambdaParameterType,
				comparisonKeyMethod.getName());
	}

	@SuppressWarnings("unchecked")
	private Type createTypeWithOptionalArguments(ITypeBinding typeBinding) {
		AST ast = astRewrite.getAST();
		Name typeName = createLambdaParameterTypeName(typeBinding, ast);
		SimpleType erasureSimpleType = ast.newSimpleType(typeName);
		ITypeBinding[] typeBindingArguments = typeBinding.getTypeArguments();
		if (typeBindingArguments.length == 0) {
			return erasureSimpleType;
		}
		ParameterizedType parameterizedType = ast.newParameterizedType(erasureSimpleType);
		List<Type> typeArguments = parameterizedType.typeArguments();
		for (ITypeBinding typeBindingArgument : typeBindingArguments) {
			typeArguments.add(createTypeWithOptionalArguments(typeBindingArgument));
		}
		return parameterizedType;
	}

	private MethodInvocation findSimpleLambdaReplacement(LambdaExpression lambda,
			LambdaAnalysisResult analysisResult) {

		MethodInvocation comparatorMethodInvocation;
		if (analysisResult.isReversed()) {
			comparatorMethodInvocation = createComparatorMethodInvocation("reverseOrder"); //$NON-NLS-1$
		} else {
			comparatorMethodInvocation = createComparatorMethodInvocation("naturalOrder"); //$NON-NLS-1$
		}
		Type explicitLambdaParameterType = analysisResult.getExplicitLambdaParameterType()
			.orElse(null);

		if (explicitLambdaParameterType != null
				&& isLambdaParameterTypeRequired(explicitLambdaParameterType, lambda)) {
			@SuppressWarnings("unchecked")
			List<Type> typeArguments = comparatorMethodInvocation.typeArguments();
			explicitLambdaParameterType = (Type) astRewrite.createCopyTarget(explicitLambdaParameterType);
			typeArguments.add(explicitLambdaParameterType);
			return comparatorMethodInvocation;
		}

		CastExpression parentCastExpression = analysisResult.getParentCastExpression()
			.orElse(null);
		if (parentCastExpression != null) {
			@SuppressWarnings("unchecked")
			List<Type> typeArguments = comparatorMethodInvocation.typeArguments();
			Type typeArgumentFromParentCastExpression = newTypeFromParentCastExpressionTypeArgument(
					parentCastExpression);
			typeArguments.add(typeArgumentFromParentCastExpression);
			return comparatorMethodInvocation;
		}
		return comparatorMethodInvocation;
	}

	private Type newTypeFromParentCastExpressionTypeArgument(CastExpression parentCastExpression) {
		Type castExpressionType = parentCastExpression.getType();
		if (castExpressionType.isParameterizedType()) {
			ParameterizedType parametrizedType = (ParameterizedType) castExpressionType;
			List<Type> castExpressionTypeArguments = ASTNodeUtil
				.convertToTypedList(parametrizedType.typeArguments(), Type.class);
			if (castExpressionTypeArguments.size() == 1) {
				Type typeToCopy = castExpressionTypeArguments.get(0);
				return (Type) astRewrite
					.createCopyTarget(typeToCopy);
			}
		}
		Name objectTypeName = addImport(java.lang.Object.class.getName());
		return astRewrite.getAST()
			.newSimpleType(objectTypeName);

	}

	private boolean isLambdaParameterTypeRequired(Type explicitLambdaParameterType, LambdaExpression lambda) {
		ITypeBinding explicitLambdaParameterTypeBinding = explicitLambdaParameterType.resolveBinding();
		ITypeBinding comparatorTypeBinding = null;
		ITypeBinding comparatorTypeArgumentBinding = null;
		StructuralPropertyDescriptor locationInParent = lambda.getLocationInParent();
		ASTNode parent = lambda.getParent();
		if (locationInParent == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			comparatorTypeBinding = ((VariableDeclarationFragment) parent).resolveBinding()
				.getType();

		} else if (locationInParent == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			Assignment assignment = ((Assignment) parent);
			comparatorTypeBinding = assignment.getLeftHandSide()
				.resolveTypeBinding();
		} else if (locationInParent == MethodInvocation.ARGUMENTS_PROPERTY) {
			MethodInvocation methodInvocation = (MethodInvocation) parent;
			int argumentIndex = methodInvocation.arguments()
				.indexOf(lambda);
			IMethodBinding parentInvocationMethodBinding = methodInvocation.resolveMethodBinding()
				.getMethodDeclaration();
			ITypeBinding[] parameterTypes = parentInvocationMethodBinding.getParameterTypes();
			int parameterIndex = Math.min(argumentIndex, parameterTypes.length - 1);
			comparatorTypeBinding = parameterTypes[parameterIndex];
		}

		if (comparatorTypeBinding != null) {
			ITypeBinding[] typeArguments = comparatorTypeBinding.getTypeArguments();
			if (typeArguments.length == 1) {
				comparatorTypeArgumentBinding = typeArguments[0];
				if (ClassRelationUtil.compareITypeBinding(comparatorTypeArgumentBinding,
						explicitLambdaParameterTypeBinding)) {
					return false;
				}
			}
		}
		return true;
	}

	private ExpressionMethodReference createExpressionMethodReference(ITypeBinding lambdaParameterType,
			String comparisonKeyMethodName) {
		AST ast = astRewrite.getAST();
		ExpressionMethodReference methodReference = ast.newExpressionMethodReference();
		methodReference.setName(ast.newSimpleName(comparisonKeyMethodName));
		Name lambdaParameterTypeName = createLambdaParameterTypeName(lambdaParameterType, ast);
		methodReference.setExpression(lambdaParameterTypeName);
		return methodReference;
	}

	private Name createLambdaParameterTypeName(ITypeBinding typeBinding, AST ast) {
		Name typeName;
		if (typeBinding.isTypeVariable() || typeBinding.isLocal()) {
			String name = typeBinding.getName();
			typeName = ast.newName(name);
		} else {
			ITypeBinding erasure = typeBinding.getErasure();
			String erasureQualifiedName = erasure.getQualifiedName();
			verifyImport(getCompilationUnit(), erasureQualifiedName);
			typeName = addImport(erasureQualifiedName);
		}
		return typeName;
	}

	private Expression createLambdaExpression(Type explicitLambdaParameterType, String lambdaParameterIdentifier,
			String comparisonKeyMethodName) {
		AST ast = astRewrite.getAST();
		LambdaExpression lambdaExpression = ast.newLambdaExpression();
		@SuppressWarnings("unchecked")
		List<VariableDeclaration> parameters = lambdaExpression.parameters();
		SingleVariableDeclaration lambdaParam = ast.newSingleVariableDeclaration();
		lambdaParam.setType(explicitLambdaParameterType);
		lambdaParam.setName(ast.newSimpleName(lambdaParameterIdentifier));
		parameters.add(lambdaParam);
		MethodInvocation lambdaBodyAsMethodInvocation = ast.newMethodInvocation();
		lambdaBodyAsMethodInvocation.setName(ast.newSimpleName(comparisonKeyMethodName));
		lambdaBodyAsMethodInvocation.setExpression(ast.newSimpleName(lambdaParameterIdentifier));
		lambdaExpression.setBody(lambdaBodyAsMethodInvocation);
		return lambdaExpression;
	}

	private String getComparatorMethodName(IMethodBinding comparisonKeyMethod) {
		ITypeBinding returnType = comparisonKeyMethod.getReturnType();
		if (ClassRelationUtil.isContentOfType(returnType, java.lang.Integer.class.getName())) {
			return "comparingInt"; //$NON-NLS-1$
		}
		if (ClassRelationUtil.isContentOfType(returnType, java.lang.Long.class.getName())) {
			return "comparingLong"; //$NON-NLS-1$
		}
		if (ClassRelationUtil.isContentOfType(returnType, java.lang.Double.class.getName())) {
			return "comparingDouble"; //$NON-NLS-1$
		}
		return "comparing"; //$NON-NLS-1$
	}

	private MethodInvocation createComparatorMethodInvocation(String methodName) {
		AST ast = astRewrite.getAST();
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setName(ast.newSimpleName(methodName));
		Name comparatorTypeName = addImport(JAVA_UTIL_COMPARATOR);
		methodInvocation.setExpression(comparatorTypeName);
		return methodInvocation;
	}

	private MethodInvocation reverseComparatorMethodInvocation(MethodInvocation comparatorMethodInvocation) {
		AST ast = astRewrite.getAST();
		MethodInvocation reverseMethodInvocation = ast.newMethodInvocation();
		reverseMethodInvocation.setName(ast.newSimpleName("reversed")); //$NON-NLS-1$
		reverseMethodInvocation.setExpression(comparatorMethodInvocation);
		return reverseMethodInvocation;
	}
}