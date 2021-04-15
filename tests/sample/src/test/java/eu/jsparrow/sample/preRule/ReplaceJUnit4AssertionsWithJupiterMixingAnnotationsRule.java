package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class ReplaceJUnit4AssertionsWithJupiterMixingAnnotationsRule {

	@org.junit.Test
	void testWithJUnit4Annotation() {
		assertEquals(0L, 0L);
	}

	@Test
	void testWithJUnitJupiterAnnotation() {
		assertEquals(0L, 0L);
	}
}