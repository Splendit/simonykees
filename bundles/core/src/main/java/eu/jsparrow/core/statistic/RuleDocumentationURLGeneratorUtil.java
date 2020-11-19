package eu.jsparrow.core.statistic;

/**
 * A utility class for generating the URLs of rule's documentation.
 * 
 * @since 3.23.0
 *
 */
public class RuleDocumentationURLGeneratorUtil {

	private RuleDocumentationURLGeneratorUtil() {
		/*
		 * Hide the default constructor.
		 */
	}

	private static final String DOCUMENTATION_SPACE_BASE_URL = "https://jsparrow.github.io/rules/"; //$NON-NLS-1$

	/**
	 * Generates URL for the {@link ruleId}. The camelcase word is split,
	 * transposed to lowercase and concatinated with a dash (-). Uses
	 * {@value #DOCUMENTATION_SPACE_BASE_URL} as base url.
	 * 
	 * 
	 * @param ruleId
	 *            name of the rule in Id format.
	 * @return
	 */
	public static String generateLinkToDocumentation(String ruleId) {
		return DOCUMENTATION_SPACE_BASE_URL
				+ String.join("-", ruleId.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) //$NON-NLS-1$ //$NON-NLS-2$
					.toLowerCase()
				+ ".html"; //$NON-NLS-1$
	}

}
