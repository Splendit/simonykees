package eu.jsparrow.core.refactorer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import eu.jsparrow.core.rule.impl.ArithmethicAssignmentRule;
import eu.jsparrow.core.statistic.entity.JsparrowData;
import eu.jsparrow.core.statistic.entity.JsparrowMetric;
import eu.jsparrow.core.statistic.entity.JsparrowRuleData;
import eu.jsparrow.rules.common.RefactoringRule;

public class StandaloneStatisticsDataTest {

	private int filesCount;
	private String projectName;

	private JsparrowMetric metricData;
	private JsparrowData jsparrowData;

	public TestableStandaloneStatisticsData statisticsData;

	@Mock
	private RefactoringPipeline refactoringPipeline;

	@Mock
	private ICompilationUnit compilationUnit;

	private RefactoringRule rule = new ArithmethicAssignmentRule();

	@BeforeEach
	public void setUp() throws Exception {
		filesCount = 5;
		projectName = "testProject";

		StandaloneStatisticsMetadata statisticsMetadata = new StandaloneStatisticsMetadata(100, "owner", "repoName");

		statisticsData = new TestableStandaloneStatisticsData(filesCount, projectName, statisticsMetadata,
				refactoringPipeline);

		statisticsData.setMetricData();
		statisticsData.setEndTime(Instant.now()
			.getEpochSecond());

		metricData = statisticsData.getMetricData()
			.get();

	}

	@Test
	public void setMetricDataTest() {
		assertTrue(!metricData.getRepoName()
			.isEmpty());
		assertTrue(!metricData.getRepoOwner()
			.isEmpty());
		assertTrue(metricData.getTimestamp() != 0);
		assertTrue(!metricData.getuuid()
			.isEmpty());

		assertTrue(null != metricData.getData());
	}

	@Test
	public void setJsparrowDataTest() {
		jsparrowData = metricData.getData();
		List<JsparrowRuleData> rules = jsparrowData.getRules();
		JsparrowRuleData ruleData = rules.get(0);

		assertTrue(jsparrowData.getTimestampGitHubStart() != 0);
		assertTrue(jsparrowData.getTimestampJSparrowFinish() != 0);
		assertTrue(jsparrowData.getTotalFilesChanged() != 0);
		assertTrue(jsparrowData.getTotalFilesCount() != 0);
		assertTrue(jsparrowData.getTotalIssuesFixed() != 0);
		assertTrue(jsparrowData.getTotalTimeSaved() != 0);

		assertTrue(null != jsparrowData.getRules());
		assertTrue(jsparrowData.getRules()
			.size() == 1);
		assertTrue(!ruleData.getRuleId()
			.isEmpty());
		assertTrue(ruleData.getFilesChanged() != 0);
		assertTrue(ruleData.getIssuesFixed() != 0);
		assertTrue(ruleData.getRemediationCost() != 0);

	}

	class TestableStandaloneStatisticsData extends StandaloneStatisticsData {

		public TestableStandaloneStatisticsData(int filesCount, String projectName,
				StandaloneStatisticsMetadata statisticsMetadata, RefactoringPipeline refactoringPipeline) {
			super(filesCount, projectName, statisticsMetadata, refactoringPipeline);
		}

		@Override
		protected List<RefactoringRule> getRulesWithChanges() {
			return Collections.singletonList(rule);
		}

		@Override
		public JsparrowRuleData getRuleData(RefactoringRule rule) {
			return new JsparrowRuleData(rule.getId(), 2, rule.getRuleDescription()
				.getRemediationCost()
				.toMinutes(), 2);
		}

		@Override
		protected void updateTotalCounter(JsparrowRuleData ruleData, RefactoringRule rule) {
			updateNumberOfTotalIssuesFixed(ruleData.getIssuesFixed());
			updateAmountOfTimeSavedForRule(Duration.ofMinutes(4));
			addFilesChangedByRule(Collections.singleton(compilationUnit));
		}
	}
}
