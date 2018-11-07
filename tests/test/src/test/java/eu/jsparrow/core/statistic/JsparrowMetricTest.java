package eu.jsparrow.core.statistic;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eu.jsparrow.core.http.JsonUtil;
import eu.jsparrow.core.statistic.entity.JsparrowData;
import eu.jsparrow.core.statistic.entity.JsparrowMetric;
import eu.jsparrow.core.statistic.entity.JsparrowRuleData;

@SuppressWarnings("nls")
public class JsparrowMetricTest {

	@Test
	public void jsonMetricTest() {
		String expected = "{\"uUID\":\"1337\"," + "\"timestamp\":{\"nano\":956000000,\"epochSecond\":1540974329},"
				+ "\"projectName\":\"jSparrow\"," + "\"data\":{\"names\":[\"A rule\"]}}";
		Instant currentTime = Instant.now();

		JsparrowMetric jm = new JsparrowMetric();
		jm.setProjectName("jSparrow");
		jm.setTimestamp(currentTime);
		jm.setuUID("1337");
		JsparrowData data = new JsparrowData();
		data.setDurationOfCalculation(Duration.ofMillis(100));
		data.setTotalIssuesFixed(5);
		data.setFilesChanged(2);
		data.setFileCount(5);

		JsparrowRuleData ruleData = new JsparrowRuleData("RuleID1", 2, Duration.ofSeconds(2), 2);

		List<JsparrowRuleData> ruleList = new ArrayList<>();
		ruleList.add(ruleData);
		data.setRulesData(ruleList);

		jm.setData(data);

		String result = JsonUtil.generateJSON(jm);
		// assertEquals(expected, result);

		JsonUtil.sendJson(result);
	}
}
