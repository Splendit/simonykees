package eu.jsparrow.core.refactorer;

import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import eu.jsparrow.core.rule.impl.ArithmethicAssignmentRule;
import eu.jsparrow.core.statistic.entity.JsparrowData;
import eu.jsparrow.core.statistic.entity.JsparrowMetric;
import eu.jsparrow.core.statistic.entity.JsparrowRuleData;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;

public class StandaloneStatisticsDataTest {

	private int filesCount;
	private String projectName;
	private String repoOwner;
	private String repoName;
	private long timestampGitHubStart;

	private JsparrowMetric metricData;
	private JsparrowData jsparrowData;

	public TestableStandaloneStatisticsData statisticsData;

	@Mock
	private RefactoringPipeline refactoringPipeline;

	@Mock
	private ICompilationUnit compilationUnit;

	@Mock
	private DocumentChange documentChange;

	@Mock
	private RuleApplicationCount ruleApplicationCount;

	private RefactoringRule rule = new ArithmethicAssignmentRule();

	@Before
	public void setUp() throws Exception {
		filesCount = 5;
		projectName = "testProject";
		repoOwner = "owner";
		repoName = "repoName";
		timestampGitHubStart = Instant.now()
			.toEpochMilli();

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

		@SuppressWarnings("unused")
		private int numberOfTotalIssuesFixed = 0;
		private Set<ICompilationUnit> changedFiles = new HashSet<>();
		private Duration amountOfTotalTimeSaved = Duration.ZERO;

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
			numberOfTotalIssuesFixed += ruleData.getIssuesFixed();
			Duration amountOfTimeSavedForRule = EliminatedTechnicalDebt.get(rule);
			amountOfTotalTimeSaved = amountOfTotalTimeSaved.plus(amountOfTimeSavedForRule);
			changedFiles.addAll(Collections.singletonList(compilationUnit));
		}
	}
}
