package eu.jsparrow.core.visitor.assertj.dedicated;

/**
 * Relevant method names for {@link BooleanAssertionOnInvocationAnalyzer}.
 * 
 * @since 4.7.0
 *
 */
@SuppressWarnings("nls")
public class Constants {

	/*
	 * boolean assertions
	 */
	public static final String IS_FALSE = "isFalse"; //$NON-NLS-1$
	public static final String IS_TRUE = "isTrue"; //$NON-NLS-1$

	/*
	 * assertion methods for all supported types
	 */
	public static final String IS_NOT_NULL = "isNotNull";
	public static final String IS_NOT_SAME_AS = "isNotSameAs";
	public static final String IS_NULL = "isNull";
	public static final String IS_SAME_AS = "isSameAs";

	/*
	 * String methods
	 */
	public static final String CONTAINS = "contains";
	public static final String DOES_NOT_CONTAIN = "doesNotContain";
	public static final String DOES_NOT_END_WITH = "doesNotEndWith";
	public static final String DOES_NOT_MATCH = "doesNotMatch";
	public static final String DOES_NOT_START_WITH = "doesNotStartWith";
	public static final String ENDS_WITH = "endsWith";
	public static final String EQUALS_IGNORE_CASE = "equalsIgnoreCase";
	public static final String IS_BLANK = "isBlank";
	public static final String IS_EMPTY = "isEmpty";
	public static final String IS_EQUAL_TO_IGNORING_CASE = "isEqualToIgnoringCase";
	public static final String IS_NOT_BLANK = "isNotBlank";
	public static final String IS_NOT_EMPTY = "isNotEmpty";
	public static final String IS_NOT_EQUAL_TO_IGNORING_CASE = "isNotEqualToIgnoringCase";
	public static final String MATCHES = "matches";
	public static final String STARTS_WITH = "startsWith";

	/*
	 * Iterable
	 */
	public static final String CONTAINS_ALL = "containsAll";

	/*
	 * Map
	 */
	public static final String CONTAINS_VALUE = "containsValue";
	public static final String CONTAINS_KEY = "containsKey";
	public static final String DOES_NOT_CONTAIN_KEY = "doesNotContainKey";
	public static final String DOES_NOT_CONTAIN_VALUE = "doesNotContainValue";

	/*
	 * Path
	 */
	public static final String IS_ABSOLUTE = "isAbsolute";
	public static final String IS_RELATIVE = "isRelative";

	/*
	 * File
	 */
	public static final String CAN_READ = "canRead";
	public static final String CAN_WRITE = "canWrite";
	public static final String DOES_NOT_EXIST = "doesNotExist";
	public static final String EXISTS = "exists";
	public static final String IS_DIRECTORY = "isDirectory";
	public static final String IS_FILE = "isFile";

	/*
	 * Date
	 */
	public static final String AFTER = "after";
	public static final String BEFORE = "before";
	public static final String IS_AFTER = "isAfter";
	public static final String IS_AFTER_OR_EQUAL_TO = "isAfterOrEqualTo";
	public static final String IS_BEFORE = "isBefore";
	public static final String IS_BEFORE_OR_EQUAL_TO = "isBeforeOrEqualTo";

	/*
	 * Stream
	 */
	public static final String ALL_MATCH = "allMatch";
	public static final String ANY_MATCH = "anyMatch";
	public static final String NONE_MATCH = "noneMatch";

	/*
	 * Iterator
	 */
	public static final String IS_EXHAUSTED = "isExhausted";
	public static final String HAS_NEXT = "hasNext";

	/*
	 * Optional
	 */
	public static final String IS_PRESENT = "isPresent";

	/*
	 * Predicate
	 */
	public static final String ACCEPTS = "accepts";
	public static final String REJECTS = "rejects";
	public static final String TEST = "test";

	public static final String OBJECT_EQUALS = "equals";
	public static final String JAVA_UTIL = "java.util";
	public static final String IS_EQUAL_TO = "isEqualTo";
	public static final String IS_NOT_EQUAL_TO = "isNotEqualTo";

	/*
	 * Numeric
	 */
	public static final String IS_NOT_ZERO = "isNotZero";
	public static final String IS_ZERO = "isZero";

	private Constants() {
		/*
		 * Hide default constructor.
		 */
	}
}
