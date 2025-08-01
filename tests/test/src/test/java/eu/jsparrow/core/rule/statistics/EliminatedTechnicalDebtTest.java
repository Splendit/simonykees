package eu.jsparrow.core.rule.statistics;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.dummies.DummyRule;
import eu.jsparrow.rules.common.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.rules.common.visitor.ASTRewriteEvent;

@SuppressWarnings("nls")
public class EliminatedTechnicalDebtTest {

	private DummyRule dummyRule;

	@BeforeEach
	public void setUp() {
		dummyRule = new DummyRule();
	}

	@Test
	public void get_WithRuleAppliedOnce_ReturnsTotalDuration() {
		RuleApplicationCount.getFor(dummyRule)
			.update(new ASTRewriteEvent("test"));

		assertEquals(5, EliminatedTechnicalDebt.get(dummyRule)
			.toMinutes());
	}

	@Test
	public void get_WithRuleAppliedMoreTimes_ReturnsTotalDuration() {
		RuleApplicationCount.getFor(dummyRule)
			.update(new ASTRewriteEvent("test"));

		assertEquals(15, EliminatedTechnicalDebt.get(dummyRule, 3)
			.toMinutes());
	}

	@Test
	public void getTotal_WithTechnicalDebt_ReturnsTotalTechnicalDebt() {
		RuleApplicationCount.getFor(dummyRule)
			.update(new ASTRewriteEvent("test"));
		Duration total = EliminatedTechnicalDebt.getTotalFor(Arrays.asList(dummyRule, dummyRule, dummyRule));

		// The dummy rule has a remediation time of 5 minutes.
		// Total time: 5 * 3 = 15
		assertEquals(15, total.toMinutes());
	}

}
