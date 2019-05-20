package eu.jsparrow.rules.java10.factory.methods;

import static eu.jsparrow.rules.common.util.ASTNodeUtil.convertToTypedList;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class MethodInvocationArgumentAnalyser extends ArgumentAnalyser<MethodInvocation> {
	
	@Override
	public void analyzeArgument(MethodInvocation argumentMethod) {
		/*
		 * Check for: Arrays.asList(...)
		 * 
		 * Extract a list of entries for the factory method
		 */
		IMethodBinding methodBinding = argumentMethod.resolveMethodBinding();

		if ("asList".equals(methodBinding.getName()) && ClassRelationUtil //$NON-NLS-1$
			.isContentOfType(methodBinding.getDeclaringClass(), java.util.Arrays.class.getName())) {
			elements = convertToTypedList(argumentMethod.arguments(), Expression.class);
		}
	}

}
