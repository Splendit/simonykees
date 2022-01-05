package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

@SuppressWarnings("nls")
class BooleanAssertionsAnalyzer {

	private static final String EQUALS = "equals"; //$NON-NLS-1$
	private static final String JAVA_UTIL = "java.util"; //$NON-NLS-1$

	private static final BooleanAssertionsAnalyzer STRING_ASSERTIONS_ANALYZER = createAnalyzerForIsTrue(
			BooleanAssertionsAnalyzer::isString, createStringAssertionsMapping());
	private static final BooleanAssertionsAnalyzer ITERABLE_ASSERTIONS_ANALYZER = createAnalyzerForIsTrue(
			BooleanAssertionsAnalyzer::isSupportedIterableType, createIterableAssertionsMapping());
	private static final BooleanAssertionsAnalyzer MAP_ASSERTIONS_ANALYZER = createAnalyzerForIsTrue(
			BooleanAssertionsAnalyzer::isSupportedMapType, createMapAssertionsMapping());
	private static final BooleanAssertionsAnalyzer OBJECT_ASSERTIONS_ANALYZER = createAnalyzerForIsTrue(
			BooleanAssertionsAnalyzer::isObject, new HashMap<>());

	private static final BooleanAssertionsAnalyzer NEGATED_STRING_ASSERTIONS_ANALYZER = createAnalyzerForIsFalse(
			BooleanAssertionsAnalyzer::isString, createNegatedStringAssertionsMapping());
	private static final BooleanAssertionsAnalyzer NEGATED_ITERABLE_ASSERTIONS_ANALYZER = createAnalyzerForIsFalse(
			BooleanAssertionsAnalyzer::isSupportedIterableType, createNegatedIterableAssertionsMapping());
	private static final BooleanAssertionsAnalyzer NEGATED_MAP_ASSERTIONS_ANALYZER = createAnalyzerForIsFalse(
			BooleanAssertionsAnalyzer::isSupportedMapType, createNegatedMapAssertionsMapping());
	private static final BooleanAssertionsAnalyzer NEGATED_OBJECT_ASSERTIONS_ANALYZER = createAnalyzerForIsFalse(
			BooleanAssertionsAnalyzer::isObject, new HashMap<>());

	private final Map<String, String> mapToAssertJAssertions;
	private final Predicate<ITypeBinding> supportedTypeBindingPredicate;

	static boolean isString(ITypeBinding typeBinding) {
		return ClassRelationUtil.isContentOfType(typeBinding, String.class.getName());
	}

	private static Map<String, String> createStringAssertionsMapping() {
		Map<String, String> map = new HashMap<>();
		map.put("equalsIgnoreCase", "isEqualToIgnoringCase");
		map.put("startsWith", "startsWith");
		map.put("contains", "contains");
		map.put("endsWith", "endsWith");
		map.put("matches", "matches");
		map.put("isEmpty", "isEmpty");
		map.put("isBlank", "isBlank");
		return map;
	}

	private static Map<String, String> createNegatedStringAssertionsMapping() {
		Map<String, String> map = new HashMap<>();
		map.put("equalsIgnoreCase", "isNotEqualToIgnoringCase");
		map.put("startsWith", "doesNotStartWith");
		map.put("contains", "doesNotContain");
		map.put("endsWith", "doesNotEndWith");
		map.put("matches", "doesNotMatch");
		map.put("isEmpty", "isNotEmpty");
		map.put("isBlank", "isNotBlank");
		return map;
	}

