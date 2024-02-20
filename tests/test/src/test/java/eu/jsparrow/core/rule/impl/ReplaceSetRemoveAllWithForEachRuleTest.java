package eu.jsparrow.core.rule.impl;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class ReplaceSetRemoveAllWithForEachRuleTest extends SingleRuleTest {

	private static final String SAMPLE_FILE = "TestReplaceSetRemoveAllWithForEachRule.java";
	private static final String SAMPLE_FILE_NOT_TRANSFORMING = "TestReplaceSetRemoveAllWithForEachNotTransformingRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "replaceSetRemoveAll";

	private ReplaceSetRemoveAllWithForEachRule rule;

	@BeforeEach
	void setUp() throws Exception {
		rule = new ReplaceSetRemoveAllWithForEachRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		assertEquals("ReplaceSetRemoveAllWithForEach", rule.getId());
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Replace Set.removeAll With ForEach", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_8, Tag.PERFORMANCE), description.getTags());
		assertEquals(5, description.getRemediationCost()
			.toMinutes());
		assertEquals(""
				+ "Using the method 'removeAll(Collection)' in order to removing elements from a Set may lead to performance problems because of a possible O(n^2) complexity."
				+ " This rule replaces 'removeAll' invocations by corresponding 'forEach' constructs."
				+ " For example 'mySet.removeAll(myList);' is replaced by 'myList.forEach(mySet::remove);'",
				description.getDescription());
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		assertEquals("1.8", rule.getRequiredJavaVersion());
	}

	@Test
	void calculateEnabledForProject_shouldReturnFalse() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
		rule.calculateEnabledForProject(testProject);
		assertFalse(rule.isEnabled());
	}

	@Test
	void calculateEnabledForProject_shouldReturnTrue() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			SAMPLE_FILE,
			SAMPLE_FILE_NOT_TRANSFORMING })
	void testTransformation(String preRuleFileName) throws Exception {

		rule.calculateEnabledForProject(testProject);
		Path preRule = getPreRuleFile(preRuleFileName);
		Path postRule = getPostRuleFile(preRuleFileName, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
}