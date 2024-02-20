package eu.jsparrow.standalone.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

		assertEquals("29.10.2020", report.getDate());
		assertEquals("project-name", report.getProjectName());
		assertEquals(1, report.getTotalFilesCount());
		assertEquals(2, report.getTotalFilesChanged());
		assertEquals(3, report.getTotalIssuesFixed());
		assertEquals(4L, report.getTotalTimeSaved());
		List<RuleDataModel> ruleDataModel = report.getRuleDataModels();
		assertEquals(1, ruleDataModel.size());
	}

	@Test
	void test_create_shouldReturnSampleRule() throws Exception {
		List<RuleDataModel> ruleDataModels = ReportDataUtil.mapToReportRuleDataModel(jSparrowData.getRules(), rulesMap);

		assertEquals(1, ruleDataModels.size());
		RuleDataModel ruleDataModel = ruleDataModels.get(0);
		assertEquals("SomeRuleId", ruleDataModel.getRuleId());
		assertEquals(5, ruleDataModel.getIssuesFixed());
		assertEquals(6L, ruleDataModel.getRemediationCost());
		assertEquals(7, ruleDataModel.getFilesChanged());
		assertEquals("https://jsparrow.github.io/rules/some-rule-id.html", ruleDataModel.getRuleLink());
		assertEquals("Some Rule", ruleDataModel.getRuleName());
	}

}
