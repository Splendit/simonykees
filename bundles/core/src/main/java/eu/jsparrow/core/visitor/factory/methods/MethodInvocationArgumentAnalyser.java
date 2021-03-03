package eu.jsparrow.core.visitor.factory.methods;

import static eu.jsparrow.rules.common.util.ASTNodeUtil.convertToTypedList;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Analyzes collection initialization of this form:
 * 
 * <pre>
 * {
 * 	&#64;code
 * 	List<String> list = Collections.unmodifiableList(Arrays.asList("1", "2"));
 * }
 * </pre>
 * 
 * Verifies the precondition for transforming this pattern to an initialization
 * using factory methods for collections. Saves the inserted elements.
 * 
 * @since 3.6.0
 *
 */
public class MethodInvocationArgumentAnalyser extends ArgumentAnalyser<MethodInvocation> {

	@Override
	public void analyzeArgument(MethodInvocation argumentMethod) {
		/*
		 * Check for: Arrays.asList(...)
		 * 
		 * Extract a list of entries for the factory method
		 */
		IMethodBinding methodBinding = argumentMethod.resolveMethodBinding();

		if (methodBinding != null && "asList".equals(methodBinding.getName()) && ClassRelationUtil //$NON-NLS-1$
			.isContentOfType(methodBinding.getDeclaringClass(), java.util.Arrays.class.getName())) {
			elements = convertToTypedList(argumentMethod.arguments(), Expression.class);
		}
	}

}
