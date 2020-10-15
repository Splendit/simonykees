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
			int indexOfLeftHS = indexOfSimpleParameterName(lambdaParameters, (SimpleName) compareToMethodExpression);
			int indexOfRightHS = indexOfSimpleParameterName(lambdaParameters, (SimpleName) compareToMethodArgument);
			if (indexOfLeftHS == 0 && indexOfRightHS == 1) {
				return createComparatorMethodInvocation("naturalOrder"); //$NON-NLS-1$
			}
			if (indexOfLeftHS == 1 && indexOfRightHS == 0) {
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
			IMethodBinding comparisonKeyMethod = invocationLeftHS
				.resolveMethodBinding();

			if (comparisonKeyMethod.getParameterTypes().length != 0) {
				return null;
			}

			ITypeBinding lambdaParameterType = lambdaParameters.get(0)
				.resolveBinding()
				.getType();
			SimpleName simpleNameLeftHS = (SimpleName) invocationLeftHS.getExpression();
			SimpleName simpleNameRightHS = (SimpleName) invocationRightHS.getExpression();
			int indexOfLeftHS = indexOfSimpleParameterName(lambdaParameters, simpleNameLeftHS);
			int indexOfRightHS = indexOfSimpleParameterName(lambdaParameters, simpleNameRightHS);

			if (indexOfLeftHS == 0 && indexOfRightHS == 1) {
				return createComparatorMethodInvocation(lambdaParameterType, comparisonKeyMethod, false);
			}
			if (indexOfLeftHS == 1 && indexOfRightHS == 0) {
				return createComparatorMethodInvocation(lambdaParameterType, comparisonKeyMethod, true);
			}
		}
		return null;
	}

	private MethodInvocation createComparatorMethodInvocation(String methodName) {
		AST ast = astRewrite.getAST();
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setName(ast.newSimpleName(methodName));
		Name comparatorTypeName = addImport(JAVA_UTIL_COMPARATOR);
		methodInvocation.setExpression(comparatorTypeName);
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
		ITypeBinding lambdaParameterTypeErasure = lambdaParameterType.getErasure();
		if (lambdaParameterType.isLocal()) {
			lambdaParameterTypeName = ast.newSimpleName(lambdaParameterTypeErasure.getName());
		} else {
			String qualifiedName = lambdaParameterTypeErasure.getQualifiedName();
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
}
