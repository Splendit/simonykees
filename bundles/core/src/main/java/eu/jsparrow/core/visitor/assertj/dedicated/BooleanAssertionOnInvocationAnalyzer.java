package eu.jsparrow.core.visitor.assertj.dedicated;

import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_FILE;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_OPTIONAL;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_OTHER_SUPPORTED_TYPE;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_PATH;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_PREDICATE;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_STREAM;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_STRING;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_SUPPORTED_ARRAY_TYPE;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_SUPPORTED_ITERABLE;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_SUPPORTED_MAP_TYPE;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_SUPPORTED_TEMPORAL_TYPE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

@SuppressWarnings("nls")
class BooleanAssertionOnInvocationAnalyzer {

	private static final Predicate<ITypeBinding> IS_SUPPORTED_ITERATOR_TYPE = SupportedAssertJAssertThatArgumentTypes::isSupportedIteratorTypeForAssertion;
	private static final String ENDS_WITH = "endsWith";
	private static final String STARTS_WITH = "startsWith";
	private static final String EQUALS = "equals";

	private static final BooleanAssertionOnInvocationAnalyzer STRING_ASSERTIONS_ANALYZER = createStringAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer ITERABLE_ASSERTIONS_ANALYZER = createIterableAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer MAP_ASSERTIONS_ANALYZER = createMapAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer PATH_ASSERTIONS_ANALYZER = createPathAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer FILE_ASSERTIONS_ANALYZER = createFileAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer OPTIONAL_ASSERTIONS_ANALYZER = createOptionalAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer DATE_AND_TIME_ASSERTIONS_ANALYZER = createDateAndTimeAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer STREAM_ASSERTIONS_ANALYZER = createStreamAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer ITERATOR_ASSERTIONS_ANALYZER = createIteratorTypesAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer PREDICATE_ASSERTIONS_ANALYZER = createPredicateAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer ARRAY_ASSERTIONS_ANALYZER = createArrayAssertionsAnalyzer();

	private static final BooleanAssertionOnInvocationAnalyzer OTHER_TYPES_ASSERTIONS_ANALYZER = createOtherTypesAssertionsAnalyzer();

	private final Map<String, String> mapToAssertJAssertions;
	private final Map<String, String> mapToNegatedAssertJAssertions;
	private final Predicate<ITypeBinding> supportedTypeBindingPredicate;

