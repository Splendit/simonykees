package eu.jsparrow.core.visitor.assertj;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Helper class to find out whether a {@link MethodInvocation} represents an
 * AssertJ-assertion carrying out an assertion without changing data.
 * 
 * @see #isSupportedAssertJAssertionMethodName(String)
 * @see #isSupportedAssertJAssertion(MethodInvocation)
 * 
 * @since 4.6.0
 */
public class SupportedAssertJAssertions {

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
	 * @return true if the given method name represents a valid
	 *         AssertJ-assertion like for example {@code isNotNull} or
	 *         {@code isNotEmpty}, otherwise false.
	 */
	public static boolean isSupportedAssertJAssertionMethodName(String methodName) {
		return SUPPORTED_ASSERTION_PREFIXES.stream()
			.anyMatch(methodName::startsWith);
	}

	private SupportedAssertJAssertions() {
		// hiding implicit public constructor of utility class
	}
}
