package at.splendit.simonykees.core.ui.preference.profile;

import java.util.ArrayList;
import java.util.List;

public class Profile implements SimonykeesProfile {

	List<String> enabledRulesIds = new ArrayList<>();
	
	String profileName;
	
	boolean isBuiltInProfile = false;
	
	public Profile(String name, List<String> enabledRulesIds) {
		this.profileName = name;
		this.enabledRulesIds = enabledRulesIds;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	@Override
	public String getProfileName() {
		return profileName;
	}

	public void setEnabledRulesIds(List<String> enabledRulesIds) {
		this.enabledRulesIds = enabledRulesIds;
	}
	
	@Override
	public List<String> getEnabledRuleIds() {
		return enabledRulesIds;
	}
	
	public boolean containsRule(String ruleId) {
		return enabledRulesIds.contains(ruleId);
	}

	@Override
	public boolean isBuiltInProfile() {
		return isBuiltInProfile;
	}
}
