package eu.jsparrow.core.visitor.impl;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;

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

	private static final int PARAM_USAGE_NATRURAL_ORDER = 0;
	private static final int PARAM_USAGE_REVERSE_ORDER = 1;
	private static final int PARAM_USAGE_INVALID = -1;

	private static final String JAVA_LANG_COMPARABLE = java.lang.Comparable.class.getName();
	private static final String JAVA_UTIL_COMPARATOR = java.util.Comparator.class.getName();

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

		ITypeBinding lambdaTypeBinding = lambda.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(lambdaTypeBinding, JAVA_UTIL_COMPARATOR)) {
			return true;
		}

		MethodInvocation lambdaReplacement = createComparatorMethodInvocation(lambda);
		if (lambdaReplacement != null) {
			astRewrite.replace(lambda, lambdaReplacement, null);
			onRewrite();
		}
		return true;
	}

	private MethodInvocation createComparatorMethodInvocation(LambdaExpression lambda) {

		MethodInvocation compareToMethodInvocation = extractCompareToInvocation(lambda);
		if (compareToMethodInvocation == null) {
			return null;
		}

		Expression compareToMethodExpression = compareToMethodInvocation.getExpression();
		if (compareToMethodExpression == null) {
			return null;
		}

		Expression compareToMethodArgument = ASTNodeUtil
			.convertToTypedList(compareToMethodInvocation.arguments(), Expression.class)
			.get(0);

		List<VariableDeclaration> lambdaParameters = ASTNodeUtil.convertToTypedList(lambda.parameters(),
				VariableDeclaration.class);
		Type explicitLambdaParameterType = null;
		if (lambdaParameters.get(0)
			.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION) {
			explicitLambdaParameterType = ((SingleVariableDeclaration) lambdaParameters.get(0)).getType();
		}

		if (compareToMethodExpression.getNodeType() == ASTNode.SIMPLE_NAME
				&& compareToMethodArgument.getNodeType() == ASTNode.SIMPLE_NAME) {

			int lambdaParameterUsage = findUsageOfBothLambdaParameters(lambdaParameters,
					(SimpleName) compareToMethodExpression, (SimpleName) compareToMethodArgument);
			if (lambdaParameterUsage == PARAM_USAGE_INVALID) {
				return null;
			}
			String comparatorMethodName;
			if (lambdaParameterUsage == PARAM_USAGE_REVERSE_ORDER) {
				comparatorMethodName = "reverseOrder"; //$NON-NLS-1$
			} else {
				comparatorMethodName = "naturalOrder"; //$NON-NLS-1$
			}
			if (explicitLambdaParameterType != null) {
				return createComparatorMethodInvocation(comparatorMethodName, explicitLambdaParameterType);
			} else {
				return createComparatorMethodInvocation(comparatorMethodName);
			}
		}

		if (compareToMethodExpression.getNodeType() == ASTNode.METHOD_INVOCATION
				&& compareToMethodArgument.getNodeType() == ASTNode.METHOD_INVOCATION) {

			MethodInvocation invocationLeftHS = (MethodInvocation) compareToMethodExpression;
			MethodInvocation invocationRightHS = (MethodInvocation) compareToMethodArgument;
			int lambdaParameterUsage = findUsageOfBothLambdaParameters(lambdaParameters, invocationLeftHS,
					invocationRightHS);
			if (lambdaParameterUsage == PARAM_USAGE_INVALID) {
				return null;
			}

			if (!isEquivalentComparisonKeyMethod(invocationLeftHS, invocationRightHS)) {
				return null;
			}
			IMethodBinding comparisonKeyMethod = invocationLeftHS
				.resolveMethodBinding();

			if (comparisonKeyMethod.getParameterTypes().length != 0) {
				return null;
			}

			ITypeBinding lambdaParameterType = lambdaParameters.get(0)
				.resolveBinding()
				.getType();

			Expression comparatorMethodArgument;
			if (explicitLambdaParameterType != null) {
				String lambdaParameterIdentifier =  lambdaParameters.get(0).getName().getIdentifier();
				comparatorMethodArgument = createLambdaExpression(explicitLambdaParameterType, comparisonKeyMethod, lambdaParameterIdentifier);
			} else {
				comparatorMethodArgument = createExpressionMethodReference(lambdaParameterType,
						comparisonKeyMethod);
			}
			String comparatorMethodName = getComparatorMethodName(comparisonKeyMethod);

			MethodInvocation comparatorMethodInvocation = createComparatorMethodInvocation(comparatorMethodName,
					comparatorMethodArgument);

			if (lambdaParameterUsage == PARAM_USAGE_REVERSE_ORDER) {
				return reverseComparatorMethodInvocation(comparatorMethodInvocation);
			}
			return comparatorMethodInvocation;

		}
		return null;
	}

	private int indexOfSimpleParameterName(List<VariableDeclaration> lambdaParameters, SimpleName simpleName) {

		for (int i = 0; i < lambdaParameters.size(); i++) {
			String parameterIdentifier = lambdaParameters.get(i)
				.getName()
				.getIdentifier();
			if (simpleName.getIdentifier()
				.equals(parameterIdentifier)) {
				return i;
			}
		}
		return -1;
	}

	private int findUsageOfBothLambdaParameters(List<VariableDeclaration> lambdaParameters,
			SimpleName simpleNameLeftHS, SimpleName simpleNameRightHS) {
		int indexOfLeftHS = indexOfSimpleParameterName(lambdaParameters, simpleNameLeftHS);
		int indexOfRightHS = indexOfSimpleParameterName(lambdaParameters, simpleNameRightHS);
		if (indexOfLeftHS == 0 && indexOfRightHS == 1) {
			return PARAM_USAGE_NATRURAL_ORDER;
		}
		if (indexOfLeftHS == 1 && indexOfRightHS == 0) {
			return PARAM_USAGE_REVERSE_ORDER;
		}
		return PARAM_USAGE_INVALID;
	}

	private int findUsageOfBothLambdaParameters(List<VariableDeclaration> lambdaParameters,
			MethodInvocation invocationLeftHS, MethodInvocation invocationRightHS) {
		if (invocationLeftHS.getExpression() == null || invocationLeftHS.getExpression()
			.getNodeType() != ASTNode.SIMPLE_NAME) {
			return PARAM_USAGE_INVALID;
		}
		if (invocationRightHS.getExpression() == null || invocationRightHS.getExpression()
			.getNodeType() != ASTNode.SIMPLE_NAME) {
			return PARAM_USAGE_INVALID;
		}
		return findUsageOfBothLambdaParameters(lambdaParameters,
				(SimpleName) invocationLeftHS.getExpression(),
				(SimpleName) invocationRightHS.getExpression());

	}

	private boolean isEquivalentComparisonKeyMethod(MethodInvocation lhs, MethodInvocation rhs) {
		IMethodBinding lhsMethodBinding = lhs.resolveMethodBinding();
		IMethodBinding rhsMethodBinding = rhs.resolveMethodBinding();

		return lhsMethodBinding.getName()
			.equals(rhsMethodBinding.getName())
				&& ClassRelationUtil.compareITypeBinding(lhsMethodBinding.getDeclaringClass(),
						rhsMethodBinding.getDeclaringClass());
	}

	private boolean isComparable(ITypeBinding typeBinding) {
		boolean isComparable = ClassRelationUtil.isContentOfType(typeBinding, JAVA_LANG_COMPARABLE);
		boolean isInheritingComparable = ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
				Collections.singletonList(JAVA_LANG_COMPARABLE));

		return isComparable || isInheritingComparable;
	}

	private MethodInvocation extractCompareToInvocation(LambdaExpression lambda) {
		ASTNode lambdaBody = lambda.getBody();
		MethodInvocation methodInvocation = null;
		if (lambdaBody.getNodeType() == ASTNode.METHOD_INVOCATION) {
			methodInvocation = (MethodInvocation) lambdaBody;
		}
		if (methodInvocation == null) {
			return null;
		}

		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();

		if (!methodBinding.getName()
			.equals("compareTo")) { //$NON-NLS-1$
			return null;
		}

		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		if (parameterTypes.length != 1) {
			return null;
		}

		// This is needed
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (!isComparable(declaringClass)) {
			return null;
		}

		ITypeBinding parameterType = parameterTypes[0];
		if (parameterType.isCapture()) {
			ITypeBinding wildcard = parameterType.getWildcard();
			ITypeBinding bound = wildcard.getBound();
			if (isComparable(bound)) {
				return methodInvocation;
			}
		}

		if (!isComparable(parameterType)) {
			return null;
		}

		return methodInvocation;
	}

	private ExpressionMethodReference createExpressionMethodReference(ITypeBinding lambdaParameterType,
			IMethodBinding comparisonKeyMethod) {
		AST ast = astRewrite.getAST();
		ExpressionMethodReference methodReference = ast.newExpressionMethodReference();
		methodReference.setName(ast.newSimpleName(comparisonKeyMethod.getName()));

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

	private Expression createLambdaExpression(Type explicitLambdaParameterType, IMethodBinding comparisonKeyMethod, String lambdaParameterIdentifier) {
		AST ast = astRewrite.getAST();
		LambdaExpression lambdaExpression = ast.newLambdaExpression();
		@SuppressWarnings("unchecked")
		List<VariableDeclaration> parameters = lambdaExpression.parameters();
		SingleVariableDeclaration lambdaParam = ast.newSingleVariableDeclaration();
		lambdaParam.setType((Type) astRewrite.createCopyTarget(explicitLambdaParameterType));
		lambdaParam.setName(ast.newSimpleName(lambdaParameterIdentifier));
		parameters.add(lambdaParam);
		MethodInvocation lambdaBodyAsMethodInvocation = ast.newMethodInvocation();
		lambdaBodyAsMethodInvocation.setName(ast.newSimpleName(comparisonKeyMethod.getName()));
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
