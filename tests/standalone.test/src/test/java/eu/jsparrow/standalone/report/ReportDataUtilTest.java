package eu.jsparrow.standalone.report;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.statistic.entity.JsparrowData;
import eu.jsparrow.core.statistic.entity.JsparrowRuleData;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.standalone.StandaloneConfig;
import eu.jsparrow.standalone.report.model.ReportData;
import eu.jsparrow.standalone.report.model.RuleDataModel;

class ReportDataUtilTest {

	private StandaloneConfig standaloneConfig;
	private JsparrowData jSparrowData;
	private JsparrowRuleData jSparrowRuleData;
	private RefactoringRule refactoringRule;
	private Map<StandaloneConfig, List<RefactoringRule>> rulesMap;

	@BeforeEach
	public void setUp() {
		standaloneConfig = mock(StandaloneConfig.class);
		refactoringRule = mock(RefactoringRule.class);
		when(refactoringRule.getId()).thenReturn("SomeRuleId");
		RuleDescription description = new RuleDescription("Some Rule", "Some description", Duration.ofMinutes(5),
				Tag.LAMBDA);
		when(refactoringRule.getRuleDescription()).thenReturn(description);
		rulesMap = new HashMap<>();
		rulesMap.put(standaloneConfig, Collections.singletonList(refactoringRule));

		jSparrowRuleData = new JsparrowRuleData("SomeRuleId", 5, 6L, 7);
		jSparrowData = new JsparrowData();
		jSparrowData.setProjectName("project-name");
		jSparrowData.setTotalFilesCount(1);
		jSparrowData.setTotalFilesChanged(2);
		jSparrowData.setTotalIssuesFixed(3);
		jSparrowData.setTotalTimeSaved(4L);
		jSparrowData.setRules(Collections.singletonList(jSparrowRuleData));
	}

	@Test
	void test_createReportData_shouldReturnSampleData() throws Exception {

		LocalDate date = LocalDate.of(2020, 10, 29);
		ReportData report = ReportDataUtil.createReportData(jSparrowData, date, rulesMap);

		assertThat(report.getDate(), equalTo("29.10.2020"));
		assertThat(report.getProjectName(), equalTo("project-name"));
		assertThat(report.getTotalFilesCount(), equalTo(1));
		assertThat(report.getTotalFilesChanged(), equalTo(2));
		assertThat(report.getTotalIssuesFixed(), equalTo(3));
		assertThat(report.getTotalTimeSaved(), equalTo(4L));
		List<RuleDataModel> ruleDataModel = report.getRuleDataModels();
		assertThat(ruleDataModel, hasSize(1));
	}

	@Test
	void test_create_shouldReturnSampleRule() throws Exception {
		List<RuleDataModel> ruleDataModels = ReportDataUtil.mapToReportRuleDataModel(jSparrowData.getRules(), rulesMap);

		assertThat(ruleDataModels, hasSize(1));
		RuleDataModel ruleDataModel = ruleDataModels.get(0);
		assertThat(ruleDataModel.getRuleId(), equalTo("SomeRuleId"));
		assertThat(ruleDataModel.getIssuesFixed(), equalTo(5));
		assertThat(ruleDataModel.getRemediationCost(), equalTo(6L));
		assertThat(ruleDataModel.getFilesChanged(), equalTo(7));
		assertThat(ruleDataModel.getRuleLink(), equalTo("https://jsparrow.github.io/rules/some-rule-id.html"));
		assertThat(ruleDataModel.getRuleName(), equalTo("Some Rule"));
	}

}
