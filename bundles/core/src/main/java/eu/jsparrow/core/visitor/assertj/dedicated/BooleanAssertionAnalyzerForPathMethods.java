package eu.jsparrow.core.visitor.assertj.dedicated;

import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.ENDS_WITH;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_ABSOLUTE;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_RELATIVE;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.STARTS_WITH;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_PATH;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Analyzer for boolean assertions on invocations of methods of
 * {@link java.nio.file.Path}.
 * 
 * @since 4.7.0
 *
 */
class BooleanAssertionAnalyzerForPathMethods extends BooleanAssertionOnInvocationAnalyzer {

	private static Map<String, String> createMethodMapping() {
		Map<String, String> map = new HashMap<>();
		map.put(IS_ABSOLUTE, IS_ABSOLUTE);
		map.put(STARTS_WITH, STARTS_WITH);
		map.put(ENDS_WITH, ENDS_WITH);
		return map;
	}

	private static Map<String, String> createNegatedMethodMapping() {
		HashMap<String, String> negatedMap = new HashMap<>();
		negatedMap.put(IS_ABSOLUTE, IS_RELATIVE);
		return negatedMap;
	}

	BooleanAssertionAnalyzerForPathMethods() {
		super(IS_PATH, createMethodMapping(), createNegatedMethodMapping());
	}

	@Override
	protected boolean analyzeMethodBinding(IMethodBinding methodBinding) {
		return super.analyzeMethodBinding(methodBinding) && analyzePathMethodParameter(methodBinding);
	}

	private boolean analyzePathMethodParameter(IMethodBinding methodBinding) {

		String methodName = methodBinding.getName();
		if (methodName.equals(STARTS_WITH) || (methodName.equals(ENDS_WITH))) {
			ITypeBinding[] parameterTypes = methodBinding.getMethodDeclaration()
				.getParameterTypes();
			return parameterTypes.length == 1
					&& ClassRelationUtil.isContentOfType(parameterTypes[0], java.nio.file.Path.class.getName());
		}
		return true;
	}
}
