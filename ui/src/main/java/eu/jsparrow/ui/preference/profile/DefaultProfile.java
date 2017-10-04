package eu.jsparrow.ui.preference.profile;

import java.util.Arrays;
import java.util.List;

import eu.jsparrow.core.rule.impl.CodeFormatterRule;
import eu.jsparrow.core.rule.impl.DiamondOperatorRule;
import eu.jsparrow.core.rule.impl.EnhancedForLoopToStreamForEachRule;
import eu.jsparrow.core.rule.impl.ForToForEachRule;
import eu.jsparrow.core.rule.impl.LambdaForEachIfWrapperToFilterRule;
import eu.jsparrow.core.rule.impl.MultiCatchRule;
import eu.jsparrow.core.rule.impl.TryWithResourceRule;
import eu.jsparrow.core.rule.impl.WhileToForEachRule;
import eu.jsparrow.i18n.Messages;

/**
 * Default profile.
 * 
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
public class DefaultProfile implements SimonykeesProfile {

	private List<String> enabledRulesIds;

	boolean isBuiltInProfile = true;

	public DefaultProfile() {
		enabledRulesIds = Arrays.asList(
				new TryWithResourceRule().getId(),
				new MultiCatchRule().getId(),
				new DiamondOperatorRule().getId(),
				new WhileToForEachRule().getId(),
				new ForToForEachRule().getId(),
				new EnhancedForLoopToStreamForEachRule().getId(),
				new LambdaForEachIfWrapperToFilterRule().getId(),
				new CodeFormatterRule().getId());
	}

	@Override
	public String getProfileName() {
		return Messages.Profile_DefaultProfile_profileName;
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
