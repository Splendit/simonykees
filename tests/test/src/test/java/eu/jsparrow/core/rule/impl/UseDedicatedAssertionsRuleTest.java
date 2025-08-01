package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.common.util.RulesTestUtil.addToClasspath;
import static eu.jsparrow.common.util.RulesTestUtil.createJavaProject;
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
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class UseDedicatedAssertionsRuleTest extends SingleRuleTest {

	private static final String STANDARD_FILE = "UseDedicatedAssertionsRule.java";
	private static final String COMPARISON_WITH_CONSTANT = "UseDedicatedAssertionsSwapComparisonOperandsRule.java";
	private static final String STATIC_EQUALS = "UseDedicatedAssertionsStaticEqualsRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "dedicatedAssertions";

	private UseDedicatedAssertionsRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new UseDedicatedAssertionsRule();
		testProject = createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		assertEquals("UseDedicatedAssertions", rule.getId());
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Use Dedicated Assertions", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_5, Tag.TESTING, Tag.JUNIT, Tag.CODING_CONVENTIONS),
				description.getTags());
		assertEquals(2, description.getRemediationCost()
			.toMinutes());
		assertEquals(""
				+ "Replaces boolean assertions (e.g., 'assertTrue' and 'assertFalse') with the corresponding "
				+ "dedicated assertions when testing for equality or null values. \nFor example, "
				+ "'assertTrue(a.equals(b))' can be replaced by 'assertEquals(a, b)'. Similarly, 'assertSame', "
				+ "'assertNotSame', 'assertNull', or 'assertNotNull' can be used instead of 'assertTrue' or 'assertFalse'.",
				description.getDescription());
	}

	@Test
	void test_requiredLibraries() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api",
					"5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertEquals("JUnit 4 or JUnit 5", rule.requiredLibraries());
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api",
					"5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertEquals("1.5", rule.getRequiredJavaVersion());
	}

	@Test
	void calculateEnabledForProject_supportLibraryVersion_4_12_shouldAllReturnTrue() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("junit", "junit", "4.12")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
		assertTrue(rule.isSatisfiedLibraries());
		assertTrue(rule.isSatisfiedJavaVersion());
	}

	@Test
	void calculateEnabledForProject_supportLibraryVersion_Jupiter_shouldAllReturnTrue() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api",
					"5.0.0")));
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

	@ParameterizedTest
	@ValueSource(strings = {
			STANDARD_FILE,
			STATIC_EQUALS,
			COMPARISON_WITH_CONSTANT })
	void testTransformation(String preRuleFileName) throws Exception {

		RulesTestUtil.addToClasspath(testProject, Arrays.asList(
				RulesTestUtil.generateMavenEntryFromDepedencyString("junit", "junit", "4.13")));
		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(preRuleFileName);
		Path postRule = getPostRuleFile(preRuleFileName, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
}
