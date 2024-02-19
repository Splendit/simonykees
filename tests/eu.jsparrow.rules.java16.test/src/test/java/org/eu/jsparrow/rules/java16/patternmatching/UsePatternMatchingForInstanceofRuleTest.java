package org.eu.jsparrow.rules.java16.patternmatching;

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
import eu.jsparrow.rules.java16.patternmatching.UsePatternMatchingForInstanceofRule;

class UsePatternMatchingForInstanceofRuleTest extends SingleRuleTest {

	private UsePatternMatchingForInstanceofRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new UsePatternMatchingForInstanceofRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("UsePatternMatchingForInstanceof"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Use Pattern Matching for Instanceof", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_16, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY), description.getTags());
		assertEquals(5, description.getRemediationCost().toMinutes());
		assertThat(description.getDescription(), equalTo(
				"This rule replaces instanceof expressions by Pattern Matching for instanceof introduced in Java 16. \n\nCommonly, an instanceof expression is followed by a local variable declaration initialized with a casting expression. Pattern Matching for instanceof combines three steps (i.e., type checking, variable declaration, and type casting) into a single step, thus reducing some boilerplate code and eliminating sources of errors."));
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		assertThat(rule.getRequiredJavaVersion(), equalTo("16"));
	}

	@Test
	void calculateEnabledForProjectShouldBeDisabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_15);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}

	@Test
	void calculateEnabledForProjectShouldBeEnabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_16);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}
}