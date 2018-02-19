package eu.jsparrow.core.rule.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.dummies.DummyRule;
import eu.jsparrow.rules.common.visitor.ASTRewriteEvent;

@SuppressWarnings("nls")
public class RuleApplicationCountTest {

	private RuleApplicationCount applicationCounter;

	@Before
	public void setUp() {
		applicationCounter = new RuleApplicationCount();
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

		RuleApplicationCount actualApplicationCounter = RuleApplicationCount.getFor(rule);
		assertEquals(expectedApplicationCounter, actualApplicationCounter);
	}

	@Test
	public void get_forNewCompilationUnit_returnsFileCounter() {
		String expectedFileName = "NewFile";
		FileChangeCount fileChangeCount = applicationCounter.getApplicationsForFile(expectedFileName);

		assertEquals(expectedFileName, fileChangeCount.getCompilationUnitHandle());
	}

	@Test
	public void get_forMultipleCompilationUnits_returnsFileCounter() {
		List<String> compilationUnits = Arrays.asList("NewFile1", "NewFile2", "NewFile3");
		int result = applicationCounter.getApplicationsForFiles(compilationUnits);

		assertEquals(0, result);
	}

	@Test
	public void update_OnNewApplicationCounter_ShouldIncreaseCount() {
		int previous = applicationCounter.toInt();

		applicationCounter.update(new ASTRewriteEvent("test"));

		assertEquals(previous + 1, applicationCounter.toInt());
	}

}
