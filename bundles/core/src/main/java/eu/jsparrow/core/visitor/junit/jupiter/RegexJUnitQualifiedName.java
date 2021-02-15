package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.function.Predicate;
import java.util.regex.Pattern;

class RegexJUnitQualifiedName {
	private static final String PATTERN_ORG_JUNIT_4_QUALIFIED_NAME = "^(junit|(org\\.junit))(\\..+)?$"; //$NON-NLS-1$
	private static final String PATTERN_ORG_JUNIT_JUPITER_QUALIFIED_NAME = "^org\\.junit\\.jupiter\\.api(\\..+)?$"; //$NON-NLS-1$
	private static final Predicate<String> PREDICATE_J_UNIT_NAME = Pattern
		.compile(PATTERN_ORG_JUNIT_4_QUALIFIED_NAME)
		.asPredicate();
	private static final Predicate<String> PREDICATE_J_UNIT_JUPITER_NAME = Pattern
		.compile(PATTERN_ORG_JUNIT_JUPITER_QUALIFIED_NAME)
		.asPredicate();

	static boolean isJUnitName(String qualifiedName) {
		return PREDICATE_J_UNIT_NAME.test(qualifiedName);
	}

	static boolean isJUnitJupiterName(String qualifiedName) {
		return PREDICATE_J_UNIT_JUPITER_NAME.test(qualifiedName);
	}

	private RegexJUnitQualifiedName() {
	}
}
