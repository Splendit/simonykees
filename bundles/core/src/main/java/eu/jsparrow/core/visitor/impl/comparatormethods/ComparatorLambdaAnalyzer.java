package eu.jsparrow.core.visitor.impl.comparatormethods;

import static eu.jsparrow.core.visitor.impl.comparatormethods.UseComparatorMethodsASTVisitor.JAVA_LANG_COMPARABLE;
import static eu.jsparrow.core.visitor.impl.comparatormethods.UseComparatorMethodsASTVisitor.JAVA_UTIL_COMPARATOR;

import java.util.Collections;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

class ComparatorLambdaAnalyzer {

	private Expression compareToMethodExpression;
	private Expression compareToMethodArgument;

	boolean analyze(LambdaExpression lambda) {
		ITypeBinding lambdaTypeBinding = lambda.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(lambdaTypeBinding,
				JAVA_UTIL_COMPARATOR)) {
			return false;
		}

		ASTNode lambdaBody = lambda.getBody();
		if (lambdaBody.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return false;
		}

		MethodInvocation compareToMethodInvocation = (MethodInvocation) lambdaBody;
		if (!isCompareToMethodOfComparator(compareToMethodInvocation)) {
			return false;
		}

		compareToMethodExpression = compareToMethodInvocation.getExpression();
		if (compareToMethodExpression == null) {
			return false;
		}

		compareToMethodArgument = ASTNodeUtil
			.convertToTypedList(compareToMethodInvocation.arguments(), Expression.class)
			.get(0);

		return true;
	}

	private boolean isCompareToMethodOfComparator(MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();

		if (!methodBinding.getName()
			.equals("compareTo")) { //$NON-NLS-1$
			return false;
		}

		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		if (parameterTypes.length != 1) {
			return false;
		}

		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (!isComparable(declaringClass)) {
			return false;
		}

		ITypeBinding parameterType = parameterTypes[0];
		if (parameterType.isCapture()) {
			ITypeBinding wildcard = parameterType.getWildcard();
			parameterType = wildcard.getBound();
		}
		return isComparable(parameterType);
	}

	private boolean isComparable(ITypeBinding typeBinding) {
		boolean isComparable = ClassRelationUtil.isContentOfType(typeBinding, JAVA_LANG_COMPARABLE);
		boolean isInheritingComparable = ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
				Collections.singletonList(JAVA_LANG_COMPARABLE));

		return isComparable || isInheritingComparable;
	}

	public Expression getCompareToMethodExpression() {
		return compareToMethodExpression;
	}

	public Expression getCompareToMethodArgument() {
		return compareToMethodArgument;
	}
}
