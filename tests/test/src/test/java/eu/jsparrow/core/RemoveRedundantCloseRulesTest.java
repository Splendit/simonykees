package eu.jsparrow.core;

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
import eu.jsparrow.core.rule.impl.RemoveRedundantCloseRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class RemoveRedundantCloseRulesTest extends SingleRuleTest {

	private static final String SAMPLE_FILE = "TestRemoveRedundantCloseRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "removeRedundantClose";

	private RemoveRedundantCloseRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new RemoveRedundantCloseRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		assertEquals("RemoveRedundantClose", rule.getId());
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Remove Redundant Close", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_7, Tag.CODING_CONVENTIONS, Tag.READABILITY), description.getTags());
		assertEquals(2, description.getRemediationCost()
			.toMinutes());
		assertEquals(""
				+ "In Java, the try-with-resource statements are able to automatically close "
				+ "the resources which are defined in the try-with-resource header. Thus, any "
				+ "explicit 'close()' invocation in the try block is redundant and potentially "
				+ "confusing. This rule eliminates redundant resource 'close()' invocations.",
				description.getDescription());
	}

	@Test
	void testTransformationWithDefaultFile() throws Exception {
		Path preRule = getPreRuleFile(SAMPLE_FILE);
		Path postRule = getPostRuleFile(SAMPLE_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	void calculateEnabledForProjectShouldBeEnabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@Test
	void calculateEnabledforProjectShouldBeDisabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}
}
