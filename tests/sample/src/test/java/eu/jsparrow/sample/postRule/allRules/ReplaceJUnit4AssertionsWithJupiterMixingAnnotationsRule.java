package eu.jsparrow.sample.postRule.allRules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ReplaceJUnit4AssertionsWithJupiterMixingAnnotationsRule {

	@Test
	void testWithJUnit4Annotation() {
		assertEquals(0L, 0L);
	}

	@Test
	void testWithJUnitJupiterAnnotation() {
		assertEquals(0L, 0L);
	}
}