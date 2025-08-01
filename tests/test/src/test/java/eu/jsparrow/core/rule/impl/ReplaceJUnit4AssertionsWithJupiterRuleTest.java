package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.common.util.RulesTestUtil.addToClasspath;
import static eu.jsparrow.common.util.RulesTestUtil.generateMavenEntryFromDepedencyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;

class ReplaceJUnit4AssertionsWithJupiterRuleTest extends SingleRuleTest {

	private static final String SAMPLE_FILE_ALWAYS_TRANSFORMED = "ReplaceJUnit4AssertionsWithJupiterAlwaysTransformedRule.java";
	private static final String SAMPLE_FILE_CONDITIONALLY_TRANSFORMED = "ReplaceJUnit4AssertionsWithJupiterConditionallyTransformedRule.java";
	private static final String SAMPLE_FILE_GENERIC_METHOD_CALLS_AS_ARGUMENTS = "ReplaceJUnit4AssertionsWithJupiterGenericMethodCallsAsArgumentsRule.java";
	private static final String SAMPLE_FILE_MIXED_ANNOTATIONS = "ReplaceJUnit4AssertionsWithJupiterMixingAnnotationsRule.java";
	private static final String SAMPLE_FILE_ASSERT_THROWS = "ReplaceJUnit4AssertionsWithJupiterAssertThrowsRule.java";
	private static final String SAMPLE_FILE_THROWING_RUNNABLE = "ReplaceJUnit4AssertionsWithJupiterThrowingRunnableRule.java";
	private static final String SAMPLE_FILE_DOUBLE_OBJECTS = "ReplaceJUnit4AssertionsWithJupiterDoubleObjectsRule.java";
	private static final String SAMPLE_FILE_STRINGS = "ReplaceJUnit4AssertionsWithJupiterStringsRule.java";
	private static final String SAMPLE_TEST_FACTORY = "ReplaceJUnit4AssertionsWithJupiterTestFactoryRule.java";
	private static final String SAMPLE_TEST_TEMPLATE = "ReplaceJUnit4AssertionsWithJupiterTestTemplateRule.java";

	private static final String POSTRULE_SUBDIRECTORY = "migrateJUnitToJupiter";

	private ReplaceJUnit4AssertionsWithJupiterRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new ReplaceJUnit4AssertionsWithJupiterRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void testAlwaysTransformed() throws Exception {
		loadUtilities();

		addToClasspath(testProject, Arrays.asList(
				generateMavenEntryFromDepedencyString("junit", "junit", "4.13"),
				generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-engine", "5.4.0"),
				generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.4.0"),
				generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-params", "5.7.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());

		Path preRule = getPreRuleFile(SAMPLE_FILE_ALWAYS_TRANSFORMED);
		Path postRule = getPostRuleFile(SAMPLE_FILE_ALWAYS_TRANSFORMED, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			SAMPLE_FILE_CONDITIONALLY_TRANSFORMED,
			SAMPLE_FILE_GENERIC_METHOD_CALLS_AS_ARGUMENTS,
			SAMPLE_FILE_MIXED_ANNOTATIONS,
			SAMPLE_FILE_ASSERT_THROWS,
			SAMPLE_FILE_THROWING_RUNNABLE,
			SAMPLE_FILE_DOUBLE_OBJECTS,
			SAMPLE_FILE_STRINGS,
			SAMPLE_TEST_FACTORY,
			SAMPLE_TEST_TEMPLATE })
	void testTransformation(String preRuleFileName) throws Exception {
		loadUtilities();

		Path preRule = getPreRuleFile(preRuleFileName);
		Path postRule = getPostRuleFile(preRuleFileName, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	void calculateEnabledForProjectShouldBeEnabled() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api",
					"5.4.0")));
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("junit", "junit", "4.13")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}
}
