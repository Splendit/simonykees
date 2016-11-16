package at.splendit.simonykees.core.ui.preference.profile;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.ui.preference.SimonykeesPreferenceConstants;

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
		return "Java 8";
	}

	@Override
	public boolean isBuiltInProfile() {
		return true;
	}

	@Override
	public List<String> getEnabledRuleIds() {
		return RulesContainer.getAllRules().stream().filter(r -> r.getRequiredJavaVersion() == JavaVersion.JAVA_1_8)
				.map(RefactoringRule::getId).collect(Collectors.toList());
	}

}
