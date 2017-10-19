package eu.jsparrow.core.rule;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ApplicationCounterTest {

	private RuleApplicationCounter applicationCounter;

	@Before
	public void setUp() {
		applicationCounter = new RuleApplicationCounter();
	}

	@Test
	public void get_ForNewRule_ReturnsNewApplicationCounter() {
		DummyRule rule = new DummyRule();
		RuleApplicationCounter ruleApplicationCounter = RuleApplicationCounter.get(rule);
		assertNotNull(ruleApplicationCounter);
	}

	@Test
	public void get_ForExistingRule_returnsExistingApplicationCounter() {
		DummyRule rule = new DummyRule();
		RuleApplicationCounter expectedApplicationCounter = RuleApplicationCounter.get(rule);

		RuleApplicationCounter applicationCounter = RuleApplicationCounter.get(rule);
		assertEquals(expectedApplicationCounter, applicationCounter);
	}

	@Test
	public void update_OnNewApplicationCounter_ShouldIncreaseCount() throws Exception {
		int previous = applicationCounter.get();

		applicationCounter.update();
		
		assertEquals(previous + 1, applicationCounter.get());
	}

}
