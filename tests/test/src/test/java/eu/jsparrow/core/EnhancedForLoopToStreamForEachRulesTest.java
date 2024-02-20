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
import eu.jsparrow.core.rule.impl.EnhancedForLoopToStreamForEachRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class EnhancedForLoopToStreamForEachRulesTest extends SingleRuleTest {

	private static final String SAMPLE_FILE = "EnhancedForLoopToStreamForEachRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "enhancedForLoopToStreamForEach";

	private EnhancedForLoopToStreamForEachRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new EnhancedForLoopToStreamForEachRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		assertEquals("EnhancedForLoopToStreamForEach", rule.getId());
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Replace For-Loop with Iterable::forEach", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA, Tag.LOOP), description.getTags());
		assertEquals(15, description.getRemediationCost()
			.toMinutes());
		String ruleDescription = ""
				+ "Enhanced For-Loops can be replaced by forEach().\n"
				+ "\n"
				+ "For example 'for(Item item: items) { }' becomes 'items.forEach()'.\n"
				+ "\n"
				+ "This makes code more readable and can be combined with other stream functions such as filter and map.";
		assertEquals(ruleDescription, description.getDescription());
	}

	@Test
	void testTransformationWithDefaultFile() throws Exception {
		loadUtilities();
		Path preRule = getPreRuleFile(SAMPLE_FILE);
		Path postRule = getPostRuleFile(SAMPLE_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	void calculateEnabledForProjectShouldBeEnabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@Test
	void calculateEnabledForProjectShouldBeDisabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}
}
