package eu.jsparrow.core.visitor.assertj.dedicated;

import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.ACCEPTS;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.AFTER;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.ALL_MATCH;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.ANY_MATCH;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.BEFORE;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.CAN_READ;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.CAN_WRITE;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.CONTAINS;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.CONTAINS_ALL;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.CONTAINS_KEY;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.CONTAINS_VALUE;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.DOES_NOT_CONTAIN;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.DOES_NOT_CONTAIN_KEY;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.DOES_NOT_CONTAIN_VALUE;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.DOES_NOT_END_WITH;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.DOES_NOT_EXIST;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.DOES_NOT_MATCH;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.DOES_NOT_START_WITH;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.ENDS_WITH;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.EQUALS_IGNORE_CASE;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.EXISTS;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.HAS_NEXT;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_ABSOLUTE;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_AFTER;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_AFTER_OR_EQUAL_TO;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_BEFORE;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_BEFORE_OR_EQUAL_TO;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_BLANK;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_DIRECTORY;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_EMPTY;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_EQUAL_TO_IGNORING_CASE;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_EXHAUSTED;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_NOT_BLANK;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_NOT_EMPTY;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_NOT_EQUAL_TO_IGNORING_CASE;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_PRESENT;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_RELATIVE;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.MATCHES;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.NONE_MATCH;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.REJECTS;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.STARTS_WITH;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.TEST;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_OPTIONAL;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_OTHER_SUPPORTED_TYPE;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_PREDICATE;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_STREAM;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_STRING;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_SUPPORTED_ARRAY_TYPE;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_SUPPORTED_ITERABLE;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_SUPPORTED_ITERATOR_TYPE;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_SUPPORTED_MAP_TYPE;
import static eu.jsparrow.core.visitor.assertj.dedicated.SupportedAssertJAssertThatArgumentTypes.IS_SUPPORTED_TEMPORAL_TYPE;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Factory class for instances of {@link BooleanAssertionOnInvocationAnalyzer}
 * which are supported for different groups of methods.
 * 
 * @since 4.7.0
 *
 */
public class BooleanAssertionOnInvocationAnalyzerFactory {

	private static final BooleanAssertionOnInvocationAnalyzer STRING_ASSERTIONS_ANALYZER = createStringAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer ITERABLE_ASSERTIONS_ANALYZER = createIterableAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer MAP_ASSERTIONS_ANALYZER = createMapAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer PATH_ASSERTIONS_ANALYZER = createPathAssertionAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer FILE_ASSERTIONS_ANALYZER = createFileAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer OPTIONAL_ASSERTIONS_ANALYZER = createOptionalAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer DATE_AND_TIME_ASSERTIONS_ANALYZER = createDateAndTimeAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer STREAM_ASSERTIONS_ANALYZER = createStreamAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer ITERATOR_ASSERTIONS_ANALYZER = createIteratorTypesAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer PREDICATE_ASSERTIONS_ANALYZER = createPredicateAssertionsAnalyzer();
	private static final BooleanAssertionOnInvocationAnalyzer ARRAY_ASSERTIONS_ANALYZER = createArrayAssertionsAnalyzer();

	private static final BooleanAssertionOnInvocationAnalyzer OTHER_TYPES_ASSERTIONS_ANALYZER = createOtherTypesAssertionsAnalyzer();

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

	private static BooleanAssertionOnInvocationAnalyzer createPathAssertionAnalyzer() {
		Map<String, String> map = new HashMap<>();
		map.put(IS_ABSOLUTE, IS_ABSOLUTE);
		map.put(STARTS_WITH, STARTS_WITH);
		map.put(ENDS_WITH, ENDS_WITH);
		HashMap<String, String> negatedMap = new HashMap<>();
		negatedMap.put(IS_ABSOLUTE, IS_RELATIVE);
		return new BooleanAssertionOnInvocationAnalyzer(SupportedAssertJAssertThatArgumentTypes.IS_PATH, map,
				negatedMap);
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
		return new BooleanAssertionOnInvocationAnalyzer(SupportedAssertJAssertThatArgumentTypes.IS_FILE, map,
				negatedMap);

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

	static Optional<BooleanAssertionOnInvocationAnalyzer> findAnalyzer(
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

	private BooleanAssertionOnInvocationAnalyzerFactory() {
		/*
		 * Hide default constructor.
		 */
	}
}
