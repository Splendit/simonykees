package at.splendit.simonykees.core.ui.preference.profile;

import java.util.List;
import java.util.stream.Collectors;

import at.splendit.simonykees.core.rule.GroupEnum;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.ui.preference.SimonykeesPreferenceConstants;
import at.splendit.simonykees.i18n.Messages;

/**
 * A profile of all rules requiring Java 8.
 * 
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
public class Java8Profile implements SimonykeesProfile {

	public static final String PROFILE_ID = SimonykeesPreferenceConstants.PROFILE_PREFIX + "java8"; //$NON-NLS-1$

	public Java8Profile() {
	}

	@Override
	public String getProfileId() {
		return PROFILE_ID;
	}

	@Override
	public String getProfileName() {
		return Messages.Profile_Java8Profile_profileName;
	}

	@Override
	public boolean isBuiltInProfile() {
		return true;
	}

	@Override
	public List<String> getEnabledRuleIds() {
		return RulesContainer.getAllRules().stream().filter(r -> r.getGroups().contains(GroupEnum.JAVA_8))
				.map(RefactoringRule::getId).collect(Collectors.toList());
	}

}
