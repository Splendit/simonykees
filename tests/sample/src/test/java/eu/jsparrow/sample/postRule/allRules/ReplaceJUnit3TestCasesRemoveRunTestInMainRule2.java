package eu.jsparrow.sample.postRule.allRules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ReplaceJUnit3TestCasesRemoveRunTestInMainRule2 {

	@Test
	public void testAddition() {
		assertEquals(10, 5 + 5);
	}

	public static void main(String[] args) {
		final String helloWorld = "Hello World!";
	}
}