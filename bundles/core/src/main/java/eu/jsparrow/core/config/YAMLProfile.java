package eu.jsparrow.core.config;

import java.util.LinkedList;
import java.util.List;

/**
 * model class for profile data
 * 
 * @author Matthias Webhofer
 * @since 2.2.2
 */
public class YAMLProfile {
	/**
	 * profile name
	 */
	private String name;

	/**
	 * this list holds all rules IDs for this profile
	 */
	private List<String> rules;

	public YAMLProfile() {
		this.name = ""; //$NON-NLS-1$
		this.rules = new LinkedList<>();
	}

	public YAMLProfile(String name, List<String> rules) {
		this.name = name;
		this.rules = rules;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getRules() {
		return rules;
	}

	public void setRules(List<String> rules) {
		this.rules = rules;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "YAMLProfile [name=" + name + ", rules=" + rules + "]";
	}

}
