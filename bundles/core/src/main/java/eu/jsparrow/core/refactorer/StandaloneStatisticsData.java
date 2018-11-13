package eu.jsparrow.core.refactorer;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.ICompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.statistic.entity.JsparrowData;
import eu.jsparrow.core.statistic.entity.JsparrowMetric;
import eu.jsparrow.core.statistic.entity.JsparrowRuleData;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;

public class StandaloneStatisticsData {

	private static final Logger logger = LoggerFactory.getLogger(StandaloneStatisticsData.class);

	/**
	 * Fields for statistics
	 */
	private int numberOfTotalIssuesFixed = 0;
	private Set<ICompilationUnit> changedFiles = new HashSet<>();
	private Duration amountOfTotalTimeSaved = Duration.ZERO;
	private int filesCount;
	private String projectName;
	private String repoOwner;
	private String repoName;
	private long timestampGitHubStart;

	private RefactoringPipeline refactoringPipeline;
	private JsparrowMetric metricData;

	public StandaloneStatisticsData(int filesCount, String projectName, String repoOwner, String repoName,
			long timestampGitHubStart, RefactoringPipeline refactoringPipeline) {
		this.filesCount = filesCount;
		this.projectName = projectName;
		this.refactoringPipeline = refactoringPipeline;
		this.repoOwner = repoOwner;
		this.repoName = repoName;
		this.timestampGitHubStart = timestampGitHubStart;
		metricData = new JsparrowMetric();
	}

	public void setMetricData() {
		metricData.setuUID(UUID.randomUUID()
			.toString());
		metricData.setTimestamp(Instant.now()
			.getEpochSecond());
		metricData.setRepoOwner(repoOwner);
		metricData.setRepoName(repoName);

		JsparrowData projectData = new JsparrowData();
		projectData.setProjectName(projectName);
		projectData.setTimestampGitHubStart(timestampGitHubStart);
		List<JsparrowRuleData> rulesDataList = new ArrayList<>();
		List<RefactoringRule> rulesWithChanges = new ArrayList<>();
		rulesWithChanges = refactoringPipeline.getRules()
			.stream()
			.filter(rule -> null != refactoringPipeline.getChangesForRule(rule)
					&& !refactoringPipeline.getChangesForRule(rule)
						.isEmpty())
			.collect(Collectors.toList());
		for (RefactoringRule rule : rulesWithChanges) {
			rulesDataList.add(getRuleData(rule));
		}
		projectData.setRulesData(rulesDataList);
		projectData.setTotalIssuesFixed(numberOfTotalIssuesFixed);
		projectData.setFilesChanged(changedFiles.size());
		projectData.setTotalTimeSaved(amountOfTotalTimeSaved.toMinutes());

		projectData.setFileCount(filesCount);

		metricData.setData(projectData);
	}

	public void setEndTime(long timestampJSparrowEnd) {
		metricData.getData()
			.setTimestampJSparrowEnd(timestampJSparrowEnd);
	}

	public void logMetricData() {
		StringBuilder logString = new StringBuilder().append("Metrics for the project ")
			.append(metricData.getRepoName())
			.append(", with owner ")
			.append(metricData.getRepoOwner())
			.append(":")
			.append(System.lineSeparator());
		logString.append("Number of total issues fixed: ")
			.append(metricData.getData()
				.getTotalIssuesFixed())
			.append(System.lineSeparator());
		logString.append("Number of total files changed: ")
			.append(metricData.getData()
				.getFilesChanged())
			.append(System.lineSeparator());
		logString.append("Total amount of time saved: ")
			.append(metricData.getData()
				.getTotalTimeSaved())
			.append(System.lineSeparator());
		logString.append("Project name: ")
			.append(metricData.getData()
				.getProjectName())
			.append(System.lineSeparator());
		logString.append("Start of the GitHub App: ")
			.append(metricData.getData()
				.getTimestampGitHubStart())
			.append(System.lineSeparator());
		logString.append("End of the jSparrow refactoring: ")
			.append(metricData.getData()
				.getTimestampJSparrowEnd())
			.append(System.lineSeparator());
		logString.append("Total number of files in the project: ")
			.append(metricData.getData()
				.getFileCount())
			.append(System.lineSeparator());
		logString.append(System.lineSeparator());

		for (JsparrowRuleData ruleData : metricData.getData()
			.getRulesData()) {
			appendLogForRule(ruleData, logString);
		}
		logger.info(logString.toString());
	}

	private void appendLogForRule(JsparrowRuleData ruleData, StringBuilder logString) {
		logString.append("Metric for rule with id ")
			.append(ruleData.getRuleId())
			.append(":")
			.append(System.lineSeparator());
		logString.append("Number of issues fixed: ")
			.append(ruleData.getIssuesFixed())
			.append(System.lineSeparator());
		logString.append("Number of files changed: ")
			.append(ruleData.getFilesChanged())
			.append(System.lineSeparator());
		logString.append("Amount of time saved: ")
			.append(ruleData.getIssuesFixed() * ruleData.getRemediationCost())
			.append(System.lineSeparator());
		logString.append(System.lineSeparator());
	}

	public JsparrowRuleData getRuleData(RefactoringRule rule) {
		// number of issues fixed
		int numberOfIssuesFixedForRule = RuleApplicationCount.getFor(rule)
			.toInt();
		numberOfTotalIssuesFixed += numberOfIssuesFixedForRule;
		// amount of time saved
		Duration amountOfTimeSavedForRule = EliminatedTechnicalDebt.get(rule);
		amountOfTotalTimeSaved = amountOfTotalTimeSaved.plus(amountOfTimeSavedForRule);
		Duration remediationCost = rule.getRuleDescription()
			.getRemediationCost();
		changedFiles.addAll(refactoringPipeline.getChangesForRule(rule)
			.keySet());

		return new JsparrowRuleData(rule.getId(), numberOfIssuesFixedForRule, remediationCost.toMinutes(),
				refactoringPipeline.getChangesForRule(rule)
					.size());
	}

	
	public JsparrowMetric getMetricData() {
		return metricData;
	}
}