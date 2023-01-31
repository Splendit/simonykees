package eu.jsparrow.ui.preference.profile;

import java.util.ArrayList;
import java.util.List;

import eu.jsparrow.i18n.Messages;

/**
 * Profile object
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
public class CustomProfile implements SimonykeesProfile {

	List<String> enabledRulesIds = new ArrayList<>();

	public CustomProfile(List<String> enabledRulesIds) {
		this.enabledRulesIds = enabledRulesIds;
	}

	@Override
	public String getProfileName() {
		return Messages.SelectRulesWizardPage_CustomProfileLabel;
	}

	@Override
	public void setEnabledRulesIds(List<String> enabledRulesIds) {
		this.enabledRulesIds = enabledRulesIds;
	}

	@Override
	public List<String> getEnabledRuleIds() {
		return enabledRulesIds;
	}

	@Override
	public boolean containsRule(String ruleId) {
		return enabledRulesIds.contains(ruleId);
	}

	@Override
	public boolean isBuiltInProfile() {
		return false;
	}
}
