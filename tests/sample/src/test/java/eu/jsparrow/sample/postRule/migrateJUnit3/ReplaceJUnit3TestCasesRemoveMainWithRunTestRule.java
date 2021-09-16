package eu.jsparrow.sample.postRule.migrateJUnit3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class ReplaceJUnit3TestCasesRemoveMainWithRunTestRule {

	@Test
	public void testAddition() {
		assertEquals(10, 5 + 5);
	}
}