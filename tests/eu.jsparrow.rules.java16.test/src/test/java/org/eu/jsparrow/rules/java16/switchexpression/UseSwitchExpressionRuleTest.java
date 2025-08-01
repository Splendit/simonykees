package org.eu.jsparrow.rules.java16.switchexpression;

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
import eu.jsparrow.rules.java16.switchexpression.UseSwitchExpressionRule;

class UseSwitchExpressionRuleTest extends SingleRuleTest {

	private UseSwitchExpressionRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new UseSwitchExpressionRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		assertEquals("UseSwitchExpression", rule.getId());
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Use Switch Expression", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_14, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY), description.getTags());
		assertEquals(5, description.getRemediationCost()
			.toMinutes());
		assertEquals(""
				+ "In Java 14, the switch expressions turned to a standard feature. "
				+ "This rule replaces the traditional switch-case statements with switch-case expressions. "
				+ "Thus, avoiding the fall-through semantics of control flow and at the same time, removing some boilerplate code.",
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
