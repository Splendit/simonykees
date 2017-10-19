package eu.jsparrow.ui.preference;

/**
 * Preference constants for key values. 
 * 
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
@SuppressWarnings("nls")
public class SimonykeesPreferenceConstants {

	private SimonykeesPreferenceConstants() {
		// private constructor to hide the implicit default constructor (SonarLint)
	}
	
	public static final String PROFILE_PREFIX = "profile.";
	
	public static final String ENABLE_INTRO = "enableIntro";
	public static final String RESOLVE_PACKAGES_RECURSIVELY = "resolvePackagesRecursively";
	
	public static final String NAME_RULES_DELIMITER = "^";
	public static final String RULE_RULE_DELIMITER = "~";
	
	public static final String PROFILE_ID_CURRENT = PROFILE_PREFIX + "currentId";
	public static final String PROFILE_LIST = PROFILE_PREFIX + "list";
}
