package eu.jsparrow.core.refactorer;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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

/**
 * Used for collecting statistics data to be stored in database or logged out.
 * 
 * @since 2.7.0
 *
 */
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
	private StandaloneStatisticsMetadata statisticsMetadata;

	private RefactoringPipeline refactoringPipeline;
	private JsparrowMetric metricData;

	public StandaloneStatisticsData(int filesCount, String projectName, StandaloneStatisticsMetadata statisticsMetadata,
			RefactoringPipeline refactoringPipeline) {
		this.filesCount = filesCount;
		this.projectName = projectName;
		this.refactoringPipeline = refactoringPipeline;
		this.statisticsMetadata = statisticsMetadata;
	}

	public void setMetricData() {
		if (statisticsMetadata.isValid()) {
			populateData();
		} else {
			metricData = null;
		}
	}

	private void populateData() {
		metricData = new JsparrowMetric();

		metricData.setuuid(UUID.randomUUID()
			.toString());
		metricData.setTimestamp(Instant.now()
			.getEpochSecond());
		metricData.setRepoOwner(statisticsMetadata.getRepoOwner());
		metricData.setRepoName(statisticsMetadata.getRepoName());

		JsparrowData projectData = new JsparrowData();
		setProjectData(projectData);

		metricData.setData(projectData);
	}

	private void setProjectData(JsparrowData projectData) {
		projectData.setProjectName(projectName);
		projectData.setTimestampGitHubStart(statisticsMetadata.getStartTime());
		List<JsparrowRuleData> rulesDataList = new ArrayList<>();
		List<RefactoringRule> rulesWithChanges = getRulesWithChanges();
		for (RefactoringRule rule : rulesWithChanges) {
			JsparrowRuleData ruleData = getRuleData(rule);
			rulesDataList.add(ruleData);
			updateTotalCounter(ruleData, rule);
		}
		projectData.setRules(rulesDataList);
		projectData.setTotalIssuesFixed(numberOfTotalIssuesFixed);
		projectData.setTotalFilesChanged(changedFiles.size());
		projectData.setTotalTimeSaved(amountOfTotalTimeSaved.toMinutes());

		projectData.setTotalFilesCount(filesCount);
	}

	protected List<RefactoringRule> getRulesWithChanges() {
		return refactoringPipeline.getRules()
			.stream()
			.filter(rule -> null != refactoringPipeline.getChangesForRule(rule)
					&& !refactoringPipeline.getChangesForRule(rule)
						.isEmpty())
			.collect(Collectors.toList());
	}

	public void setEndTime(long timestampJSparrowEnd) {
		if (metricData != null) {
			metricData.getData()
				.setTimestampJSparrowFinish(timestampJSparrowEnd);
		}
	}

	public JsparrowRuleData getRuleData(RefactoringRule rule) {
		int issuesFixedForRule = RuleApplicationCount.getFor(rule)
			.toInt();
		Duration remediationCost = rule.getRuleDescription()
			.getRemediationCost();

		return new JsparrowRuleData(rule.getId(), issuesFixedForRule, remediationCost.toMinutes(),
				refactoringPipeline.getChangesForRule(rule)
					.size());
	}

	public Optional<JsparrowMetric> getMetricData() {
		return Optional.ofNullable(metricData);
	}

	protected void updateTotalCounter(JsparrowRuleData ruleData, RefactoringRule rule) {
		updateNumberOfTotalIssuesFixed(ruleData.getIssuesFixed());
		updateAmountOfTimeSavedForRule(EliminatedTechnicalDebt.get(rule));
		addFilesChangedByRule(refactoringPipeline.getChangesForRule(rule)
			.keySet());
	}

	protected void addFilesChangedByRule(Set<ICompilationUnit> files) {
		changedFiles.addAll(files);
	}

	protected void updateAmountOfTimeSavedForRule(Duration amountOfTimeSavedForRule) {
		amountOfTotalTimeSaved = amountOfTotalTimeSaved.plus(amountOfTimeSavedForRule);
	}

	protected void updateNumberOfTotalIssuesFixed(int issuesFixed) {
		numberOfTotalIssuesFixed += issuesFixed;
	}

	@SuppressWarnings("nls")
	public void logMetricData() {
		StringBuilder logString = new StringBuilder();
		if (null != metricData) {
			logString.append("Metrics for the project ")
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
					.getTotalFilesChanged())
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
					.getTimestampJSparrowFinish())
				.append(System.lineSeparator());
			logString.append("Total number of files in the project: ")
				.append(metricData.getData()
					.getTotalFilesCount())
				.append(System.lineSeparator());
			logString.append(System.lineSeparator());

			for (JsparrowRuleData ruleData : metricData.getData()
				.getRules()) {
				appendLogForRule(ruleData, logString);
			}

			logger.info(logString.toString());
		}
	}

	@SuppressWarnings("nls")
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

}