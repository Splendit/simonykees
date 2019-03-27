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

	private YAMLRenamingRule renamingRule;

	private YAMLLoggerRule loggerRule;

	public YAMLProfile() {
		this.name = ""; //$NON-NLS-1$
		this.rules = new LinkedList<>();
		this.renamingRule = new YAMLRenamingRule();
		this.loggerRule = new YAMLLoggerRule();
	}

	public YAMLProfile(String name, List<String> rules, YAMLRenamingRule renamingRule, YAMLLoggerRule loggerRule) {
		this.name = name;
		this.rules = rules;
		this.renamingRule = renamingRule;
		this.loggerRule = loggerRule;
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

	public YAMLRenamingRule getRenamingRule() {
		return renamingRule;
	}

	public void setRenamingRule(YAMLRenamingRule renamingRule) {
		this.renamingRule = renamingRule;
	}

	public YAMLLoggerRule getLoggerRule() {
		return loggerRule;
	}

	public void setLoggerRule(YAMLLoggerRule loggerRule) {
		this.loggerRule = loggerRule;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "YAMLProfile [name=" + name + ", rules=" + rules + ", " + renamingRule.toString() + ", " + loggerRule.toString() + "]";
	}

}
