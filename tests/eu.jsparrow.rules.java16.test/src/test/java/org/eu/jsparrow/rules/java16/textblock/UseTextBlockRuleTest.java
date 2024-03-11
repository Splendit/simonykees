package org.eu.jsparrow.rules.java16.textblock;

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
import eu.jsparrow.rules.java16.textblock.UseTextBlockRule;

class UseTextBlockRuleTest extends SingleRuleTest {

	private UseTextBlockRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new UseTextBlockRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		assertEquals("UseTextBlock", rule.getId());
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Use Text Block", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_15, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY), description.getTags());
		assertEquals(5, description.getRemediationCost()
			.toMinutes());
		assertEquals(""
				+ "Java 15 introduced Text Blocks to express String literals spanning several "
				+ "lines of code and significantly reduce the need for escape sequences. \nThis rule replaces "
				+ "multiline String concatenation expressions with Text Blocks String literals. Thus, removing "
				+ "some boilerplate code and increasing the readability of String expressions.",
				description.getDescription());
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		assertEquals("15", rule.getRequiredJavaVersion());
	}

	@Test
	public void calculateEnabledForProjectShouldBeDisabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_14);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}

	@Test
	public void calculateEnabledForProjectShouldBeEnabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_15);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}
}