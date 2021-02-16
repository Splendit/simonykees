package eu.jsparrow.sample.postRule.migrateJUnitToJupiter;

import org.junit.Before;

import eu.jsparrow.sample.utilities.ClassUsingJUnit4TestRule;

public class ReplaceJUnit4AnnotationsWithJupiterUnsupportedImplicitUsedTypeRule extends ClassUsingJUnit4TestRule {

	@Before
	public void before() {
		getTestRule();
	}
}
