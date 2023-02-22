package eu.jsparrow.ui.preference.profile;

import java.util.ArrayList;
import java.util.List;

import eu.jsparrow.i18n.Messages;

/**
 * Represents the custom profile in the wizard to select rules for re-factoring.
 * It indicates a set of selected rules which have not yet been saved as
 * profile.
 * 
 * @since 4.15.0
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
