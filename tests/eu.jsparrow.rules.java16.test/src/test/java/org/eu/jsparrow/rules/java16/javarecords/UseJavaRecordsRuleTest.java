package org.eu.jsparrow.rules.java16.javarecords;

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
import eu.jsparrow.rules.java16.javarecords.UseJavaRecordsRule;

class UseJavaRecordsRuleTest extends SingleRuleTest {

	private UseJavaRecordsRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new UseJavaRecordsRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		assertEquals("UseJavaRecords", rule.getId());
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Use Java Records", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_16, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY), description.getTags());
		assertEquals(20, description.getRemediationCost()
			.toMinutes());
		assertEquals(""
				+ "Since Java 16, record classes are a new kind of class in the Java language. "
				+ "Record classes help to model plain data aggregates with less ceremony than normal classes. "
				+ "This rule replaces the declarations of local classes, inner classes, and package private "
				+ "root classes with record class declarations.", //
				description.getDescription());
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		assertEquals("16", rule.getRequiredJavaVersion());
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