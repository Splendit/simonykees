package eu.jsparrow.standalone.report;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import eu.jsparrow.core.statistic.RuleDocumentationURLGeneratorUtil;
import eu.jsparrow.core.statistic.entity.JsparrowRuleData;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.standalone.report.model.RuleDataModel;

/**
 * 
 * @since 3.23.0
 *
 */
public class ReportDataUtil {

	private ReportDataUtil() {
		/*
		 * Hide the default constructor.
		 */
	}

	public static List<RuleDataModel> mapToReportDataModel(Map<String, RefactoringRule> rules,
			List<JsparrowRuleData> ruleData) {
		return ruleData.stream()
			.map(jrd -> createRuleDataModel(rules.get(jrd.getRuleId()), jrd))
			.collect(Collectors.toList());
	}

	private static RuleDataModel createRuleDataModel(RefactoringRule rule,
			JsparrowRuleData jsparrowRuleData) {
		String ruleId = jsparrowRuleData.getRuleId();
		RuleDescription descirption = rule.getRuleDescription();
		String ruleName = descirption.getName();
		String link = RuleDocumentationURLGeneratorUtil.generateLinkToDocumentation(ruleId);

		return new RuleDataModel(ruleId,
				ruleName,
				link,
				jsparrowRuleData.getIssuesFixed(),
				jsparrowRuleData.getFilesChanged(),
				jsparrowRuleData.getRemediationCost());
	}

}
