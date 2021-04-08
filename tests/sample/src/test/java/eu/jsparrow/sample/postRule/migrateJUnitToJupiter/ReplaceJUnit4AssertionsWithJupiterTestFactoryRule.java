package eu.jsparrow.sample.postRule.migrateJUnitToJupiter;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.Assertions;

public class ReplaceJUnit4AssertionsWithJupiterTestFactoryRule {

	@TestFactory
	Collection<DynamicTest> dynamicTestsFromCollection() {
		Assertions.assertEquals(0L, 0L, "Expected: 0L == 0L");
		return Arrays.asList(
				dynamicTest("1st dynamic test", () -> assertEquals("Expected: 1L == 1L", 1L, 1L)),
				dynamicTest("2nd dynamic test", () -> assertEquals("Expected: 2L == 2L", 2L, 2L)));
	}
}