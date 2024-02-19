package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.common.util.RulesTestUtil.createJavaProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class RemoveRedundantTypeCastRuleTest extends SingleRuleTest {

	private static final String STANDARD_FILE = "RemoveRedundantTypeCastRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "redundantTypeCast";
	private RemoveRedundantTypeCastRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new RemoveRedundantTypeCastRule();
		testProject = createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("RemoveRedundantTypeCast"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getName(), equalTo("Remove Redundant Type Casts"));
		assertEquals(Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY), description.getTags());
		assertEquals(5, description.getRemediationCost().toMinutes());
		assertThat(description.getDescription(),
				equalTo("This rule removes unnecessary type cast operations. If the expression is casted "
						+ "to a type which already is exactly the type of the expression, then the type casting "
						+ "prefix is removed. \nAdditionally, also parentheses will be removed if they are "
						+ "not necessary any more."));
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertThat(rule.getRequiredJavaVersion(), equalTo("1.1"));
	}

	@Test
	void calculateEnabledForProject_supportLibraryVersion_4_13_shouldReturnTrue_shouldReturnTrue() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
		assertTrue(rule.isSatisfiedLibraries());
		assertTrue(rule.isSatisfiedJavaVersion());
	}

	@Test
	void calculateEnabledForProject_supportJunitJupiter_5_0_shouldReturnTrue() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@Test
	void testTransformationWithDefaultFile() throws Exception {
		loadUtilities();

		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(STANDARD_FILE);
		Path postRule = getPostRuleFile(STANDARD_FILE, POSTRULE_SUBDIRECTORY);
		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));
		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
}
