package eu.jsparrow.core.visitor.impl.comparatormethods;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

class ComparatorLambdaAnalyzer {
	
	boolean analyze(LambdaExpression lambda) {
		ITypeBinding lambdaTypeBinding = lambda.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(lambdaTypeBinding,
				UseComparatorMethodsASTVisitor.JAVA_UTIL_COMPARATOR)) {
			return false;
		}

		return true;
	}
}
