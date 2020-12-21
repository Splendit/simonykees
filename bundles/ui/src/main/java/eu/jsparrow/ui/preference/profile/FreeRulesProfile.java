package eu.jsparrow.ui.preference.profile;

import java.util.List;
import java.util.stream.Collectors;

import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;

/**
 * Profile containing free rules.
 * 
 * @since 3.0.0
 * 
 */
public class FreeRulesProfile implements SimonykeesProfile {

	private List<String> enabledRulesIds;

	boolean isBuiltInProfile = true;

	public FreeRulesProfile() {
		enabledRulesIds = RulesContainer.getAllRules(false)
			.stream()
			.filter(RefactoringRule::isFree)
			.map(RefactoringRule::getId)
			.collect(Collectors.toList());
	}

	@Override
	public String getProfileName() {
		return Messages.Profile_FreeRulesProfile_profileName;
	}

	@Override
	public boolean isBuiltInProfile() {
		return isBuiltInProfile;
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
	public boolean containsRule(String id) {
		return getEnabledRuleIds().contains(id);
	}

}
