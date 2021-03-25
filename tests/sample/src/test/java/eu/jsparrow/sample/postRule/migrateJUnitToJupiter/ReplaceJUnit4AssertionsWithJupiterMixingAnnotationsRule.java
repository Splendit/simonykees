package eu.jsparrow.sample.postRule.migrateJUnitToJupiter;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class ReplaceJUnit4AssertionsWithJupiterMixingAnnotationsRule {

	@org.junit.Test
	void testWithJUnit4Annotation() {
		assertEquals(0L, 0L);
	}

	@Test
	void testWithJUnitJupiterAnnotation() {
		Assertions.assertEquals(0L, 0L);
	}
}