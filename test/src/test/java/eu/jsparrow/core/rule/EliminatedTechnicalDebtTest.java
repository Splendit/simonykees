package eu.jsparrow.core.rule;

import static org.junit.Assert.*;

import java.time.Duration;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.dummies.DummyRule;

public class EliminatedTechnicalDebtTest {

	private DummyRule dummyRule;

	@Before
	public void setUp() {
		dummyRule = new DummyRule();
	}

	@Test
	public void get_WithRuleAppliedOnce_ReturnsTotalDuration() {
		RuleApplicationCount.getFor(dummyRule)
			.update();

		// The dummy rule has a remediation time of 5 minutes.
		assertEquals(5, EliminatedTechnicalDebt.get(dummyRule)
			.toMinutes());
	}

	@Test
	public void getTotal_WithTechnicalDebt_ReturnsTotalTechnicalDebt() {
		RuleApplicationCount.getFor(dummyRule)
			.update();
		Duration total = EliminatedTechnicalDebt.getTotalFor(Arrays.asList(dummyRule, dummyRule, dummyRule));

		// The dummy rule has a remediation time of 5 minutes.
		// Total time: 5 * 3 = 15
		assertEquals(15, total.toMinutes());
	}

}
