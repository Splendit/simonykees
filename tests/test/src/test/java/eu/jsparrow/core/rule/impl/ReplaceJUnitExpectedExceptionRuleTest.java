package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.common.util.RulesTestUtil.addToClasspath;
import static eu.jsparrow.common.util.RulesTestUtil.createJavaProject;
import static eu.jsparrow.common.util.RulesTestUtil.generateMavenEntryFromDepedencyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class ReplaceJUnitExpectedExceptionRuleTest extends SingleRuleTest {

	private ReplaceJUnitExpectedExceptionRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new ReplaceJUnitExpectedExceptionRule();
		testProject = createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		assertEquals("ReplaceJUnitExpectedException", rule.getId());
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Replace JUnit ExpectedException with assertThrows", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_8, Tag.TESTING, Tag.JUNIT, Tag.LAMBDA, Tag.READABILITY),
				description.getTags());
		assertEquals(5, description.getRemediationCost()
			.toMinutes());
		assertEquals(""
				+ "The 'ExpectedException.none()' rule is deprecated since JUnit 4.13."
				+ " The recommended alternative is to use 'assertThrows()'."
				+ " This makes JUnit tests easier to understand and prevents scenarios where some parts of the test code are unreachable."
				+ " \nThe goal of this rule is to replace 'expectedException.expect()' with 'assertThrows()'."
				+ " Additionally, new assertions are added for each invocation of 'expectMessage()' and 'expectCause()'.", //
				description.getDescription());
	}

	@Test
	void test_requiredLibraries() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api",
					"5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertEquals("JUnit 4.13 or JUnit 5", rule.requiredLibraries());
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api",
					"5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertEquals("1.8", rule.getRequiredJavaVersion());
	}

	@Test
	void calculateEnabledForProject_supportLibraryVersion_4_13_shouldReturnTrue_shouldReturnTrue() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("junit", "junit", "4.13")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
		assertTrue(rule.isSatisfiedLibraries());
		assertTrue(rule.isSatisfiedJavaVersion());
	}

	@Test
	void calculateEnabledForProject_supportJunitJupiter_5_0_shouldReturnTrue() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api",
					"5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}
}
