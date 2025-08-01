package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.common.util.RulesTestUtil.addToClasspath;
import static eu.jsparrow.common.util.RulesTestUtil.createJavaProject;
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

class ReplaceJUnitTimeoutAnnotationPropertyRuleTest extends SingleRuleTest {

	private static final String STANDARD_FILE = "ReplaceTimeoutAnnotationPropertyRule.java";
	private static final String ABSTRAC_FILE = "ReplaceTimeoutAnnotationPropertyRuleAbstrasctClass.java";
	private static final String POSTRULE_SUBDIRECTORY = "timeoutAnnotationProperty";

	private ReplaceJUnitTimeoutAnnotationPropertyRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new ReplaceJUnitTimeoutAnnotationPropertyRule();
		testProject = createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		assertEquals("ReplaceJUnitTimeoutAnnotationProperty", rule.getId());
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Replace JUnit Timeout Annotation Property with assertTimeout", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_8, Tag.TESTING, Tag.JUNIT, Tag.LAMBDA, Tag.READABILITY),
				description.getTags());
		assertEquals(5, description.getRemediationCost()
			.toMinutes());
		assertEquals(""
				+ "JUnit Jupiter API provides timeout assertions, i.e., assertions that execution of some code completes before a timeout exceeds."
				+ " In JUnit 4 this is achieved by using the 'timeout' property of '@Test(timeout=...)' annotation."
				+ " \nThis rule removes the 'timeout' annotation property and inserts an  'assertTimeout' instead.", //
				description.getDescription());
	}

	@Test
	void test_requiredLibraries() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertEquals("JUnit 5", rule.requiredLibraries());
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertEquals("1.8", rule.getRequiredJavaVersion());
	}

	@Test
	void calculateEnabledForProject_supportLibraryVersion_4_13_shouldReturnTrue_shouldReturnTrue() throws Exception {
		addToClasspath(testProject, Arrays.asList(generateMavenEntryFromDepedencyString("junit", "junit", "4.13")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
		assertFalse(rule.isSatisfiedLibraries());
		assertTrue(rule.isSatisfiedJavaVersion());
	}

	@Test
	void calculateEnabledForProject_supportJunitJupiter_5_0_shouldReturnTrue() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@Test
	void testTransformationWithJupiter() throws Exception {
		root = RulesTestUtil.addSourceContainer(testProject, "/allRulesTestRoot");

		RulesTestUtil.addToClasspath(testProject,
				Arrays.asList(RulesTestUtil.generateMavenEntryFromDepedencyString("junit", "junit", "4.13"),
						RulesTestUtil.generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-engine",
								"5.0.0"),
						RulesTestUtil.generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api",
								"5.0.0")));
		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(STANDARD_FILE);
		Path postRule = getPostRuleFile(STANDARD_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	void testAnnotationsInAbstractMethods() throws Exception {
		root = RulesTestUtil.addSourceContainer(testProject, "/allRulesTestRoot");

		RulesTestUtil.addToClasspath(testProject,
				Arrays.asList(RulesTestUtil.generateMavenEntryFromDepedencyString("junit", "junit", "4.13"),
						RulesTestUtil.generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-engine",
								"5.0.0"),
						RulesTestUtil.generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api",
								"5.0.0")));
		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(ABSTRAC_FILE);
		Path postRule = getPostRuleFile(ABSTRAC_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

}
