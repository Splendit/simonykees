package eu.jsparrow.core;

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
import eu.jsparrow.core.rule.impl.InlineLocalVariablesRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class InlineLocalVariablesRuleTest extends SingleRuleTest {

	private static final String SAMPLE_FILE = "TestInlineLocalVariablesRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "inlineLocalVariables";

	private InlineLocalVariablesRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new InlineLocalVariablesRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		assertEquals("InlineLocalVariables", rule.getId());
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Inline Local Variables", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_1, Tag.CODING_CONVENTIONS), description.getTags());
		assertEquals(2, description.getRemediationCost()
			.toMinutes());
		assertEquals("" +
				"According to the rule 'S1488' on the web site 'sonarcloud.io'," +
				" local variables should not be declared and then immediately returned or thrown." +
				" Therefore this rule scans for local variables which are exclusively used" +
				" in one single return- or throw statement and in-lines them if this is possible.",
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
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}
}
