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
				TryWithResourceRule.TRY_WITH_RESOURCE_RULE_ID,
				OverrideAnnotationRule.OVERRIDE_ANNOTATION_RULE_ID,
				MultiVariableDeclarationLineRule.MULTI_VARIABLE_DECLARATION_LINE_RULE_ID,
				EnumsWithoutEqualsRule.ENUMS_WITHOUT_EQUALS_RULE_ID,
				RemoveDoubleNegationRule.REMOVE_DOUBLE_NEGATION_RULE_ID,
				OptionalFilterRule.OPTIONAL_FILTER_RULE_ID,
				RemoveNullCheckBeforeInstanceofRule.REMOVE_NULL_CHECKS_BEFORE_INSTANCE_OF_RULE_ID,
				CollapseIfStatementsRule.COLLAPSE_IF_STATEMENTS_RULE_ID,
				RemoveEmptyStatementRule.REMOVE_EMPTY_STATEMENT_RULE_ID,
				RemoveUnnecessaryThrownExceptionsRule.REMOVE_UNNECESSARY_THROWN_EXCEPTIONS_RULE_ID,
				UseSecureRandomRule.USE_SECURE_RANDOM_RULE_ID,
				InefficientConstructorRule.INEFFICIENT_CONSTRUCTOR_RULE_ID,
				PrimitiveBoxedForStringRule.PRIMITIVE_BOXED_FOR_STRING_RULE_ID,
				RemoveToStringOnStringRule.REMOVE_TO_STRING_ON_STRING_RULE_ID,
				UseOffsetBasedStringMethodsRule.USE_OFFSET_BASED_STRING_METHODS_RULE_ID,
				StringLiteralEqualityCheckRule.STRING_LITERAL_EQUALITY_CHECK_RULE_ID,
				UseIsEmptyOnCollectionsRule.USE_IS_EMPTY_ON_COLLECTIONS_RULE_ID,
				ForToForEachRule.FOR_TO_FOR_EACH_RULE_ID,
				LambdaToMethodReferenceRule.LAMBDA_TO_METHOD_REFERENCE_RULE_ID,
				OrganiseImportsRuleBase.ORGANISE_IMPORTS_RULE_ID);
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
