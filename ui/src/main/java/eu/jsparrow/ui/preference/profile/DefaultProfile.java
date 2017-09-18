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
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.core.visitor.DiamondOperatorASTVisitor;
import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachIfWrapperToFilterASTVisitor;
import eu.jsparrow.core.visitor.loop.fortoforeach.ForToForEachASTVisitor;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamForEachASTVisitor;
import eu.jsparrow.core.visitor.loop.whiletoforeach.WhileToForEachASTVisitor;
import eu.jsparrow.core.visitor.trycatch.MultiCatchASTVisitor;
import eu.jsparrow.core.visitor.trycatch.TryWithResourceASTVisitor;
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
				new TryWithResourceRule(TryWithResourceASTVisitor.class).getId(),
				new MultiCatchRule(MultiCatchASTVisitor.class).getId(),
				new DiamondOperatorRule(DiamondOperatorASTVisitor.class).getId(),
				new WhileToForEachRule(WhileToForEachASTVisitor.class).getId(),
				new ForToForEachRule(ForToForEachASTVisitor.class).getId(),
				new EnhancedForLoopToStreamForEachRule(EnhancedForLoopToStreamForEachASTVisitor.class).getId(),
				new LambdaForEachIfWrapperToFilterRule(LambdaForEachIfWrapperToFilterASTVisitor.class).getId(),
				new CodeFormatterRule(AbstractASTRewriteASTVisitor.class).getId());
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
