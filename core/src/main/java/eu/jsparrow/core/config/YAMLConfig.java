package eu.jsparrow.core.config;

import java.util.LinkedList;
import java.util.List;

/**
 * Model class for configuration data.
 * 
 * @author Matthias Webhofer
 * @since 2.2.2
 */
public class YAMLConfig {

	/**
	 * this list holds all IDs of rules which should be executed if no default
	 * profile has been specified
	 */
	private List<String> rules;

	/**
	 * this list holds all specified profiles (see {@link YAMLProfile}
	 */
	private List<YAMLProfile> profiles;

	/**
	 * default profile specifies a profile from the {@link YAMLConfig#profiles} list
	 * which gets executed by default
	 */
	private String defaultProfile;

	public YAMLConfig() {
		this.rules = new LinkedList<>();
		this.profiles = new LinkedList<>();
		this.defaultProfile = ""; //$NON-NLS-1$
	}

	public YAMLConfig(List<String> rules, List<YAMLProfile> profiles, String defaultProfile) {
		this.rules = rules;
		this.profiles = profiles;
		this.defaultProfile = defaultProfile;
	}

	/**
	 * provides a default configuration for jsparrow
	 * 
	 * @return default configuration
	 */
	public static YAMLConfig getDefaultConfig() {
		YAMLConfig config = new YAMLConfig();

		List<String> profileRules = new LinkedList<>();
		profileRules.add("CodeFormatterRule"); //$NON-NLS-1$
		profileRules.add("DiamondOperatorRule"); //$NON-NLS-1$
		profileRules.add("ForToForEachRule"); //$NON-NLS-1$
		profileRules.add("EnhancedForLoopToStreamForEachRule"); //$NON-NLS-1$
		profileRules.add("WhileToForEachRule"); //$NON-NLS-1$
		profileRules.add("MultiCatchRule"); //$NON-NLS-1$
		profileRules.add("LambdaForEachIfWrapperToFilterRule"); //$NON-NLS-1$
		profileRules.add("TryWithResourceRule"); //$NON-NLS-1$

		YAMLProfile profile = new YAMLProfile();
		profile.setName("default"); //$NON-NLS-1$
		profile.setRules(profileRules);

		config.getProfiles().add(profile);

		config.setDefaultProfile("default"); //$NON-NLS-1$

		return config;
	}

	public List<String> getRules() {
		return rules;
	}

	public void setRules(List<String> rules) {
		this.rules = rules;
	}

	public List<YAMLProfile> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<YAMLProfile> profiles) {
		this.profiles = profiles;
	}

	public String getDefaultProfile() {
		return defaultProfile;
	}

	public void setDefaultProfile(String defaultProfile) {
		this.defaultProfile = defaultProfile;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "YAMLConfig [rules=" + rules + ", profiles=" + profiles + ", defaultProfile=" + defaultProfile + "]";
	}

}
