package at.splendit.simonykees.ui.preference.profile;

import java.util.Arrays;
import java.util.List;

import at.splendit.simonykees.core.rule.impl.CodeFormatterRule;
import at.splendit.simonykees.core.rule.impl.DiamondOperatorRule;
import at.splendit.simonykees.core.rule.impl.EnhancedForLoopToStreamForEachRule;
import at.splendit.simonykees.core.rule.impl.ForToForEachRule;
import at.splendit.simonykees.core.rule.impl.LambdaForEachIfWrapperToFilterRule;
import at.splendit.simonykees.core.rule.impl.MultiCatchRule;
import at.splendit.simonykees.core.rule.impl.TryWithResourceRule;
import at.splendit.simonykees.core.rule.impl.WhileToForEachRule;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.core.visitor.DiamondOperatorASTVisitor;
import at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach.EnhancedForLoopToStreamForEachASTVisitor;
import at.splendit.simonykees.core.visitor.lambdaForEach.LambdaForEachIfWrapperToFilterASTVisitor;
import at.splendit.simonykees.core.visitor.loop.forToForEach.ForToForEachASTVisitor;
import at.splendit.simonykees.core.visitor.loop.whileToForEach.WhileToForEachASTVisitor;
import at.splendit.simonykees.core.visitor.tryStatement.MultiCatchASTVisitor;
import at.splendit.simonykees.core.visitor.tryStatement.TryWithResourceASTVisitor;
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
