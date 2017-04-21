package at.splendit.simonykees.core.ui.preference.profile;

import java.util.List;
import java.util.stream.Collectors;

import at.splendit.simonykees.core.rule.GroupEnum;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.ui.preference.SimonykeesPreferenceConstants;
import at.splendit.simonykees.i18n.Messages;

/**
 * A profile of all rules requiring Java 7.
 * 
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
public class Java7Profile implements SimonykeesProfile {

	public static final String PROFILE_ID = SimonykeesPreferenceConstants.PROFILE_PREFIX + "java7"; //$NON-NLS-1$

	public Java7Profile() {
	}

	@Override
	public String getProfileId() {
		return PROFILE_ID;
	}

	@Override
	public String getProfileName() {
		return Messages.Java7Profile_profileName;
	}

	@Override
	public boolean isBuiltInProfile() {
		return true;
	}

	@Override
	public List<String> getEnabledRuleIds() {
		return RulesContainer.getAllRules().stream().filter(r -> r.getGroups().contains(GroupEnum.JAVA_7))
				.map(RefactoringRule::getId).collect(Collectors.toList());
	}

}
