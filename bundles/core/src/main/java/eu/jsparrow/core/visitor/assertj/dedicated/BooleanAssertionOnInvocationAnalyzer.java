package eu.jsparrow.core.visitor.assertj.dedicated;

import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.*;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * This helper class analyzes method invocations which are used as argument of
 * an AssertJ assertThat invocation in connection with the boolean assertion
 * {@code isTrue} or isFalse and provides the informations for a corresponding
 * dedicated assertion.
 * 
 * For example:
 * 
 * For the AssertJ assertion
 * 
 * <pre>
 * assertThat(string.equals("Hello World!")).isTrue();
 * </pre>
 * 
 * the argument {@code string.equals("Hello World!")} is analyzed. The new
 * AssertJ assertThat argument will be {@code string} and the new dedicated
 * assertion will be {@code isEqualTo("Hello World!")}, resulting in
 * 
 * <pre>
 * assertThat(string).isEqualTo("Hello World!");
 * </pre>
 */
class BooleanAssertionOnInvocationAnalyzer {

	private static final Predicate<ITypeBinding> IS_SUPPORTED_ITERATOR_TYPE = SupportedAssertJAssertThatArgumentTypes::isSupportedIteratorTypeForAssertion;

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
		map.put(EQUALS_IGNORE_CASE, IS_EQUAL_TO_IGNORING_CASE);
		map.put(STARTS_WITH, STARTS_WITH);
		map.put(CONTAINS, CONTAINS);
		map.put(ENDS_WITH, ENDS_WITH);
		map.put(MATCHES, MATCHES);
		map.put(IS_EMPTY, IS_EMPTY);
		map.put(IS_BLANK, IS_BLANK);
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put(EQUALS_IGNORE_CASE, IS_NOT_EQUAL_TO_IGNORING_CASE);
		negatedMap.put(STARTS_WITH, DOES_NOT_START_WITH);
		negatedMap.put(CONTAINS, DOES_NOT_CONTAIN);
		negatedMap.put(ENDS_WITH, DOES_NOT_END_WITH);
		negatedMap.put(MATCHES, DOES_NOT_MATCH);
		negatedMap.put(IS_EMPTY, IS_NOT_EMPTY);
		negatedMap.put(IS_BLANK, IS_NOT_BLANK);
		return new BooleanAssertionOnInvocationAnalyzer(IS_STRING, map, negatedMap);
	}

	private static BooleanAssertionOnInvocationAnalyzer createIterableAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put(CONTAINS, CONTAINS);
		map.put(CONTAINS_ALL, CONTAINS_ALL);
		map.put(IS_EMPTY, IS_EMPTY);
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put(CONTAINS, DOES_NOT_CONTAIN);
		negatedMap.put(IS_EMPTY, IS_NOT_EMPTY);
		return new BooleanAssertionOnInvocationAnalyzer(IS_SUPPORTED_ITERABLE, map, negatedMap);
	}

	private static BooleanAssertionOnInvocationAnalyzer createMapAssertionsAnalyzer() {
		Map<String, String> map1 = new HashMap<>();
		map1.put(CONTAINS_KEY, CONTAINS_KEY);
		map1.put(CONTAINS_VALUE, CONTAINS_VALUE);
		map1.put(IS_EMPTY, IS_EMPTY);
		Map<String, String> map = map1;
		Map<String, String> map2 = new HashMap<>();
		map2.put(CONTAINS_KEY, DOES_NOT_CONTAIN_KEY);
		map2.put(CONTAINS_VALUE, DOES_NOT_CONTAIN_VALUE);
		map2.put(IS_EMPTY, IS_NOT_EMPTY);
		Map<String, String> negatedMap = map2;
		return new BooleanAssertionOnInvocationAnalyzer(IS_SUPPORTED_MAP_TYPE, map, negatedMap);
	}

	private static BooleanAssertionOnInvocationAnalyzer createPathAssertionsAnalyzer() {

		Map<String, String> map = new HashMap<>();
		map.put(IS_ABSOLUTE, IS_ABSOLUTE);
		map.put(STARTS_WITH, STARTS_WITH);
		map.put(ENDS_WITH, ENDS_WITH);
		HashMap<String, String> negatedMap = new HashMap<>();
		negatedMap.put(IS_ABSOLUTE, IS_RELATIVE);

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
		map.put(EXISTS, EXISTS); // File
		map.put(Constants.IS_FILE, Constants.IS_FILE); // File
		map.put(IS_DIRECTORY, IS_DIRECTORY); // File
		// map.put("isHidden", "isHidden"); // File -- not supported
		map.put(IS_ABSOLUTE, IS_ABSOLUTE); // Path
		map.put(CAN_READ, CAN_READ); // File
		map.put(CAN_WRITE, CAN_WRITE); // File
		// map.put("canExecute", "canExecute"); // File -- not supported
		HashMap<String, String> negatedMap = new HashMap<>();
		negatedMap.put(IS_ABSOLUTE, IS_RELATIVE);
		negatedMap.put(EXISTS, DOES_NOT_EXIST); // File
		return new BooleanAssertionOnInvocationAnalyzer(SupportedAssertJAssertThatArgumentTypes.IS_FILE, map, negatedMap);

	}

	private static BooleanAssertionOnInvocationAnalyzer createOptionalAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put(IS_EMPTY, IS_EMPTY);
		map.put(IS_PRESENT, IS_PRESENT);
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put(IS_EMPTY, IS_PRESENT);
		negatedMap.put(IS_PRESENT, IS_EMPTY);
		return new BooleanAssertionOnInvocationAnalyzer(IS_OPTIONAL, map, negatedMap);
	}

	private static BooleanAssertionOnInvocationAnalyzer createDateAndTimeAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put(AFTER, IS_AFTER);
		map.put(BEFORE, IS_BEFORE);
		map.put(IS_AFTER, IS_AFTER);
		map.put(IS_BEFORE, IS_BEFORE);
		// map.put("isSupported", "isSupported"); not supported
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put(AFTER, IS_BEFORE_OR_EQUAL_TO);
		negatedMap.put(BEFORE, IS_AFTER_OR_EQUAL_TO);
		negatedMap.put(IS_AFTER, IS_BEFORE_OR_EQUAL_TO);
		negatedMap.put(IS_BEFORE, IS_AFTER_OR_EQUAL_TO);

		return new BooleanAssertionOnInvocationAnalyzer(IS_SUPPORTED_TEMPORAL_TYPE, map, negatedMap);
	}

	private static BooleanAssertionOnInvocationAnalyzer createStreamAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put(ALL_MATCH, ALL_MATCH);
		map.put(ANY_MATCH, ANY_MATCH);
		map.put(NONE_MATCH, NONE_MATCH);
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put(ANY_MATCH, NONE_MATCH);
		negatedMap.put(NONE_MATCH, ANY_MATCH);
		return new BooleanAssertionOnInvocationAnalyzer(IS_STREAM, map, negatedMap);
	}

	private static BooleanAssertionOnInvocationAnalyzer createIteratorTypesAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put(HAS_NEXT, HAS_NEXT);
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put(HAS_NEXT, IS_EXHAUSTED);
		return new BooleanAssertionOnInvocationAnalyzer(IS_SUPPORTED_ITERATOR_TYPE, map, negatedMap);
	}

	private static BooleanAssertionOnInvocationAnalyzer createPredicateAssertionsAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put(TEST, ACCEPTS);
		Map<String, String> negatedMap = new HashMap<>();
		negatedMap.put(TEST, REJECTS);
		return new BooleanAssertionOnInvocationAnalyzer(IS_PREDICATE, map, negatedMap);
	}

	private static BooleanAssertionOnInvocationAnalyzer createArrayAssertionsAnalyzer() {
		return new BooleanAssertionOnInvocationAnalyzer(IS_SUPPORTED_ARRAY_TYPE, new HashMap<>(), new HashMap<>());
	}

	private static BooleanAssertionOnInvocationAnalyzer createOtherTypesAssertionsAnalyzer() {
		return new BooleanAssertionOnInvocationAnalyzer(IS_OTHER_SUPPORTED_TYPE, new HashMap<>(), new HashMap<>());
	}

	static Optional<NewAssertJAssertThatWithAssertionData> analyzeBooleanAssertionWithMethodInvocation(
			MethodInvocation assertThat,
			MethodInvocation invocationAsAssertThatArgument, String assertionMethodName) {

		Expression newAssertThatArgument = invocationAsAssertThatArgument.getExpression();
		if (newAssertThatArgument == null) {
			return Optional.empty();
		}
		List<Expression> newAssertionArguments = ASTNodeUtil.convertToTypedList(
				invocationAsAssertThatArgument.arguments(),
				Expression.class);

		if (newAssertionArguments.size() > 1) {
			return Optional.empty();
		}

		ITypeBinding newAssertThatArgumentTypeBinding = newAssertThatArgument.resolveTypeBinding();
		if (newAssertThatArgumentTypeBinding == null) {
			return Optional.empty();
		}

		IMethodBinding assertThatArgumentMethodBinding = invocationAsAssertThatArgument.resolveMethodBinding();
		if (assertThatArgumentMethodBinding == null) {
			return Optional.empty();
		}

		BooleanAssertionOnInvocationAnalyzer analyzer = findAnalyzer(newAssertThatArgumentTypeBinding).orElse(null);
		if (analyzer != null) {
			MethodInvocationData newAssertionData = analyzer
				.findDedicatedAssertJAssertionData(assertThatArgumentMethodBinding, newAssertionArguments,
						assertionMethodName)
				.orElse(null);
			if (newAssertionData != null) {
				MethodInvocationData assertThatData = MethodInvocationData.createNewAssertThatData(assertThat,
						newAssertThatArgument);
				NewAssertJAssertThatWithAssertionData dedicatedAssertionData = new NewAssertJAssertThatWithAssertionData(
						assertThatData,
						newAssertionData);

				return Optional.of(dedicatedAssertionData);
			}
		}
		return Optional.empty();
	}

	private static Optional<BooleanAssertionOnInvocationAnalyzer> findAnalyzer(
			ITypeBinding newAssertThatArgumentTypeBinding) {

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

	private BooleanAssertionOnInvocationAnalyzer(Predicate<ITypeBinding> supportedTypeBindingPredicate,
			Map<String, String> mapToAssertJAssertions,
			Map<String, String> mapToNegatedAssertJAssertions) {
		this.supportedTypeBindingPredicate = supportedTypeBindingPredicate;

		Map<String, String> tmpMap = new HashMap<>();
		tmpMap.put(OBJECT_EQUALS, IS_EQUAL_TO);
		tmpMap.putAll(mapToAssertJAssertions);
		this.mapToAssertJAssertions = Collections.unmodifiableMap(tmpMap);

		tmpMap = new HashMap<>();
		tmpMap.put(OBJECT_EQUALS, IS_NOT_EQUAL_TO);
		tmpMap.putAll(mapToNegatedAssertJAssertions);
		this.mapToNegatedAssertJAssertions = Collections.unmodifiableMap(tmpMap);
	}

	private Optional<MethodInvocationData> findDedicatedAssertJAssertionData(IMethodBinding methodBinding,
			List<Expression> newAssertionArguments, String booleanAssertion) {

		if (!analyzeMethodBinding(methodBinding)) {
			return Optional.empty();
		}

		String methodName = methodBinding.getName();
		String newAssertionName;
		if (booleanAssertion.equals(UseDedicatedAssertJAssertionsASTVisitor.IS_FALSE)) {
			newAssertionName = mapToNegatedAssertJAssertions.get(methodName);
		} else {
			newAssertionName = mapToAssertJAssertions.get(methodName);
		}
		if (newAssertionName != null) {
			MethodInvocationData dedicatedAssertionData = new MethodInvocationData(newAssertionName);
			dedicatedAssertionData.setArguments(newAssertionArguments);
			return Optional.of(dedicatedAssertionData);
		}
		return Optional.empty();

	}

	protected boolean analyzeMethodBinding(IMethodBinding methodBinding) {
		return analyzeEqualsMethodParameters(methodBinding);
	}

	private static boolean analyzeEqualsMethodParameters(IMethodBinding methodBinding) {
		String methodName = methodBinding.getName();
		if (!methodName.equals(OBJECT_EQUALS)) {
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
