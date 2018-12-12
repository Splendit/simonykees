package eu.jsparrow.ui.preference.profile;

import java.util.Arrays;
import java.util.List;

import eu.jsparrow.core.rule.impl.EnumsWithoutEqualsRule;
import eu.jsparrow.core.rule.impl.ForToForEachRule;
import eu.jsparrow.core.rule.impl.InefficientConstructorRule;
import eu.jsparrow.core.rule.impl.LambdaToMethodReferenceRule;
import eu.jsparrow.core.rule.impl.MultiVariableDeclarationLineRule;
import eu.jsparrow.core.rule.impl.OverrideAnnotationRule;
import eu.jsparrow.core.rule.impl.PrimitiveBoxedForStringRule;
import eu.jsparrow.core.rule.impl.RemoveDoubleNegationRule;
import eu.jsparrow.core.rule.impl.RemoveEmptyStatementRule;
import eu.jsparrow.core.rule.impl.RemoveToStringOnStringRule;
import eu.jsparrow.core.rule.impl.RemoveUnnecessaryThrownExceptionsRule;
import eu.jsparrow.core.rule.impl.StringLiteralEqualityCheckRule;
import eu.jsparrow.core.rule.impl.TryWithResourceRule;
import eu.jsparrow.core.rule.impl.UseIsEmptyOnCollectionsRule;
import eu.jsparrow.i18n.Messages;

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
		enabledRulesIds = Arrays.asList(new OverrideAnnotationRule().getId(), new TryWithResourceRule().getId(),
				new LambdaToMethodReferenceRule().getId(), new RemoveUnnecessaryThrownExceptionsRule().getId(),
				new MultiVariableDeclarationLineRule().getId(), new InefficientConstructorRule().getId(),
				new RemoveEmptyStatementRule().getId(), new RemoveDoubleNegationRule().getId(),

				new ForToForEachRule().getId(), new RemoveToStringOnStringRule().getId(),
				new StringLiteralEqualityCheckRule().getId(), new EnumsWithoutEqualsRule().getId(),
				new UseIsEmptyOnCollectionsRule().getId(), new PrimitiveBoxedForStringRule().getId());
		// TODO OrganizeImports Rule
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
