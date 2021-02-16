package eu.jsparrow.sample.postRule.migrateJUnitToJupiter;

import org.junit.Before;

public class MigrateJUnit4ToJupiteUnsupportedSuperTypeRule {

	private eu.jsparrow.sample.utilities.DummyTestRule rule;

	@Before
	public void before() {
		rule = new eu.jsparrow.sample.utilities.DummyTestRule();
	}
}
