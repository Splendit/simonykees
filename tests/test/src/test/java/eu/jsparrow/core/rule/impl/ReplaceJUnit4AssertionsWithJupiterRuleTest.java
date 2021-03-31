package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.core.util.RulesTestUtil.addToClasspath;
import static eu.jsparrow.core.util.RulesTestUtil.generateMavenEntryFromDepedencyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.SingleRuleTest;
import eu.jsparrow.core.util.RulesTestUtil;

public class ReplaceJUnit4AssertionsWithJupiterRuleTest extends SingleRuleTest {

	private static final String SAMPLE_FILE_ALWAYS_TRANSFORMED = "ReplaceJUnit4AssertionsWithJupiterAlwaysTransformedRule.java";
	private static final String SAMPLE_FILE_CONDITIONALLY_TRANSFORMED = "ReplaceJUnit4AssertionsWithJupiterConditionallyTransformedRule.java";
	private static final String SAMPLE_FILE_GENERIC_METHOD_CALLS_AS_ARGUMENTS = "ReplaceJUnit4AssertionsWithJupiterGenericMethodCallsAsArgumentsRule.java";
	private static final String SAMPLE_FILE_MIXED_ANNOTATIONS = "ReplaceJUnit4AssertionsWithJupiterMixingAnnotationsRule.java";
	private static final String SAMPLE_FILE_ASSERT_THROWS = "ReplaceJUnit4AssertionsWithJupiterAssertThrowsRule.java";
	private static final String SAMPLE_FILE_THROWING_RUNNABLE = "ReplaceJUnit4AssertionsWithJupiterThrowingRunnableRule.java";
	private static final String SAMPLE_FILE_DOUBLE_OBJECTS = "ReplaceJUnit4AssertionsWithJupiterDoubleObjectsRule.java";
	private static final String SAMPLE_FILE_STRINGS = "ReplaceJUnit4AssertionsWithJupiterStringsRule.java";

	private static final String POSTRULE_SUBDIRECTORY = "migrateJUnitToJupiter";

	private ReplaceJUnit4AssertionsWithJupiterRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new ReplaceJUnit4AssertionsWithJupiterRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	public void testAlwaysTransformed() throws Exception {
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

	@Test
	public void testConditionallyTransformed() throws Exception {
		loadUtilities();

		Path preRule = getPreRuleFile(SAMPLE_FILE_CONDITIONALLY_TRANSFORMED);
		Path postRule = getPostRuleFile(SAMPLE_FILE_CONDITIONALLY_TRANSFORMED, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testGenericMethodCallsAsArguments() throws Exception {
		loadUtilities();

		Path preRule = getPreRuleFile(SAMPLE_FILE_GENERIC_METHOD_CALLS_AS_ARGUMENTS);
		Path postRule = getPostRuleFile(SAMPLE_FILE_GENERIC_METHOD_CALLS_AS_ARGUMENTS, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testMixedAnnotations() throws Exception {
		loadUtilities();

		Path preRule = getPreRuleFile(SAMPLE_FILE_MIXED_ANNOTATIONS);
		Path postRule = getPostRuleFile(SAMPLE_FILE_MIXED_ANNOTATIONS, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testAssertThrows() throws Exception {
		loadUtilities();

		Path preRule = getPreRuleFile(SAMPLE_FILE_ASSERT_THROWS);
		Path postRule = getPostRuleFile(SAMPLE_FILE_ASSERT_THROWS, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testAssertThrowinmgRunnable() throws Exception {
		loadUtilities();
		Path preRule = getPreRuleFile(SAMPLE_FILE_THROWING_RUNNABLE);
		Path postRule = getPostRuleFile(SAMPLE_FILE_THROWING_RUNNABLE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testDoubleObjects() throws Exception {
		loadUtilities();

		Path preRule = getPreRuleFile(SAMPLE_FILE_DOUBLE_OBJECTS);
		Path postRule = getPostRuleFile(SAMPLE_FILE_DOUBLE_OBJECTS, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testStrings() throws Exception {
		loadUtilities();

		Path preRule = getPreRuleFile(SAMPLE_FILE_STRINGS);
		Path postRule = getPostRuleFile(SAMPLE_FILE_STRINGS, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void calculateEnabledForProjectShouldBeEnabled() throws Exception {
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