	static boolean isSupportedIterableType(ITypeBinding typeBinding) {
		if (ClassRelationUtil.isContentOfType(typeBinding, java.lang.Iterable.class.getName())) {
			return true;
		}

		String packageName = typeBinding.getPackage()
			.getName();
		return packageName.equals(JAVA_UTIL)
				&& ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
						Arrays.asList(java.lang.Iterable.class.getName()));
	}

	private static Map<String, String> createNegatedIterableAssertionsMapping() {
		Map<String, String> map = new HashMap<>();
		map.put("contains", "doesNotContain");
		map.put("isEmpty", "isNotEmpty");
		return map;
	}

	private static Map<String, String> createIterableAssertionsMapping() {
		Map<String, String> map = new HashMap<>();
		map.put("contains", "contains");
		map.put("containsAll", "containsAll");
		map.put("isEmpty", "isEmpty");
		return map;
	}

	static boolean isSupportedMapType(ITypeBinding typeBinding) {
		if (ClassRelationUtil.isContentOfType(typeBinding, java.util.Map.class.getName())) {
			return true;
		}

		String packageName = typeBinding.getPackage()
			.getName();
		return packageName.equals(JAVA_UTIL)
				&& ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
						Arrays.asList(java.util.Map.class.getName()));
	}

	private static Map<String, String> createNegatedMapAssertionsMapping() {
		Map<String, String> map = new HashMap<>();
		map.put("containsKey", "doesNotContainKey");
		map.put("containsValue", "doesNotContainValue");
		map.put("isEmpty", "isNotEmpty");
		return map;
	}

	private static Map<String, String> createMapAssertionsMapping() {
		Map<String, String> map = new HashMap<>();
		map.put("containsKey", "containsKey");
		map.put("containsValue", "containsValue");
		map.put("isEmpty", "isEmpty");
		return map;
	}

	static boolean isObject(ITypeBinding typeBinding) {
		return ClassRelationUtil.isContentOfType(typeBinding, Object.class.getName());
	}

	static Optional<String> findNewAssertionNameForIsTrue(String assertionMethodName,
			ITypeBinding newAssertThatArgumentTypeBinding, IMethodBinding assertThatArgumentMethodBinding) {

		Optional<String> optionalNewAssertionName = STRING_ASSERTIONS_ANALYZER
			.findAssertJAssertionName(assertThatArgumentMethodBinding, newAssertThatArgumentTypeBinding);
		if (optionalNewAssertionName.isPresent()) {
			return optionalNewAssertionName;
		}

		optionalNewAssertionName = ITERABLE_ASSERTIONS_ANALYZER
			.findAssertJAssertionName(assertThatArgumentMethodBinding, newAssertThatArgumentTypeBinding);

		if (optionalNewAssertionName.isPresent()) {
			return optionalNewAssertionName;
		}

		optionalNewAssertionName = MAP_ASSERTIONS_ANALYZER
			.findAssertJAssertionName(assertThatArgumentMethodBinding, newAssertThatArgumentTypeBinding);

		if (optionalNewAssertionName.isPresent()) {
			return optionalNewAssertionName;
		}

		return OBJECT_ASSERTIONS_ANALYZER.findAssertJAssertionName(assertThatArgumentMethodBinding,
				newAssertThatArgumentTypeBinding);
	}

	static Optional<String> findNewAssertionNameForIsFalse(String assertionMethodName,
			ITypeBinding newAssertThatArgumentTypeBinding, IMethodBinding assertThatArgumentMethodBinding) {

		Optional<String> optionalNewAssertionName = NEGATED_STRING_ASSERTIONS_ANALYZER
			.findAssertJAssertionName(assertThatArgumentMethodBinding, newAssertThatArgumentTypeBinding);
		if (optionalNewAssertionName.isPresent()) {
			return optionalNewAssertionName;
		}

		optionalNewAssertionName = NEGATED_ITERABLE_ASSERTIONS_ANALYZER
			.findAssertJAssertionName(assertThatArgumentMethodBinding, newAssertThatArgumentTypeBinding);

		if (optionalNewAssertionName.isPresent()) {
			return optionalNewAssertionName;
		}

		optionalNewAssertionName = NEGATED_MAP_ASSERTIONS_ANALYZER
			.findAssertJAssertionName(assertThatArgumentMethodBinding, newAssertThatArgumentTypeBinding);

		if (optionalNewAssertionName.isPresent()) {
			return optionalNewAssertionName;
		}

		return NEGATED_OBJECT_ASSERTIONS_ANALYZER.findAssertJAssertionName(assertThatArgumentMethodBinding,
				newAssertThatArgumentTypeBinding);
	}

	private static BooleanAssertionsAnalyzer createAnalyzerForIsTrue(
			Predicate<ITypeBinding> supportedTypeBindingPredicate,
			Map<String, String> mapToAssertJAssertions) {

		Map<String, String> tmpMap = new HashMap<>();
		tmpMap.put(EQUALS, "isEqualTo"); //$NON-NLS-1$
		tmpMap.putAll(mapToAssertJAssertions);
		return new BooleanAssertionsAnalyzer(supportedTypeBindingPredicate, Collections.unmodifiableMap(tmpMap));
	}

	private static BooleanAssertionsAnalyzer createAnalyzerForIsFalse(
			Predicate<ITypeBinding> supportedTypeBindingPredicate,
			Map<String, String> mapToAssertJAssertions) {

		Map<String, String> tmpMap = new HashMap<>();
		tmpMap.put(EQUALS, "isNotEqualTo"); //$NON-NLS-1$
		tmpMap.putAll(mapToAssertJAssertions);
		return new BooleanAssertionsAnalyzer(supportedTypeBindingPredicate, Collections.unmodifiableMap(tmpMap));
	}

	private BooleanAssertionsAnalyzer(Predicate<ITypeBinding> supportedTypeBindingPredicate,
			Map<String, String> mapToAssertJAssertions) {
		this.supportedTypeBindingPredicate = supportedTypeBindingPredicate;
		this.mapToAssertJAssertions = mapToAssertJAssertions;
	}

	Optional<String> findAssertJAssertionName(IMethodBinding methodBinding,
			ITypeBinding invocationExpressionTypeBinding) {

		if (!supportedTypeBindingPredicate.test(invocationExpressionTypeBinding)) {
			return Optional.empty();
		}

		if (!analyzeMethodBinding(methodBinding)) {
			return Optional.empty();
		}

		String methodName = methodBinding.getName();
		return Optional.ofNullable(mapToAssertJAssertions.get(methodName));

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
}
