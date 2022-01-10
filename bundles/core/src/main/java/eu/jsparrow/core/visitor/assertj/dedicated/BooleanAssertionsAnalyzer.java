package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

@SuppressWarnings("nls")
class BooleanAssertionsAnalyzer {

	private static final String ENDS_WITH = "endsWith";
	private static final String STARTS_WITH = "startsWith";
	private static final String EQUALS = "equals";
	private static final String JAVA_UTIL = "java.util";

	private static final BooleanAssertionsAnalyzer STRING_ASSERTIONS_ANALYZER = createStringAssertionsAnalyzer();
	private static final BooleanAssertionsAnalyzer ITERABLE_ASSERTIONS_ANALYZER = createIterableAssertionsAnalyzer();
	private static final BooleanAssertionsAnalyzer MAP_ASSERTIONS_ANALYZER = createMapAssertionsAnalyzer();
	private static final BooleanAssertionsAnalyzer PATH_ASSERTIONS_ANALYZER = createPathAssertionsAnalyzer();
	private static final BooleanAssertionsAnalyzer FILE_ASSERTIONS_ANALYZER = createFileAssertionsAnalyzer();
	private static final BooleanAssertionsAnalyzer OPTIONAL_ASSERTIONS_ANALYZER = createOptionalAssertionsAnalyzer();
	private static final BooleanAssertionsAnalyzer DATE_AND_TIME_ASSERTIONS_ANALYZER = createDateAndTimeAssertionsAnalyzer();
	private static final BooleanAssertionsAnalyzer STREAM_ASSERTIONS_ANALYZER = createStreamAssertionsAnalyzer();
	private static final BooleanAssertionsAnalyzer ITERATOR_ASSERTIONS_ANALYZER = createIteratorTypesAssertionsAnalyzer();
	private static final BooleanAssertionsAnalyzer PREDICATE_ASSERTIONS_ANALYZER = createPredicateAssertionsAnalyzer();

	private static final BooleanAssertionsAnalyzer OTHER_TYPES_ASSERTIONS_ANALYZER = createOtherTypesAssertionsAnalyzer();

	private final Map<String, String> mapToAssertJAssertions;
	private final Map<String, String> mapToNegatedAssertJAssertions;
	private final Predicate<ITypeBinding> supportedTypeBindingPredicate;

	private static BooleanAssertionsAnalyzer createStringAssertionsAnalyzer() {
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
		return new BooleanAssertionsAnalyzer(getTypeBindingPredicate(String.class), map, negatedMap);
	}

