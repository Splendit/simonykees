package eu.jsparrow.ui.preference.profile;

import java.util.LinkedList;
import java.util.List;

import eu.jsparrow.i18n.Messages;

public class EmptyProfile implements SimonykeesProfile {
	
	@Override
	public String getProfileName() {
		return Messages.EmptyProfile_profileName;
	}

	@Override
	public List<String> getEnabledRuleIds() {
		return new LinkedList<>();
	}

	@Override
	public void setEnabledRulesIds(List<String> enabledRulesIds) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsRule(String id) {
		return false;
	}

	@Override
	public boolean isBuiltInProfile() {
		return true;
	}

}
