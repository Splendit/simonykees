package eu.jsparrow.core.rule.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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

class ReplaceStringFormatByFormattedRuleTest extends SingleRuleTest {

	private ReplaceStringFormatByFormattedRule rule;

	@BeforeEach
	void setUp() throws Exception {
		rule = new ReplaceStringFormatByFormattedRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("ReplaceStringFormatByFormatted"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getName(), equalTo("Replace String.format by String.formatted"));
		assertEquals(Arrays.asList(Tag.JAVA_15, Tag.STRING_MANIPULATION, Tag.READABILITY), description.getTags());
		assertEquals(2, description.getRemediationCost().toMinutes());
		assertThat(description.getDescription(),
				equalTo("This rule replaces the static invocations of String.format(String, Object...) "
						+ "by invocations of the instance method String.formatted(Object...) introduced in Java 15. "
						+ "This way, eliminating some code clutter."));
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		assertThat(rule.getRequiredJavaVersion(), equalTo("15"));
	}

	@Test
	void calculateEnabledForProject_shouldReturnFalse() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_14);
		rule.calculateEnabledForProject(testProject);
		assertFalse(rule.isEnabled());
	}

	@Test
	void calculateEnabledForProject_shouldReturnTrue() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_15);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());
	}
}