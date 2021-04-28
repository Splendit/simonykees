package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.core.util.RulesTestUtil.addToClasspath;
import static eu.jsparrow.core.util.RulesTestUtil.generateMavenEntryFromDepedencyString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

public class ReplaceJUnit4CategoryWithJupiterTagRuleTest extends SingleRuleTest {

	private static final String CANNOT_REMOVE_CATEGORY_IMPORT = "ReplaceJUnit4CategoryWithJupiterTagCannotRemoveCategoryImportRule.java";
	private static final String REMOVE_CATEGORY_IMPORT = "ReplaceJUnit4CategoryWithJupiterTagRemoveCategoryImportRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "migrateJUnitToJupiter";

	private ReplaceJUnit4CategoryWithJupiterTagRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new ReplaceJUnit4CategoryWithJupiterTagRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	public void testCannotRemoveCategoryImport() throws Exception {
		loadUtilities();

		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(CANNOT_REMOVE_CATEGORY_IMPORT);
		Path postRule = getPostRuleFile(CANNOT_REMOVE_CATEGORY_IMPORT, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testRemoveCategoryImport() throws Exception {
		loadUtilities();

		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(REMOVE_CATEGORY_IMPORT);
		Path postRule = getPostRuleFile(REMOVE_CATEGORY_IMPORT, POSTRULE_SUBDIRECTORY);

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

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("ReplaceJUnit4CategoryWithJupiterTag"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getName(), equalTo("Replace JUnit4 Category with JUnit Jupiter Tag"));
		assertThat(description.getTags(),
				contains(Tag.JAVA_1_8, Tag.TESTING));
		assertThat(description.getRemediationCost(), equalTo(Duration.ofMinutes(5)));
		assertThat(description.getDescription(),
				equalTo("This rule replaces each JUnit 4 @Category - annotation with one or more Jupiter @Tag - annotations."
						+ " By replacing each of these JUnit 4 annotations by the corresponding Jupiter alternatives,"
						+ " this rule promotes a stepwise transition to JUnit Jupiter."));
	}
}
