package eu.jsparrow.core.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.jsparrow.dummies.DummyRule;

public class RuleApplicationCountTest {

	private RuleApplicationCount applicationCounter;
	private ICompilationUnit compilationUnit;

	@Before
	public void setUp() {
		applicationCounter = new RuleApplicationCount();
		compilationUnit = Mockito.mock(ICompilationUnit.class);
	}

	@Test
	public void get_ForNewRule_ReturnsNewApplicationCounter() {
		DummyRule rule = new DummyRule();
		RuleApplicationCount ruleApplicationCounter = RuleApplicationCount.getFor(rule);
		assertNotNull(ruleApplicationCounter);
	}

	@Test
	public void get_ForExistingRule_ReturnsExistingApplicationCounter() {
		DummyRule rule = new DummyRule();
		RuleApplicationCount expectedApplicationCounter = RuleApplicationCount.getFor(rule);

		RuleApplicationCount applicationCounter = RuleApplicationCount.getFor(rule);
		assertEquals(expectedApplicationCounter, applicationCounter);
	}

	@Test
	public void update_OnNewApplicationCounter_ShouldIncreaseCount() throws Exception {
		int previous = applicationCounter.toInt();

		applicationCounter.update(compilationUnit.getHandleIdentifier());

		assertEquals(previous + 1, applicationCounter.toInt());
	}

}
