package org.eu.jsparrow.rules.java16.switchexpression.ifstatement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.java16.switchexpression.ifstatement.ReplaceMultiBranchIfBySwitchRule;

class ReplaceMultiBranchIfBySwitchRuleTest extends SingleRuleTest {

	private ReplaceMultiBranchIfBySwitchRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new ReplaceMultiBranchIfBySwitchRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		assertEquals("ReplaceMultiBranchIfBySwitch", rule.getId());
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Replace Multi-Branch If By Switch", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_14, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY), description.getTags());
		assertEquals(15, description.getRemediationCost()
			.toMinutes());
		assertEquals(""
				+ "In Java 14, the switch expressions turned to a standard feature."
				+ " This rule replaces multi-branch if statements by corresponding switch expressions or switch statements with switch labeled rules."
				+ " Because this rule removes a lot of redundant parts of code, readability is improved.", //
				description.getDescription());
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		assertEquals("14", rule.getRequiredJavaVersion());
	}

	@Test
	void calculateEnabledForProjectShouldBeDisabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_13);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}

	@Test
	void calculateEnabledForProjectShouldBeEnabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_14);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}
}
