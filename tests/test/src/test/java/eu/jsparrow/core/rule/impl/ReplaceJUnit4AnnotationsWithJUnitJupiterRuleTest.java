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

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;

public class ReplaceJUnit4AnnotationsWithJUnitJupiterRuleTest extends SingleRuleTest {

	private static final String SAMPLE_FILE_ALWAYS_TRANSFORMED = "ReplaceJUnit4AnnotationsWithJupiterAlwaysTransformedRule.java";
	private static final String SAMPLE_FILE_CONDITIONALLY_TRANSFORMED = "ReplaceJUnit4AnnotationsWithJupiterConditionallyTransformedRule.java";
	private static final String SAMPLE_REFERENCE_ON_TESTRULE_NOT_TRANSFORMED = "ReplaceJUnit4AnnotationsWithJupiterWithUnit4TestRuleToStringRule.java";
	private static final String SAMPLE_UNSUPPORTED_IMPLICIT_USED_TYPE = "ReplaceJUnit4AnnotationsWithJupiterUnsupportedImplicitUsedTypeRule.java";
	private static final String SAMPLE_UNSUPPORTED_SUPER_TYPE = "ReplaceJUnit4AnnotationsWithJupiterUnsupportedSuperTypeRule.java";
	private static final String SAMPLE_UNSUPPORTED_TYPE_ARGUMENT = "ReplaceJUnit4AnnotationsWithJupiterUnsupportedTypeArgumentRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "migrateJUnitToJupiter";

	private ReplaceJUnit4AnnotationsWithJupiterRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new ReplaceJUnit4AnnotationsWithJupiterRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	public void testAlwaysTransformed() throws Exception {
		loadUtilities();
		
		rule.calculateEnabledForProject(testProject);

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
	public void testUnsupportedImplicitUsedType_shouldNotTransforme() throws Exception {
		loadUtilities();

		Path preRule = getPreRuleFile(SAMPLE_UNSUPPORTED_IMPLICIT_USED_TYPE);
		Path postRule = getPostRuleFile(SAMPLE_UNSUPPORTED_IMPLICIT_USED_TYPE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testUnsupportedSuperType_shouldNotTransforme() throws Exception {
		loadUtilities();

		Path preRule = getPreRuleFile(SAMPLE_UNSUPPORTED_SUPER_TYPE);
		Path postRule = getPostRuleFile(SAMPLE_UNSUPPORTED_SUPER_TYPE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testUnsupportedParameterized_shouldNotTransforme() throws Exception {
		loadUtilities();

		Path preRule = getPreRuleFile(SAMPLE_UNSUPPORTED_TYPE_ARGUMENT);
		Path postRule = getPostRuleFile(SAMPLE_UNSUPPORTED_TYPE_ARGUMENT, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testReferenceOnTestRuleNotTransformed() throws Exception {
		loadUtilities();

		Path preRule = getPreRuleFile(SAMPLE_REFERENCE_ON_TESTRULE_NOT_TRANSFORMED);
		Path postRule = getPostRuleFile(SAMPLE_REFERENCE_ON_TESTRULE_NOT_TRANSFORMED, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void calculateEnabledForProjectShouldBeEnabled() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api",
					"5.0.0")));
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("junit", "junit", "4.13")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}
}
