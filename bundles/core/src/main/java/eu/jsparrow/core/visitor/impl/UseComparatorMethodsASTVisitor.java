package eu.jsparrow.core.visitor.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
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

	private static final String JAVA_LANG_COMPARABLE = java.lang.Comparable.class.getName();

	@Override
	public boolean visit(LambdaExpression lambda) {

		MethodInvocation lambdaReplacement = createComparatorMethodInvocation(lambda);
		if (lambdaReplacement != null) {
			astRewrite.replace(lambda, lambdaReplacement, null);
			onRewrite();
		}
		return true;
	}

	private MethodInvocation createComparatorMethodInvocation(LambdaExpression lambda) {

		List<VariableDeclaration> lambdaParameters = ASTNodeUtil.convertToTypedList(lambda.parameters(),
				VariableDeclaration.class);

		if (!checkLambdaParameterList(lambdaParameters)) {
			return null;
		}

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

		if (compareToMethodExpression.getNodeType() == ASTNode.SIMPLE_NAME
				&& compareToMethodArgument.getNodeType() == ASTNode.SIMPLE_NAME) {
			SimpleName simpleNameLeftHS = (SimpleName) compareToMethodExpression;
			SimpleName simpleNameRightHS = (SimpleName) compareToMethodArgument;
			Order lambdaParameterUsageOrder = findLambdaParameterUsageOrder(lambdaParameters, simpleNameLeftHS,
					simpleNameRightHS);
			if (lambdaParameterUsageOrder == Order.NATURAL_ORDER) {
				return createComparatorMethodInvocation("naturalOrder"); //$NON-NLS-1$
			}
			if (lambdaParameterUsageOrder == Order.REVERSE_ORDER) {
				return createComparatorMethodInvocation("reverseOrder");//$NON-NLS-1$
			}
		}

		if (compareToMethodExpression.getNodeType() == ASTNode.METHOD_INVOCATION
				&& compareToMethodArgument.getNodeType() == ASTNode.METHOD_INVOCATION) {

			MethodInvocation invocationLeftHS = (MethodInvocation) compareToMethodExpression;
			MethodInvocation invocationRightHS = (MethodInvocation) compareToMethodArgument;
			if (invocationLeftHS.getExpression() == null || invocationLeftHS.getExpression()
				.getNodeType() != ASTNode.SIMPLE_NAME) {
				return null;
			}

			if (invocationRightHS.getExpression() == null || invocationRightHS.getExpression()
				.getNodeType() != ASTNode.SIMPLE_NAME) {
				return null;
			}
			if (!isEqivalentComparisonKeyMethod(invocationLeftHS, invocationRightHS)) {
				return null;
			}

			SimpleName simpleNameLeftHS = (SimpleName) invocationLeftHS.getExpression();
			SimpleName simpleNameRightHS = (SimpleName) invocationRightHS.getExpression();

			Order lambdaParameterUsageOrder = findLambdaParameterUsageOrder(lambdaParameters, simpleNameLeftHS,
					simpleNameRightHS);
			IMethodBinding comparisonKeyMethod = invocationLeftHS
				.resolveMethodBinding();
			ITypeBinding lambdaParameterType = lambdaParameters.get(0)
				.resolveBinding()
				.getType();
			if (lambdaParameterUsageOrder == Order.NATURAL_ORDER) {
				return createComparatorMethodInvocation(lambdaParameterType, comparisonKeyMethod, false);
			}
			if (lambdaParameterUsageOrder == Order.REVERSE_ORDER) {
				return createComparatorMethodInvocation(lambdaParameterType, comparisonKeyMethod, true);
			}
		}
		return null;
	}

	private MethodInvocation createComparatorMethodInvocation(String methodName) {
		AST ast = astRewrite.getAST();
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setName(ast.newSimpleName(methodName));
		methodInvocation.setExpression(ast.newSimpleName("Comparator")); //$NON-NLS-1$
		return methodInvocation;
	}

	private MethodInvocation createComparatorMethodInvocation(ITypeBinding lambdaParameterType,
			IMethodBinding comparisonKeyMethod,
			boolean reverseOrder) {
		AST ast = astRewrite.getAST();
		String methodName = getComparisonKeyMethodName(comparisonKeyMethod);
		MethodInvocation methodInvocation = createComparatorMethodInvocation(methodName);
		ExpressionMethodReference methodReference = ast.newExpressionMethodReference();
		methodReference.setName(ast.newSimpleName(comparisonKeyMethod.getName()));

		Name lambdaParameterTypeName;
		if (lambdaParameterType.isLocal()) {
			lambdaParameterTypeName = ast.newSimpleName(lambdaParameterType.getName());
		} else {
			String qualifiedName = lambdaParameterType.getQualifiedName();
			verifyImport(getCompilationUnit(), qualifiedName);
			lambdaParameterTypeName = addImport(qualifiedName);
		}
		methodReference.setExpression(lambdaParameterTypeName);

		@SuppressWarnings("unchecked")
		List<Expression> arguments = methodInvocation.arguments();
		arguments.add(methodReference);
		if (!reverseOrder) {
			return methodInvocation;
		}
		MethodInvocation reverseMethodInvocation = ast.newMethodInvocation();
		// reversed
		reverseMethodInvocation.setName(ast.newSimpleName("reversed")); //$NON-NLS-1$
		reverseMethodInvocation.setExpression(methodInvocation);
		return reverseMethodInvocation;
	}

	private String getComparisonKeyMethodName(IMethodBinding comparisonKeyMethod) {
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

	private Order findLambdaParameterUsageOrder(List<VariableDeclaration> lambdaParameters,
			SimpleName simpleNameLeftHS,
			SimpleName simpleNameRightHS) {

		List<String> lambdaParameterIdentidiers = lambdaParameters.stream()
			.map(VariableDeclaration::getName)
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toList());

		int indexOfLHS = lambdaParameterIdentidiers.indexOf(simpleNameLeftHS.getIdentifier());
		int indexOfRHS = lambdaParameterIdentidiers.indexOf(simpleNameRightHS.getIdentifier());
		if (indexOfLHS == 0 && indexOfRHS == 1) {
			return Order.NATURAL_ORDER;
		}
		if (indexOfLHS == 1 && indexOfRHS == 0) {
			return Order.REVERSE_ORDER;
		}
		return null;
	}

	private boolean isEqivalentComparisonKeyMethod(MethodInvocation lhs, MethodInvocation rhs) {
		IMethodBinding lhsMethodBinding = lhs.resolveMethodBinding();
		IMethodBinding rhsMethodBinding = rhs.resolveMethodBinding();

		return lhsMethodBinding.getName()
			.equals(rhsMethodBinding.getName())
				&& ClassRelationUtil.compareITypeBinding(lhsMethodBinding.getDeclaringClass(),
						rhsMethodBinding.getDeclaringClass());
	}

	private boolean checkLambdaParameterList(List<VariableDeclaration> lambdaParameters) {
		if (lambdaParameters.size() != 2) {
			return false;
		}
		ITypeBinding lhsType = lambdaParameters.get(0)
			.resolveBinding()
			.getType();
		ITypeBinding rhsType = lambdaParameters.get(1)
			.resolveBinding()
			.getType();

		return ClassRelationUtil.compareITypeBinding(lhsType, rhsType);
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

		if (!isComparable(parameterTypes[0])) {
			return null;
		}

		if (!isComparable(methodBinding.getDeclaringClass())) {
			return null;
		}

		return methodInvocation;
	}

	private enum Order {
		NATURAL_ORDER,
		REVERSE_ORDER
	}

}
