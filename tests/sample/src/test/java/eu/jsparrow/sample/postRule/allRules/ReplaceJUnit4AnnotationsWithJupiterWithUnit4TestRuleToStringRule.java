package eu.jsparrow.sample.postRule.allRules;

import org.junit.Before;

import eu.jsparrow.sample.utilities.ClassUsingJUnit4TestRule;

public class ReplaceJUnit4AnnotationsWithJupiterWithUnit4TestRuleToStringRule extends ClassUsingJUnit4TestRule {

	@Before
	public void before() {
		testRule.toString();
	}
}