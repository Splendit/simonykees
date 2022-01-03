package eu.jsparrow.core.visitor.assertj.dedicated;

import static eu.jsparrow.core.visitor.assertj.dedicated.ReplaceBooleanAssertionsByDedicatedAssertionsAnalyzer.IS_FALSE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

abstract class AbstractBooleanAssertionsAnalyzer {

	static final String EQUALS = "equals"; //$NON-NLS-1$

	private final Map<String, String> mapToAssertJAssertions;
	private final Map<String, String> mapToNegatedAssertJAssertions;

	protected AbstractBooleanAssertionsAnalyzer(Map<String, String> mapToAssertJAssertions,
			Map<String, String> mapToNegatedAssertJAssertions) {
		Map<String, String> tmpMap = new HashMap<>();
		tmpMap.put(EQUALS, "isEqualTo"); //$NON-NLS-1$
		tmpMap.putAll(mapToAssertJAssertions);
		this.mapToAssertJAssertions = Collections.unmodifiableMap(tmpMap);

		Map<String, String> tmpNegatedMap = new HashMap<>();
		tmpNegatedMap.put(EQUALS, "isNotEqualTo"); //$NON-NLS-1$
		tmpNegatedMap.putAll(mapToNegatedAssertJAssertions);
		this.mapToNegatedAssertJAssertions = Collections.unmodifiableMap(tmpNegatedMap);
	}

	Optional<String> findAssertJAssertionName(IMethodBinding methodBinding,
			ITypeBinding invocationExpressionTypeBinding, String assertionName) {

		if (!isSupportedTypeForAssertion(invocationExpressionTypeBinding)) {
			return Optional.empty();
		}

		if (!analyzeMethodBinding(methodBinding)) {
			return Optional.empty();
		}

		String methodName = methodBinding.getName();
		if (assertionName.equals(IS_FALSE)) {
			return Optional.ofNullable(mapToNegatedAssertJAssertions.get(methodName));
		} else {
			return Optional.ofNullable(mapToAssertJAssertions.get(methodName));
		}
	}

	protected boolean analyzeMethodBinding(IMethodBinding methodBinding) {
		return analyzeEqualsMethodParameters(methodBinding);
	}

	private static boolean analyzeEqualsMethodParameters(IMethodBinding methodBinding) {
		String methodName = methodBinding.getName();
		if (!methodName.equals(EQUALS)) {
			return true;
		}
		ITypeBinding[] parameterTypes = methodBinding.getMethodDeclaration()
			.getParameterTypes();
		if (parameterTypes.length != 1) {
			return false;
		}
		return ClassRelationUtil.isContentOfType(parameterTypes[0], Object.class.getName());
	}

	protected abstract boolean isSupportedTypeForAssertion(ITypeBinding typeBinding);

}
