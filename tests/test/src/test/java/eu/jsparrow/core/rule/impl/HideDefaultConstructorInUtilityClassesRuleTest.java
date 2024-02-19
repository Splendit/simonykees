package eu.jsparrow.core.rule.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class HideDefaultConstructorInUtilityClassesRuleTest extends SingleRuleTest {

	private static final String STANDARD_FILE = "HideDefaultConstructorInUtilityClassRule.java";
	private static final String INTERFACE_FILE = "HideDefaultConstructorInUtilityInterfaceRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "hideDefaultConstructor";

	private HideDefaultConstructorInUtilityClassesRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new HideDefaultConstructorInUtilityClassesRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("HideDefaultConstructorInUtilityClasses"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Hide Default Constructor In Utility Classes", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_1, Tag.CODING_CONVENTIONS), description.getTags());
		assertEquals(5, description.getRemediationCost().toMinutes());
		assertThat(description.getDescription(), equalTo(
				"Utility classes are classes containing static properties only. Those classes should not be instantiated. A "
						+ "private constructor, throwing an IllegalStateException, is introduced to utility classes by this rule, unless "
						+ "they already have another declared constructor. This hides the default public constructor and thus "
						+ "prevents the instantiation of such a class."));
	}

	@Test
	void test_requiredLibraries() throws Exception {

		rule.calculateEnabledForProject(testProject);

		assertThat(rule.requiredLibraries(), nullValue());
	}

	@Test
	void test_requiredJavaVersion() throws Exception {

		rule.calculateEnabledForProject(testProject);

		assertThat(rule.getRequiredJavaVersion(), equalTo("1.1"));
	}

	@Test
	void calculateEnabledForProject_shouldReturnTrue() throws Exception {

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
		assertTrue(rule.isSatisfiedLibraries());
		assertTrue(rule.isSatisfiedJavaVersion());
	}

	@Test
	void testTransformationWithDefaultFile() throws Exception {
		root = RulesTestUtil.addSourceContainer(testProject, "/allRulesTestRoot");

		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(STANDARD_FILE);
		Path postRule = getPostRuleFile(STANDARD_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	void testTransformationWithInterfaceFile() throws Exception {
		root = RulesTestUtil.addSourceContainer(testProject, "/allRulesTestRoot");

		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(INTERFACE_FILE);
		Path postRule = getPostRuleFile(INTERFACE_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
}
