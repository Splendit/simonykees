package eu.jsparrow.sample.postRule.allRules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ReplaceJUnit4AssertionsWithJupiterAlwaysTransformedRule {

	@BeforeAll
	public static void beforeAll() {
		assertEquals(0L, 0L, "0L equals 0L");
	}

	@BeforeEach
	public void beforeEach() {
		assertEquals(0L, 0L, "0L equals 0L");
	}

	@AfterAll
	public static void afterAll() {
		assertEquals(0L, 0L, "0L equals 0L");
	}

	@AfterEach
	public void afterEach() {
		assertEquals(0L, 0L, "0L equals 0L");
	}

	@Test
	public void test() throws Exception {
		assertEquals(0L, 0L, "0L equals 0L");
	}

	@Disabled
	public void testWithDisabledAnnotation() throws Exception {
		assertEquals(0L, 0L, "0L equals 0L");
	}
}