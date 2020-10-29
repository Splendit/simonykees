package eu.jsparrow.standalone.report;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import eu.jsparrow.core.statistic.RuleDocumentationURLGeneratorUtil;
import eu.jsparrow.core.statistic.entity.JsparrowData;
import eu.jsparrow.core.statistic.entity.JsparrowRuleData;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.standalone.StandaloneConfig;
import eu.jsparrow.standalone.report.model.ReportData;
import eu.jsparrow.standalone.report.model.RuleDataModel;

/**
 * A utility class for generating the data for the HTML report.
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

	/**
	 * Extracts the necessary data for the jSparrow report.
	 * 
	 * @param standaloneConfigs
	 *            the configurations for each project after the refactoring has
	 *            been computed.
	 * @param jSparrowData
	 *            the summary of the jSparrow data for all projects.
	 * @param date
	 *            the current date for the report.
	 * @return the extracted data.
	 */
	public static ReportData createReportData(List<StandaloneConfig> standaloneConfigs, JsparrowData jSparrowData,
			Date date) {
		List<RuleDataModel> ruleDataModels = mapToReportRuleDataModel(standaloneConfigs,
				jSparrowData.getRules());
		String projectName = jSparrowData.getProjectName();
		int totalIssuesFixed = jSparrowData.getTotalIssuesFixed();
		int totalFilesCount = jSparrowData.getTotalFilesCount();
		int totalFilesChanged = jSparrowData.getTotalFilesChanged();
		long totalTimeSaved = jSparrowData.getTotalTimeSaved();
		return new ReportData(
				projectName,
				date,
				totalIssuesFixed,
				totalFilesCount,
				totalFilesChanged,
				totalTimeSaved,
				ruleDataModels);
	}

	public static List<RuleDataModel> mapToReportRuleDataModel(List<StandaloneConfig> standaloneConfigs,
			List<JsparrowRuleData> ruleData) {

		Map<String, RefactoringRule> ruleIdsMap = standaloneConfigs.stream()
			.map(StandaloneConfig::getProjectRules)
			.flatMap(List::stream)
			.collect(Collectors.toMap(RefactoringRule::getId,
					Function.identity(),
					(r1, r2) -> r1 /* simply drop the duplicates */));

		return ruleData.stream()
			.map(jrd -> createRuleDataModel(ruleIdsMap.get(jrd.getRuleId()), jrd))
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
