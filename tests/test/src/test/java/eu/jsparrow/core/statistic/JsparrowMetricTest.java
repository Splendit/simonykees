package eu.jsparrow.core.statistic;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import eu.jsparrow.core.http.JsonUtil;
import eu.jsparrow.core.statistic.entity.JsparrowData;
import eu.jsparrow.core.statistic.entity.JsparrowMetric;
import eu.jsparrow.core.statistic.entity.JsparrowRuleData;

@SuppressWarnings("nls")
public class JsparrowMetricTest {

	@Test
	@Disabled
	public void jsonMetricTest() {
		JsparrowMetric jm = new JsparrowMetric();
		jm.setRepoName("jSparrow");
		jm.setRepoOwner("Splendit");
		jm.setTimestamp(50);
		jm.setuuid("1338");
		JsparrowData data = new JsparrowData();
		data.setProjectName("Name");
		data.setTimestampGitHubStart(100);
		data.setTimestampJSparrowFinish(200);
		data.setTotalIssuesFixed(5);
		data.setTotalTimeSaved(1000);
		data.setTotalFilesChanged(2);
		data.setTotalFilesCount(5);

		JsparrowRuleData ruleData = new JsparrowRuleData("RuleID1", 2, 2, 2);

		List<JsparrowRuleData> ruleList = new ArrayList<>();
		ruleList.add(ruleData);
		data.setRules(ruleList);

		jm.setData(data);

		String result = JsonUtil.generateJSON(jm);
		// assertEquals(expected, result);

		JsonUtil.sendJsonToAwsStatisticsService(result);
	}
}
