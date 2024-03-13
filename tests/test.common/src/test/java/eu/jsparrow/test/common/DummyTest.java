package eu.jsparrow.test.common;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import eu.jsparrow.common.SingleRuleTest;

/**
 * At least one test is needed by tycho surefire plugin. 
 * 
 * @since 4.4.0
 *
 */
public class DummyTest extends SingleRuleTest {

	@Test
	void test() {
		assertTrue(true);
	}
}
