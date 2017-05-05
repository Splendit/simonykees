package at.splendit.simonykees.core.ui.preference.profile;

import java.util.Arrays;
import java.util.List;

import at.splendit.simonykees.core.rule.impl.CodeFormatterRule;
import at.splendit.simonykees.core.rule.impl.OrganiseImportsRule;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.Messages;

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
		enabledRulesIds = Arrays.asList(new CodeFormatterRule(AbstractASTRewriteASTVisitor.class).getId(),
				new OrganiseImportsRule(AbstractASTRewriteASTVisitor.class).getId());
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