	private static BooleanAssertionOnInvocationAnalyzer createStringAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put("equalsIgnoreCase", "isEqualToIgnoringCase");
		map.put(STARTS_WITH, STARTS_WITH);
		map.put("contains", "contains");
		map.put(ENDS_WITH, ENDS_WITH);
		map.put("matches", "matches");
		map.put("isEmpty", "isEmpty");
		map.put("isBlank", "isBlank");
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put("equalsIgnoreCase", "isNotEqualToIgnoringCase");
		negatedMap.put(STARTS_WITH, "doesNotStartWith");
		negatedMap.put("contains", "doesNotContain");
		negatedMap.put(ENDS_WITH, "doesNotEndWith");
		negatedMap.put("matches", "doesNotMatch");
		negatedMap.put("isEmpty", "isNotEmpty");
		negatedMap.put("isBlank", "isNotBlank");
		return new BooleanAssertionOnInvocationAnalyzer(IS_STRING, map, negatedMap);
	}

	private static BooleanAssertionOnInvocationAnalyzer createIterableAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put("contains", "contains");
		map.put("containsAll", "containsAll");
		map.put("isEmpty", "isEmpty");
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put("contains", "doesNotContain");
		negatedMap.put("isEmpty", "isNotEmpty");
		return new BooleanAssertionOnInvocationAnalyzer(IS_SUPPORTED_ITERABLE, map, negatedMap);
	}

	private static BooleanAssertionOnInvocationAnalyzer createMapAssertionsAnalyzer() {
		Map<String, String> map1 = new HashMap<>();
		map1.put("containsKey", "containsKey");
		map1.put("containsValue", "containsValue");
		map1.put("isEmpty", "isEmpty");
		Map<String, String> map = map1;
		Map<String, String> map2 = new HashMap<>();
		map2.put("containsKey", "doesNotContainKey");
		map2.put("containsValue", "doesNotContainValue");
		map2.put("isEmpty", "isNotEmpty");
		Map<String, String> negatedMap = map2;
		return new BooleanAssertionOnInvocationAnalyzer(IS_SUPPORTED_MAP_TYPE, map, negatedMap);
	}

	private static BooleanAssertionOnInvocationAnalyzer createPathAssertionsAnalyzer() {

		Map<String, String> map = new HashMap<>();
		map.put("isAbsolute", "isAbsolute");
		map.put(STARTS_WITH, STARTS_WITH);
		map.put(ENDS_WITH, ENDS_WITH);
		HashMap<String, String> negatedMap = new HashMap<>();
		negatedMap.put("isAbsolute", "isRelative");

		return new BooleanAssertionOnInvocationAnalyzer(IS_PATH, map, negatedMap) {
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
		};
	}

	private static BooleanAssertionOnInvocationAnalyzer createFileAssertionsAnalyzer() {

		Map<String, String> map = new HashMap<>();
		map.put("exists", "exists"); // File
		map.put("isFile", "isFile"); // File
		map.put("isDirectory", "isDirectory"); // File
		// map.put("isHidden", "isHidden"); // File -- not supported
		map.put("isAbsolute", "isAbsolute"); // Path
		map.put("canRead", "canRead"); // File
		map.put("canWrite", "canWrite"); // File
		// map.put("canExecute", "canExecute"); // File -- not supported
		HashMap<String, String> negatedMap = new HashMap<>();
		negatedMap.put("isAbsolute", "isRelative");
		negatedMap.put("exists", "doesNotExist"); // File
		return new BooleanAssertionOnInvocationAnalyzer(IS_FILE, map, negatedMap);

	}

	private static BooleanAssertionOnInvocationAnalyzer createOptionalAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put("isEmpty", "isEmpty");
		map.put("isPresent", "isPresent");
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put("isEmpty", "isPresent");
		negatedMap.put("isPresent", "isEmpty");
		return new BooleanAssertionOnInvocationAnalyzer(IS_OPTIONAL, map, negatedMap);
	}

	private static BooleanAssertionOnInvocationAnalyzer createDateAndTimeAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put("after", "isAfter");
		map.put("before", "isBefore");
		map.put("isAfter", "isAfter");
		map.put("isBefore", "isBefore");
		// map.put("isSupported", "isSupported"); not supported
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put("after", "isBeforeOrEqualTo");
		negatedMap.put("before", "isAfterOrEqualTo");
		negatedMap.put("isAfter", "isBeforeOrEqualTo");
		negatedMap.put("isBefore", "isAfterOrEqualTo");

		return new BooleanAssertionOnInvocationAnalyzer(IS_SUPPORTED_TEMPORAL_TYPE, map, negatedMap);
	}

	private static BooleanAssertionOnInvocationAnalyzer createStreamAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put("allMatch", "allMatch");
		map.put("anyMatch", "anyMatch");
		map.put("noneMatch", "noneMatch");
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put("anyMatch", "noneMatch");
		negatedMap.put("noneMatch", "anyMatch");
		return new BooleanAssertionOnInvocationAnalyzer(IS_STREAM, map, negatedMap);
	}

	private static BooleanAssertionOnInvocationAnalyzer createIteratorTypesAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put("hasNext", "hasNext");
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put("hasNext", "isExhausted");
		return new BooleanAssertionOnInvocationAnalyzer(IS_SUPPORTED_ITERATOR_TYPE, map, negatedMap);
	}

	private static BooleanAssertionOnInvocationAnalyzer createPredicateAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put("test", "accepts");
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put("test", "rejects");
		return new BooleanAssertionOnInvocationAnalyzer(IS_PREDICATE, map, negatedMap);
	}

	private static BooleanAssertionOnInvocationAnalyzer createArrayAssertionsAnalyzer() {
		return new BooleanAssertionOnInvocationAnalyzer(IS_SUPPORTED_ARRAY_TYPE, new HashMap<>(), new HashMap<>());
	}

	private static BooleanAssertionOnInvocationAnalyzer createOtherTypesAssertionsAnalyzer() {
		return new BooleanAssertionOnInvocationAnalyzer(IS_OTHER_SUPPORTED_TYPE, new HashMap<>(), new HashMap<>());
	}

	static Optional<BooleanAssertionOnInvocationAnalyzer> findAnalyzer(ITypeBinding newAssertThatArgumentTypeBinding) {

		if (STRING_ASSERTIONS_ANALYZER.isSupportedForType(newAssertThatArgumentTypeBinding)) {
			return Optional.of(STRING_ASSERTIONS_ANALYZER);
		}
		if (ITERABLE_ASSERTIONS_ANALYZER.isSupportedForType(newAssertThatArgumentTypeBinding)) {
			return Optional.of(ITERABLE_ASSERTIONS_ANALYZER);
		}
		if (MAP_ASSERTIONS_ANALYZER.isSupportedForType(newAssertThatArgumentTypeBinding)) {
			return Optional.of(MAP_ASSERTIONS_ANALYZER);
		}
		if (PATH_ASSERTIONS_ANALYZER.isSupportedForType(newAssertThatArgumentTypeBinding)) {
			return Optional.of(PATH_ASSERTIONS_ANALYZER);
		}
		if (FILE_ASSERTIONS_ANALYZER.isSupportedForType(newAssertThatArgumentTypeBinding)) {
			return Optional.of(FILE_ASSERTIONS_ANALYZER);
		}
		if (OPTIONAL_ASSERTIONS_ANALYZER.isSupportedForType(newAssertThatArgumentTypeBinding)) {
			return Optional.of(OPTIONAL_ASSERTIONS_ANALYZER);
		}
		if (DATE_AND_TIME_ASSERTIONS_ANALYZER.isSupportedForType(newAssertThatArgumentTypeBinding)) {
			return Optional.of(DATE_AND_TIME_ASSERTIONS_ANALYZER);
		}
		if (STREAM_ASSERTIONS_ANALYZER.isSupportedForType(newAssertThatArgumentTypeBinding)) {
			return Optional.of(STREAM_ASSERTIONS_ANALYZER);
		}
		if (ITERATOR_ASSERTIONS_ANALYZER.isSupportedForType(newAssertThatArgumentTypeBinding)) {
			return Optional.of(ITERATOR_ASSERTIONS_ANALYZER);
		}
		if (PREDICATE_ASSERTIONS_ANALYZER.isSupportedForType(newAssertThatArgumentTypeBinding)) {
			return Optional.of(PREDICATE_ASSERTIONS_ANALYZER);
		}
		if (ARRAY_ASSERTIONS_ANALYZER.isSupportedForType(newAssertThatArgumentTypeBinding)) {
			return Optional.of(ARRAY_ASSERTIONS_ANALYZER);
		}
		if (OTHER_TYPES_ASSERTIONS_ANALYZER.isSupportedForType(newAssertThatArgumentTypeBinding)) {
			return Optional.of(OTHER_TYPES_ASSERTIONS_ANALYZER);
		}
		return Optional.empty();
	}

	static Optional<String> findNewAssertionNameForIsTrue(ITypeBinding newAssertThatArgumentTypeBinding,
			IMethodBinding assertThatArgumentMethodBinding) {

		BooleanAssertionOnInvocationAnalyzer analyzer = findAnalyzer(newAssertThatArgumentTypeBinding).orElse(null);
		if (analyzer != null) {
			return analyzer.findAssertJAssertionName(assertThatArgumentMethodBinding);
		}
		return Optional.empty();
	}

	static Optional<String> findNewAssertionNameForIsFalse(ITypeBinding newAssertThatArgumentTypeBinding,
			IMethodBinding assertThatArgumentMethodBinding) {

		BooleanAssertionOnInvocationAnalyzer analyzer = findAnalyzer(newAssertThatArgumentTypeBinding).orElse(null);
		if (analyzer != null) {
			return analyzer.findNegatedAssertJAssertionName(assertThatArgumentMethodBinding);
		}
		return Optional.empty();

	}

	static boolean isSupportedForInfixOrInstanceOf(ITypeBinding newAssertThatArgumentTypeBinding) {
		if (newAssertThatArgumentTypeBinding.isPrimitive()) {
			return true;
		}
		BooleanAssertionOnInvocationAnalyzer analyzerForReferenceType = findAnalyzer(newAssertThatArgumentTypeBinding)
			.orElse(null);
		return analyzerForReferenceType != null;
	}

	private BooleanAssertionOnInvocationAnalyzer(Predicate<ITypeBinding> supportedTypeBindingPredicate,
			Map<String, String> mapToAssertJAssertions,
			Map<String, String> mapToNegatedAssertJAssertions) {
		this.supportedTypeBindingPredicate = supportedTypeBindingPredicate;

		Map<String, String> tmpMap = new HashMap<>();
		tmpMap.put(EQUALS, "isEqualTo"); //$NON-NLS-1$
		tmpMap.putAll(mapToAssertJAssertions);
		this.mapToAssertJAssertions = Collections.unmodifiableMap(tmpMap);

		tmpMap = new HashMap<>();
		tmpMap.put(EQUALS, "isNotEqualTo"); //$NON-NLS-1$
		tmpMap.putAll(mapToNegatedAssertJAssertions);
		this.mapToNegatedAssertJAssertions = Collections.unmodifiableMap(tmpMap);
	}

	Optional<String> findAssertJAssertionName(IMethodBinding methodBinding) {

		if (!analyzeMethodBinding(methodBinding)) {
			return Optional.empty();
		}

		String methodName = methodBinding.getName();
		return Optional.ofNullable(mapToAssertJAssertions.get(methodName));

	}

	Optional<String> findNegatedAssertJAssertionName(IMethodBinding methodBinding) {

		if (!analyzeMethodBinding(methodBinding)) {
			return Optional.empty();
		}

		String methodName = methodBinding.getName();
		return Optional.ofNullable(mapToNegatedAssertJAssertions.get(methodName));

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

	boolean isSupportedForType(ITypeBinding typeBinding) {
		return supportedTypeBindingPredicate.test(typeBinding);
	}

}
