package eu.jsparrow.ui.preference.profile;

import java.util.Arrays;
import java.util.List;

import eu.jsparrow.core.rule.impl.CollapseIfStatementsRule;
import eu.jsparrow.core.rule.impl.EnumsWithoutEqualsRule;
import eu.jsparrow.core.rule.impl.ForToForEachRule;
import eu.jsparrow.core.rule.impl.InefficientConstructorRule;
import eu.jsparrow.core.rule.impl.LambdaToMethodReferenceRule;
import eu.jsparrow.core.rule.impl.MultiVariableDeclarationLineRule;
import eu.jsparrow.core.rule.impl.OptionalFilterRule;
import eu.jsparrow.core.rule.impl.OverrideAnnotationRule;
import eu.jsparrow.core.rule.impl.PrimitiveBoxedForStringRule;
import eu.jsparrow.core.rule.impl.RemoveDoubleNegationRule;
import eu.jsparrow.core.rule.impl.RemoveEmptyStatementRule;
import eu.jsparrow.core.rule.impl.RemoveNullCheckBeforeInstanceofRule;
import eu.jsparrow.core.rule.impl.RemoveToStringOnStringRule;
import eu.jsparrow.core.rule.impl.RemoveUnnecessaryThrownExceptionsRule;
import eu.jsparrow.core.rule.impl.StringLiteralEqualityCheckRule;
import eu.jsparrow.core.rule.impl.TryWithResourceRule;
import eu.jsparrow.core.rule.impl.UseIsEmptyOnCollectionsRule;
import eu.jsparrow.core.rule.impl.UseOffsetBasedStringMethodsRule;
import eu.jsparrow.core.rule.impl.UseSecureRandomRule;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.OrganiseImportsRuleBase;

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
		enabledRulesIds = Arrays.asList(
				TryWithResourceRule.RULE_ID,
				OverrideAnnotationRule.RULE_ID,
				MultiVariableDeclarationLineRule.RULE_ID,
				EnumsWithoutEqualsRule.RULE_ID,
				RemoveDoubleNegationRule.RULE_ID,
				OptionalFilterRule.OPTIONAL_FILTER_RULE_ID,
				RemoveNullCheckBeforeInstanceofRule.RULE_ID,
				CollapseIfStatementsRule.RULE_ID,
				RemoveEmptyStatementRule.RULE_ID,
				RemoveUnnecessaryThrownExceptionsRule.RULE_ID,
				UseSecureRandomRule.RULE_ID,
				InefficientConstructorRule.RULE_ID,
				PrimitiveBoxedForStringRule.RULE_ID,
				RemoveToStringOnStringRule.RULE_ID,
				UseOffsetBasedStringMethodsRule.RULE_ID,
				StringLiteralEqualityCheckRule.RULE_ID,
				UseIsEmptyOnCollectionsRule.RULE_ID,
				ForToForEachRule.FOR_TO_FOR_EACH_RULE_ID,
				LambdaToMethodReferenceRule.RULE_ID,
				OrganiseImportsRuleBase.RULE_ID);
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
