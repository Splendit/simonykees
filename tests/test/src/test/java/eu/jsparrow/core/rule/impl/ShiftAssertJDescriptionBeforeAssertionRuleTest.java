package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.common.util.RulesTestUtil.addToClasspath;
import static eu.jsparrow.common.util.RulesTestUtil.createJavaProject;
import static eu.jsparrow.common.util.RulesTestUtil.generateMavenEntryFromDepedencyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class ShiftAssertJDescriptionBeforeAssertionRuleTest extends SingleRuleTest {

	private ShiftAssertJDescriptionBeforeAssertionRule rule;

	@BeforeEach
	void setUp() throws Exception {
		rule = new ShiftAssertJDescriptionBeforeAssertionRule();
		testProject = createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		assertEquals("ShiftAssertJDescriptionBeforeAssertion", rule.getId());
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Shift AssertJ Description Before Assertion", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_7, Tag.TESTING, Tag.ASSERTJ, Tag.CODING_CONVENTIONS),
				description.getTags());
		assertEquals(5, description.getRemediationCost()
			.toMinutes());
		assertEquals(""
				+ "AssertJ provides methods for setting descriptions or error messages of assertions, e.g.: as, describedAs, withFailMessage, overridingErrorMessage. "
				+ "\nThese methods should always be invoked before the actual assertion they intend to describe, otherwise, they have no effect. "
				+ "\nThis rule, swaps the invocation of the assertion methods with the invocation of the methods setting descriptions or the error "
				+ "messages for the corresponding assertions.",
				description.getDescription());
	}

	@Test
	void test_requiredLibraries() throws Exception {

		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertEquals("AssertJ", rule.requiredLibraries());
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		assertEquals("1.7", rule.getRequiredJavaVersion());
	}

	@Test
	void calculateEnabledForProject_ShouldBeDisabled() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.assertj", "assertj-core", "3.21.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}

	@Test
	void calculateEnabledForProject_ShouldBeEnabled() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.assertj", "assertj-core", "3.21.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@Test
	void calculateEnabledForProject_missingDependency_ShouldBeEnabled() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}
}
