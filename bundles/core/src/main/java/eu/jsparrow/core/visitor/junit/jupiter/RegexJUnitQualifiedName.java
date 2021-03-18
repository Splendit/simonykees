package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * A wrapper for a REGEX for defining whether a type is declared in
 * {@code org.junit} package but not in {@code org.junit.jupiter}.
 * 
 * @since 3.27.0
 *
 */
public class RegexJUnitQualifiedName {
	private static final String PATTERN_ORG_JUNIT_4_QUALIFIED_NAME = "^(junit|(org\\.junit))(\\..+)?$"; //$NON-NLS-1$
	private static final String PATTERN_ORG_JUNIT_JUPITER_QUALIFIED_NAME = "^org\\.junit\\.jupiter\\.api(\\..+)?$"; //$NON-NLS-1$
	private static final Predicate<String> PREDICATE_J_UNIT_NAME = Pattern
		.compile(PATTERN_ORG_JUNIT_4_QUALIFIED_NAME)
		.asPredicate();
	private static final Predicate<String> PREDICATE_J_UNIT_JUPITER_NAME = Pattern
		.compile(PATTERN_ORG_JUNIT_JUPITER_QUALIFIED_NAME)
		.asPredicate();

	public static boolean isJUnitName(String qualifiedName) {
		return PREDICATE_J_UNIT_NAME.test(qualifiedName);
	}

	public static boolean isJUnitJupiterName(String qualifiedName) {
		return PREDICATE_J_UNIT_JUPITER_NAME.test(qualifiedName);
	}

	private RegexJUnitQualifiedName() {
		/*
		 * Hide the default constructor.
		 */
	}
}
