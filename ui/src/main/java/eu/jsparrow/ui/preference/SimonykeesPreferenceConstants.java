package eu.jsparrow.ui.preference;

/**
 * Preference constants for key values. 
 * 
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
@SuppressWarnings("nls")
public class SimonykeesPreferenceConstants {

	public static final String PROFILE_PREFIX = "profile.";
	public static final String PROFILE_USE_OPTION_PREFIX = "useProfileOption";
	
	public static final String ENABLE_INTRO = "enableIntro";
	
	public static final String PROFILE_USE_OPTION = PROFILE_PREFIX + PROFILE_USE_OPTION_PREFIX;
	public static final String PROFILE_USE_OPTION_NO_PROFILE = PROFILE_PREFIX + PROFILE_USE_OPTION_PREFIX + "noProfile";
	public static final String PROFILE_USE_OPTION_SELECTED_PROFILE = PROFILE_PREFIX + PROFILE_USE_OPTION_PREFIX + "selectedProfile";

	public static final String NAME_RULES_DELIMITER = "^";
	public static final String RULE_RULE_DELIMITER = "~";
	
	public static final String PROFILE_ID_CURRENT = PROFILE_PREFIX + "currentId";
	public static final String PROFILE_LIST = PROFILE_PREFIX + "list";
}
