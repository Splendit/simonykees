package eu.jsparrow.core.statistic;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import eu.jsparrow.core.http.JsonUtil;
import eu.jsparrow.core.statistic.entity.JsparrowData;
import eu.jsparrow.core.statistic.entity.JsparrowMetric;
import eu.jsparrow.core.statistic.entity.JsparrowRuleData;

@SuppressWarnings("nls")
public class JsparrowMetricTest {

	@Test
	@Ignore
	public void jsonMetricTest() {
		String expected = "{\"uUID\":\"1337\",\"timestamp\":{\"nano\":0,\"epochSecond\":0},\"projectName\":\"jSparrow\",\"data\":{\"durationOfCalculation\":100,\"totalTimeSaved\":null,\"totalIssuesFixed\":5,\"filesChanged\":2,\"fileCount\":5,\"rulesData\":[{\"ruleId\":\"RuleID1\",\"issuesFixed\":2,\"remediationCost\":{\"seconds\":2,\"negative\":false,\"nano\":0,\"units\":[\"SECONDS\",\"NANOS\"],\"zero\":false},\"filesChanged\":2}]}}";
		Instant currentTime = Instant.ofEpochSecond(0);

		JsparrowMetric jm = new JsparrowMetric();
		jm.setProjectName("jSparrow");
		jm.setTimestamp(currentTime.getEpochSecond());
		jm.setuUID("1337");
		JsparrowData data = new JsparrowData();
		data.setDurationOfCalculation(100L);
		data.setTotalIssuesFixed(5);
		data.setTotalTimeSaved(1000L);
		data.setFilesChanged(2);
		data.setFileCount(5);

		JsparrowRuleData ruleData = new JsparrowRuleData("RuleID1", 2, 2L, 2);

		List<JsparrowRuleData> ruleList = new ArrayList<>();
		ruleList.add(ruleData);
		data.setRulesData(ruleList);

		jm.setData(data);

		String result = JsonUtil.generateJSON(jm);
		//assertEquals(expected, result);

		JsonUtil.sendJson(result);
	}
}
