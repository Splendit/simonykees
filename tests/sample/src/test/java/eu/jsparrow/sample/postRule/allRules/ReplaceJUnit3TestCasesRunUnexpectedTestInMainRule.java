package eu.jsparrow.sample.postRule.allRules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ReplaceJUnit3TestCasesRunUnexpectedTestInMainRule {

	@Test
	public void testAddition() {
		assertEquals(10, 5 + 5);
	}

	public static void main(String[] args) {
		final String helloWorld = "Hello World!";
		junit.textui.TestRunner.run(eu.jsparrow.sample.utilities.UnexpectedTestCase.class);
	}
}