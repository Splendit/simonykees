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
import eu.jsparrow.core.rule.impl.UseTernaryOperatorRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

@SuppressWarnings("nls")
public class UseTernaryOperatorRuleTest extends SingleRuleTest {

	private static final String SAMPLE_FILE = "TestUseTernaryOperatorRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "useTernaryOperator";

	private UseTernaryOperatorRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new UseTernaryOperatorRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		assertEquals("UseTernaryOperator", rule.getId());
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Use Ternary Operator", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY), description.getTags());
		assertEquals(5, description.getRemediationCost()
			.toMinutes());
		assertEquals(""
				+ "This rule replaces if-statements by equivalent statements using the ternary operator"
				+ " in cases where such a replacement is reasonable."
				+ " For example, the statement \"x = condition ? 1 : 0;\" is shorter and better readable"
				+ " than the corresponding if-statement.",
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
