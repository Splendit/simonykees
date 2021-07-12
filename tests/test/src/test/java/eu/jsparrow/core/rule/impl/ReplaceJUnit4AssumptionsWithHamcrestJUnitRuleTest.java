package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.core.util.RulesTestUtil.addToClasspath;
import static eu.jsparrow.core.util.RulesTestUtil.generateMavenEntryFromDepedencyString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.SingleRuleTest;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class ReplaceJUnit4AssumptionsWithHamcrestJUnitRuleTest extends SingleRuleTest {
	private static final String SAMPLE_FILE_TRANSFORM_IMPORTS = "ReplaceJUnit4AssumptionsWithHamcrestJUnitTransformImportsRule.java";

	private static final String POSTRULE_SUBDIRECTORY = "migrateJUnitToJupiter";

	private ReplaceJUnit4AssumptionsWithHamcrestJUnitRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new ReplaceJUnit4AssumptionsWithHamcrestJUnitRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("ReplaceJUnit4AssumptionsWithHamcrestJUnit"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getName(), equalTo("Replace JUnit 4 Assumptions with Hamcrest JUnit"));
		assertThat(description.getTags(),
				contains(Tag.JAVA_1_5, Tag.TESTING));
		assertThat(description.getRemediationCost(), equalTo(Duration.ofMinutes(2)));
		assertThat(description.getDescription(),
				equalTo("This rule replaces the JUnit 4 assumptions 'assumeThat', 'asssumeNoException', and 'assumeNotNull' by the equivalent Hamcrest JUnit assumption 'MatcherAssume.assumeThat'."));
	}

	@Test
	void test_requiredLibraries() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-core", "1.3"),
					generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-junit", "1.0.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);

		rule.calculateEnabledForProject(testProject);

		assertThat(rule.requiredLibraries(), equalTo("Hamcrest Core 1.3 and Hamcrest JUnit 1.0"));
	}

	@Test
	void calculateEnabledForProjectShouldBeEnabled() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-core", "1.3"),
					generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-junit", "1.0.0.0")));

		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@Test
	void calculateEnabledForProjectShouldBeDisabled() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-junit", "1.0.0.0")));

		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}

	@Test
	void calculateEnabledForJava4ProjectShouldBeDisabled() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-core", "1.3"),
					generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-junit", "1.0.0.0")));

		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}

	@Test
	void testTransformImports() throws Exception {
		loadUtilities();

		addToClasspath(testProject, Arrays.asList(
				generateMavenEntryFromDepedencyString("junit", "junit", "4.13"),
				generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-library", "1.3"),
				generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-core", "1.3"),
				generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-junit", "1.0.0.0")));

		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());

		Path preRule = getPreRuleFile(SAMPLE_FILE_TRANSFORM_IMPORTS);
		Path postRule = getPostRuleFile(SAMPLE_FILE_TRANSFORM_IMPORTS, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
}
