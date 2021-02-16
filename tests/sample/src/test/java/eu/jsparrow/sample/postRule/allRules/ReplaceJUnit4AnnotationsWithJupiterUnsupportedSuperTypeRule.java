package eu.jsparrow.sample.postRule.allRules;

import org.junit.Before;

public class ReplaceJUnit4AnnotationsWithJupiterUnsupportedSuperTypeRule {

	private eu.jsparrow.sample.utilities.DummyTestRule rule;

	@Before
	public void before() {
		rule = new eu.jsparrow.sample.utilities.DummyTestRule();
	}
}
