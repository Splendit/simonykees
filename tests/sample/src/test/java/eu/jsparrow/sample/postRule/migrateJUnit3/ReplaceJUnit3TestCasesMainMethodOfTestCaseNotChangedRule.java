package eu.jsparrow.sample.postRule.migrateJUnit3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class ReplaceJUnit3TestCasesMainMethodOfTestCaseNotChangedRule {

	@Test
	public void test() throws Exception {
		assertEquals(0x7fffffff, Integer.MAX_VALUE);
	}
	
	public static void methodCalledInMain() {
		
	}

	public static void main(String[] args) {
		methodCalledInMain();
	}
}