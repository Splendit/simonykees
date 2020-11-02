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
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.visitor.impl.comparatormethods.UseComparatorMethodsAnalyzer.LambdaAnalysisResult;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * 
 * 
 * @since 3.22.0
 *
 */
public class UseComparatorMethodsASTVisitor extends AbstractAddImportASTVisitor {
	static final String JAVA_LANG_COMPARABLE = java.lang.Comparable.class.getName();
	static final String JAVA_UTIL_COMPARATOR = java.util.Comparator.class.getName();

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (continueVisiting) {
			verifyImport(compilationUnit, JAVA_UTIL_COMPARATOR);
		}
		return continueVisiting;
	}

	@Override
	public boolean visit(LambdaExpression lambda) {

		LambdaAnalysisResult lambdaAnalysisResult = new UseComparatorMethodsAnalyzer().analyze(lambda)
			.orElse(null);
		if (lambdaAnalysisResult == null) {
			return true;
		}

		MethodInvocation lambdaReplacement = createComparatorMethodInvocation(lambda, lambdaAnalysisResult);
		if (lambdaReplacement != null) {
			astRewrite.replace(lambda, lambdaReplacement, null);
			onRewrite();
		}
		return true;
	}

	private MethodInvocation createComparatorMethodInvocation(LambdaExpression lambda,
			LambdaAnalysisResult analysisResult) {

		List<VariableDeclaration> lambdaParameters = ASTNodeUtil.convertToTypedList(lambda.parameters(),
				VariableDeclaration.class);
		Type explicitLambdaParameterType = null;
		if (lambdaParameters.get(0)
			.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION) {
			explicitLambdaParameterType = ((SingleVariableDeclaration) lambdaParameters.get(0)).getType();
		}

		boolean isTypeCastExpression = lambda.getLocationInParent() == CastExpression.EXPRESSION_PROPERTY;
		Type castExpressionTypeArgument = null;
		if (isTypeCastExpression) {
			castExpressionTypeArgument = extractCastExpressionTypeArgument((CastExpression) lambda.getParent());
		}

		IMethodBinding comparisonKeyMethod = analysisResult.getComparisonKeyMethod()
			.orElse(null);

		if (comparisonKeyMethod == null) {
			return findSimpleLambdaReplacement(lambda, analysisResult);
		}

		ITypeBinding lambdaParameterType = lambdaParameters.get(0)
			.resolveBinding()
			.getType();

		Expression comparatorMethodArgument;
		String lambdaParameterIdentifier = lambdaParameters.get(0)
			.getName()
			.getIdentifier();

		if (explicitLambdaParameterType != null
				&& isLambdaParameterTypeRequired(explicitLambdaParameterType, lambda)) {
			comparatorMethodArgument = createLambdaExpression(explicitLambdaParameterType,
					lambdaParameterIdentifier, comparisonKeyMethod.getName());
		} else if (isTypeCastExpression) {
			if (castExpressionTypeArgument != null) {
				comparatorMethodArgument = createLambdaExpression(castExpressionTypeArgument,
						lambdaParameterIdentifier, comparisonKeyMethod.getName());
			} else {
				return null;
			}
		} else {
			comparatorMethodArgument = createExpressionMethodReference(lambdaParameterType,
					comparisonKeyMethod.getName());
		}
		String comparatorMethodName = getComparatorMethodName(comparisonKeyMethod);

		MethodInvocation comparatorMethodInvocation = createComparatorMethodInvocation(comparatorMethodName,
				comparatorMethodArgument);

		if (analysisResult.isReversed()) {
			return reverseComparatorMethodInvocation(comparatorMethodInvocation);
		}
		return comparatorMethodInvocation;
	}

	private MethodInvocation findSimpleLambdaReplacement(LambdaExpression lambda, LambdaAnalysisResult analysisResult) {
		String comparatorMethodName;
		if (analysisResult.isReversed()) {
			comparatorMethodName = "reverseOrder"; //$NON-NLS-1$
		} else {
			comparatorMethodName = "naturalOrder"; //$NON-NLS-1$
		}
		Type explicitLambdaParameterType = analysisResult.getExplicitLambdaParameterType()
			.orElse(null);

		if (explicitLambdaParameterType != null
				&& isLambdaParameterTypeRequired(explicitLambdaParameterType, lambda)) {
			return createComparatorMethodInvocation(comparatorMethodName, explicitLambdaParameterType);
		}

		boolean isTypeCastExpression = lambda.getLocationInParent() == CastExpression.EXPRESSION_PROPERTY;
		Type castExpressionTypeArgument = null;
		if (isTypeCastExpression) {
			castExpressionTypeArgument = extractCastExpressionTypeArgument((CastExpression) lambda.getParent());
		}

		if (isTypeCastExpression) {
			if (castExpressionTypeArgument != null) {
				return createComparatorMethodInvocation(comparatorMethodName, castExpressionTypeArgument);
			} else {
				return null;
			}
		}
		return createComparatorMethodInvocation(comparatorMethodName);

	}

	private Type extractCastExpressionTypeArgument(CastExpression castExpression) {
		Type castExpressionType = castExpression.getType();
		if (castExpressionType.isParameterizedType()) {
			ParameterizedType parametrizedType = (ParameterizedType) castExpressionType;
			List<Type> castExpressionTypeArguments = ASTNodeUtil
				.convertToTypedList(parametrizedType.typeArguments(), Type.class);
			if (castExpressionTypeArguments.size() == 1) {
				return castExpressionTypeArguments.get(0);
			}
		}
		return null;
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
			MethodDeclaration methodDeclaration = (MethodDeclaration) getCompilationUnit()
				.findDeclaringNode(methodInvocation.resolveMethodBinding());
			List<SingleVariableDeclaration> parameterDeclarations = ASTNodeUtil
				.convertToTypedList(methodDeclaration.parameters(), SingleVariableDeclaration.class);
			int parameterIndex = Math.min(argumentIndex, parameterDeclarations.size() - 1);
			SingleVariableDeclaration parameterDeclaration = parameterDeclarations.get(parameterIndex);
			comparatorTypeBinding = parameterDeclaration.getType()
				.resolveBinding();
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

		Name lambdaParameterTypeName;
		ITypeBinding lambdaParameterTypeErasure = lambdaParameterType.getErasure();
		if (lambdaParameterType.isLocal()) {
			lambdaParameterTypeName = ast.newSimpleName(lambdaParameterTypeErasure.getName());
		} else {
			String qualifiedName = lambdaParameterTypeErasure.getQualifiedName();
			verifyImport(getCompilationUnit(), qualifiedName);
			lambdaParameterTypeName = addImport(qualifiedName);
		}
		methodReference.setExpression(lambdaParameterTypeName);
		return methodReference;
	}

	private Expression createLambdaExpression(Type explicitLambdaParameterType, String lambdaParameterIdentifier,
			String comparisonKeyMethodName) {
		AST ast = astRewrite.getAST();
		LambdaExpression lambdaExpression = ast.newLambdaExpression();
		@SuppressWarnings("unchecked")
		List<VariableDeclaration> parameters = lambdaExpression.parameters();
		SingleVariableDeclaration lambdaParam = ast.newSingleVariableDeclaration();
		lambdaParam.setType((Type) astRewrite.createCopyTarget(explicitLambdaParameterType));
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

	private MethodInvocation createComparatorMethodInvocation(String methodName, Type typeArgument) {
		MethodInvocation methodInvocation = createComparatorMethodInvocation(methodName);
		@SuppressWarnings("unchecked")
		List<Type> typeArguments = methodInvocation.typeArguments();
		typeArguments.add((Type) astRewrite.createCopyTarget(typeArgument));
		return methodInvocation;
	}

	private MethodInvocation createComparatorMethodInvocation(String comparatorMethodName,
			Expression methodArgument) {
		MethodInvocation comparatorMethodInvocation = createComparatorMethodInvocation(comparatorMethodName);
		@SuppressWarnings("unchecked")
		List<Expression> arguments = comparatorMethodInvocation.arguments();
		arguments.add(methodArgument);
		return comparatorMethodInvocation;
	}

	private MethodInvocation reverseComparatorMethodInvocation(MethodInvocation comparatorMethodInvocation) {
		AST ast = astRewrite.getAST();
		MethodInvocation reverseMethodInvocation = ast.newMethodInvocation();
		// reversed
		reverseMethodInvocation.setName(ast.newSimpleName("reversed")); //$NON-NLS-1$
		reverseMethodInvocation.setExpression(comparatorMethodInvocation);
		return reverseMethodInvocation;
	}
}