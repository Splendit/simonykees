package at.splendit.simonykees.core.ui.preference.profile;

import java.util.Arrays;
import java.util.List;

import at.splendit.simonykees.core.rule.CodeFormatterRule;
import at.splendit.simonykees.core.rule.OrganiseImportsRule;
import at.splendit.simonykees.core.ui.preference.SimonykeesPreferenceConstants;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Default profile.
 * 
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
public class DefaultProfile implements SimonykeesProfile {

	public static final String PROFILE_ID = SimonykeesPreferenceConstants.PROFILE_PREFIX + "default"; //$NON-NLS-1$

	public DefaultProfile() {
	}

	@Override
	public String getProfileId() {
		return PROFILE_ID;
	}

	@Override
	public String getProfileName() {
		return "Default";
	}

	@Override
	public boolean isBuiltInProfile() {
		return false; // obsolete, just for readability
	}

	@Override
	public List<String> getEnabledRuleIds() {
		return Arrays.asList(new CodeFormatterRule(AbstractASTRewriteASTVisitor.class).getId(),
				new OrganiseImportsRule(AbstractASTRewriteASTVisitor.class).getId());
	}

}
