package eu.jsparrow.core.visitor.assertj;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Helper class to find out whether
 * <ul>
 * <li>a methodName represents a supported AssertJ AssertThat invocation</li>
 * <li>a methodName represents a supported AssertJ assertion</li>
 * <li>a {@link ITypeBinding} represents a class defining supported AssertJ
 * AssertThat methods</li>
 * </ul>
 * 
 * @see #isSupportedAssertJAssertionMethodName(String)
 * @see #isSupportedAssertJAssertionMethodName(String)
 * 
 * @since 4.6.0
 */
public class SupportedAssertJAssertions {

	private static final String ORG_ASSERTJ_CORE_API_PREFIX = "org.assertj.core.api."; //$NON-NLS-1$

	private static final List<String> SUPPORTED_ASSERTIONS_CLASS_NAMES = Collections.unmodifiableList(Arrays.asList(
			ORG_ASSERTJ_CORE_API_PREFIX + "Assertions", //$NON-NLS-1$
			ORG_ASSERTJ_CORE_API_PREFIX + "AssertionsForClassTypes", //$NON-NLS-1$
			ORG_ASSERTJ_CORE_API_PREFIX + "AssertionsForInterfaceTypes" //$NON-NLS-1$
	));

	private static final List<String> SUPPORTED_ASSERT_THAT_METHODS = Collections.unmodifiableList(Arrays.asList(
			"assertThat", //$NON-NLS-1$
			"assertThatCode", //$NON-NLS-1$
			"assertThatThrownBy", //$NON-NLS-1$
			"assertThatObject"//$NON-NLS-1$
	));

	private static final List<String> SUPPORTED_ASSERTION_PREFIXES = Collections.unmodifiableList(Arrays.asList(
			"is", //$NON-NLS-1$
			"has", //$NON-NLS-1$
			"contains", //$NON-NLS-1$
			"can", //$NON-NLS-1$
			"doesNot", //$NON-NLS-1$
			"startsWith", //$NON-NLS-1$
			"endsWith", //$NON-NLS-1$
			"matches", //$NON-NLS-1$
			"allSatisfy", //$NON-NLS-1$
			"anySatisfy", //$NON-NLS-1$
			"noneSatisfy", //$NON-NLS-1$
			"allMatch", //$NON-NLS-1$
			"anyMatch", //$NON-NLS-1$
			"noneMatch", //$NON-NLS-1$
			"exists", //$NON-NLS-1$
			"satisfies", //$NON-NLS-1$
			"are", //$NON-NLS-1$
			"have", //$NON-NLS-1$
			"rejects", //$NON-NLS-1$
			"accepts"//$NON-NLS-1$
	));

	/**
	 * 
	 * @return true if the given method name represents is a supported
	 *         'assertThat' method like for example {@code assertThat} or
	 *         {@code assertThatThrownBy}, otherwise false.
	 */
	public static boolean isSupportedAssertJAsserThatMethodName(String methodName) {
		return SUPPORTED_ASSERT_THAT_METHODS.contains(methodName);
	}

	/**
	 * @return true if the given method name represents a supported AssertJ
	 *         assertion like for example {@code isNotNull} or
	 *         {@code isNotEmpty}, otherwise false.
	 */
	public static boolean isSupportedAssertJAssertionMethodName(String methodName) {
		return SUPPORTED_ASSERTION_PREFIXES.stream()
			.anyMatch(methodName::startsWith);
	}

	/**
	 * @return true if the type binding represents a class which defines
	 *         supported 'assertThat' methods.
	 */
	public static boolean isSupportedAssertionsType(ITypeBinding classDeclaringAssertThat) {
		return ClassRelationUtil.isContentOfTypes(classDeclaringAssertThat, SUPPORTED_ASSERTIONS_CLASS_NAMES);
	}

	/**
	 * @return true if the given class name represents a valid AssertJ-assertion
	 *         like for example {@code isNotNull} or {@code isNotEmpty},
	 *         otherwise false.
	 * 
	 */
	private SupportedAssertJAssertions() {
		// hiding implicit public constructor of utility class
	}
}
