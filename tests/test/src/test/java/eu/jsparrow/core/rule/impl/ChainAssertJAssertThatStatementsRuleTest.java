package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.common.util.RulesTestUtil.addToClasspath;
import static eu.jsparrow.common.util.RulesTestUtil.generateMavenEntryFromDepedencyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class ChainAssertJAssertThatStatementsRuleTest extends SingleRuleTest {

	private static final String SAMPLE_FILE_ALWAYS_TRANSFORMED = "ChainAssertJAssertThatStatementsAlwaysTransformedRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "chainAssertJAssertThat";

	private ChainAssertJAssertThatStatementsRule rule;

	@BeforeEach
	void setUp() throws Exception {
		rule = new ChainAssertJAssertThatStatementsRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void testAlwaysTransformed() throws Exception {
		loadUtilities();

		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(SAMPLE_FILE_ALWAYS_TRANSFORMED);
		Path postRule = getPostRuleFile(SAMPLE_FILE_ALWAYS_TRANSFORMED, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	void test_ruleId() {
		assertEquals("ChainAssertJAssertThatStatements", rule.getId());
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Chain AssertJ AssertThat Statements", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_7, Tag.TESTING, Tag.ASSERTJ, Tag.CODING_CONVENTIONS, Tag.READABILITY),
				description.getTags());
		assertEquals(5, description.getRemediationCost()
			.toMinutes());
		assertEquals(""
				+ "AssertJ encourages writing fluent test cases by chaining the assertions that target the "
				+ "same object instead of invoking 'assertThat' multiple times. This rule replaces consecutive "
				+ "AssertJ assertThat invocations targeting the same object with an assertion chain. Thus, "
				+ "eliminating some redundant code and increasing the readability of test cases.",
				description.getDescription());
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		assertEquals("1.7", rule.getRequiredJavaVersion());
	}

	@Test
	void calculateEnabledForProjectShouldBeDisabledWithJava_1_7() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}

	@Test
	void calculateEnabledForProjectShouldBeDisabledWithJava_1_6() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.assertj", "assertj-core", "3.21.0")));

		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}

	@Test
	void calculateEnabledForProjectShouldBeEnabled() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.assertj", "assertj-core", "3.21.0")));

		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@Test
	void test_requiredLibraries() throws Exception {
		assertEquals("AssertJ", rule.requiredLibraries());
	}
}