	private static BooleanAssertionsAnalyzer createIterableAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put("contains", "contains");
		map.put("containsAll", "containsAll");
		map.put("isEmpty", "isEmpty");
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put("contains", "doesNotContain");
		negatedMap.put("isEmpty", "isNotEmpty");
		return new BooleanAssertionsAnalyzer(BooleanAssertionsAnalyzer::isSupportedIterableType, map, negatedMap);
	}

	private static BooleanAssertionsAnalyzer createMapAssertionsAnalyzer() {
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
		return new BooleanAssertionsAnalyzer(BooleanAssertionsAnalyzer::isSupportedMapType, map, negatedMap);
	}

	private static BooleanAssertionsAnalyzer createPathAssertionsAnalyzer() {

		Map<String, String> map = new HashMap<>();
		map.put("isAbsolute", "isAbsolute");
		map.put(STARTS_WITH, STARTS_WITH);
		map.put(ENDS_WITH, ENDS_WITH);
		HashMap<String, String> negatedMap = new HashMap<>();
		negatedMap.put("isAbsolute", "isRelative");

		return new BooleanAssertionsAnalyzer(getTypeBindingPredicate(java.nio.file.Path.class), map, negatedMap) {
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

	private static BooleanAssertionsAnalyzer createFileAssertionsAnalyzer() {

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
		return new BooleanAssertionsAnalyzer(getTypeBindingPredicate(java.io.File.class), map, negatedMap);

	}

	private static BooleanAssertionsAnalyzer createOptionalAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put("isEmpty", "isEmpty");
		map.put("isPresent", "isPresent");
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put("isEmpty", "isPresent");
		negatedMap.put("isPresent", "isEmpty");
		return new BooleanAssertionsAnalyzer(
				getTypeBindingPredicate(
						java.util.Optional.class,
						java.util.OptionalDouble.class,
						java.util.OptionalInt.class,
						java.util.OptionalLong.class),
				map, negatedMap);
	}

	private static BooleanAssertionsAnalyzer createDateAndTimeAssertionsAnalyzer() {
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

		return new BooleanAssertionsAnalyzer(
				getTypeBindingPredicate(
						java.util.Date.class,
						java.time.Instant.class,
						java.time.LocalDate.class,
						java.time.LocalDateTime.class,
						java.time.LocalTime.class,
						java.time.OffsetDateTime.class,
						java.time.OffsetTime.class,
						java.time.ZonedDateTime.class),
				map, negatedMap);
	}

	private static BooleanAssertionsAnalyzer createStreamAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put("allMatch", "allMatch");
		map.put("anyMatch", "anyMatch");
		map.put("noneMatch", "noneMatch");
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put("anyMatch", "noneMatch");
		negatedMap.put("noneMatch", "anyMatch");

		return new BooleanAssertionsAnalyzer(
				getTypeBindingPredicate(
						java.util.stream.Stream.class,
						java.util.stream.IntStream.class,
						java.util.stream.LongStream.class,
						java.util.stream.DoubleStream.class),
				map, negatedMap);
	}

	private static BooleanAssertionsAnalyzer createIteratorTypesAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put("hasNext", "hasNext");
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put("hasNext", "isExhausted");
		return new BooleanAssertionsAnalyzer(BooleanAssertionsAnalyzer::isSupportedIteratorTypeForAssertion, map,
				negatedMap);
	}

	private static BooleanAssertionsAnalyzer createPredicateAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put("test", "accepts");
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put("test", "rejects");
		return new BooleanAssertionsAnalyzer(
				getTypeBindingPredicate(
						java.util.function.DoublePredicate.class,
						java.util.function.IntPredicate.class,
						java.util.function.LongPredicate.class,
						java.util.function.Predicate.class),
				map, negatedMap);
	}

	private static BooleanAssertionsAnalyzer createOtherTypesAssertionsAnalyzer() {
		return new BooleanAssertionsAnalyzer(getTypeBindingPredicate(
				java.lang.Object.class,
				java.lang.Boolean.class,
				java.lang.Character.class,
				java.lang.Byte.class,
				java.lang.Short.class,
				java.lang.Integer.class,
				java.lang.Long.class,
				java.lang.Float.class,
				java.lang.Double.class,
				//
				java.lang.StringBuffer.class,
				java.lang.StringBuilder.class,
				java.lang.CharSequence.class,
				//
				java.lang.Class.class,
				java.lang.Exception.class,
				java.lang.Throwable.class,
				//
				java.io.InputStream.class,
				//
				java.math.BigInteger.class,
				java.math.BigDecimal.class,
				//
				java.time.Period.class),
				new HashMap<>(), new HashMap<>());
	}

	static Predicate<ITypeBinding> getTypeBindingPredicate(Class<?>... classes) {
		if (classes.length == 1) {
			return typeBinding -> ClassRelationUtil.isContentOfType(typeBinding, classes[0].getName());
		}
		List<String> classNamesList = Stream.of(classes)
			.map(Class::getName)
			.collect(Collectors.toList());
		return typeBinding -> ClassRelationUtil.isContentOfTypes(typeBinding, classNamesList);
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

	public static boolean isSupportedIteratorTypeForAssertion(ITypeBinding typeBinding) {

		if (ClassRelationUtil.isContentOfType(typeBinding, java.util.Iterator.class.getName())) {
			return true;
		}
		String packageName = typeBinding.getPackage()
			.getName();

		return packageName.equals(JAVA_UTIL)
				&& ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
						Arrays.asList(java.util.Iterator.class.getName()));

	}

	static Optional<BooleanAssertionsAnalyzer> findAnalyzer(ITypeBinding newAssertThatArgumentTypeBinding) {

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
		if (OTHER_TYPES_ASSERTIONS_ANALYZER.isSupportedForType(newAssertThatArgumentTypeBinding)) {
			return Optional.of(OTHER_TYPES_ASSERTIONS_ANALYZER);
		}
		return Optional.empty();
	}

	static Optional<String> findNewAssertionNameForIsTrue(ITypeBinding newAssertThatArgumentTypeBinding,
			IMethodBinding assertThatArgumentMethodBinding) {

		BooleanAssertionsAnalyzer analyzer = findAnalyzer(newAssertThatArgumentTypeBinding).orElse(null);
		if (analyzer != null) {
			return analyzer.findAssertJAssertionName(assertThatArgumentMethodBinding);
		}
		return Optional.empty();
	}

	static Optional<String> findNewAssertionNameForIsFalse(ITypeBinding newAssertThatArgumentTypeBinding,
			IMethodBinding assertThatArgumentMethodBinding) {

		BooleanAssertionsAnalyzer analyzer = findAnalyzer(newAssertThatArgumentTypeBinding).orElse(null);
		if (analyzer != null) {
			return analyzer.findNegatedAssertJAssertionName(assertThatArgumentMethodBinding);
		}
		return Optional.empty();

	}

	private BooleanAssertionsAnalyzer(Predicate<ITypeBinding> supportedTypeBindingPredicate,
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
