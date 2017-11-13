package eu.jsparrow.core.rule;

import static org.junit.Assert.*;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.jsparrow.dummies.DummyRule;

public class EliminatedTechnicalDebtTest {

	private DummyRule dummyRule;
	private ICompilationUnit compilationUnit;

	@Before
	public void setUp() {
		dummyRule = new DummyRule();
		compilationUnit = Mockito.mock(ICompilationUnit.class);
	}

	@Test
	public void get_WithRuleAppliedOnce_ReturnsTotalDuration() {
		RuleApplicationCount.getFor(dummyRule)
			.update(compilationUnit.getHandleIdentifier());

		// The dummy rule has a remediation time of 5 minutes.
		assertEquals(5, EliminatedTechnicalDebt.get(dummyRule)
			.toMinutes());
	}

	@Test
	public void getTotal_WithTechnicalDebt_ReturnsTotalTechnicalDebt() {
		RuleApplicationCount.getFor(dummyRule)
			.update(compilationUnit.getHandleIdentifier());
		Duration total = EliminatedTechnicalDebt.getTotalFor(Arrays.asList(dummyRule, dummyRule, dummyRule));

		// The dummy rule has a remediation time of 5 minutes.
		// Total time: 5 * 3 = 15
		assertEquals(15, total.toMinutes());
	}

}
