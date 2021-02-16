package eu.jsparrow.sample.postRule.allRules;

import org.junit.Before;

import eu.jsparrow.sample.utilities.ClassUsingJUnit4TestRule;

public class MigrateJUnit4ToJupiteUnsupportedImplicitUsedTypeRule extends ClassUsingJUnit4TestRule {

	@Before
	public void before() {
		getTestRule();
	}
}
