package eu.jsparrow.core.rule;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.dummies.DummyRule;

public class RuleApplicationCountTest {

	private RuleApplicationCount applicationCounter;

	@Before
	public void setUp() {
		applicationCounter = new RuleApplicationCount();
	}

	@Test
	public void get_ForNewRule_ReturnsNewApplicationCounter() {
		DummyRule rule = new DummyRule();
		RuleApplicationCount ruleApplicationCounter = RuleApplicationCount.get(rule);
		assertNotNull(ruleApplicationCounter);
	}

	@Test
	public void get_ForExistingRule_ReturnsExistingApplicationCounter() {
		DummyRule rule = new DummyRule();
		RuleApplicationCount expectedApplicationCounter = RuleApplicationCount.get(rule);

		RuleApplicationCount applicationCounter = RuleApplicationCount.get(rule);
		assertEquals(expectedApplicationCounter, applicationCounter);
	}

	@Test
	public void update_OnNewApplicationCounter_ShouldIncreaseCount() throws Exception {
		int previous = applicationCounter.toInt();

		applicationCounter.update();

		assertEquals(previous + 1, applicationCounter.toInt());
	}

}
