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
import eu.jsparrow.core.rule.impl.AvoidConcatenationInLoggingStatementsRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class AvoidConcatenationInLoggingStatementsRuleTest extends SingleRuleTest {

	/*
	 * Note: We support logback but since logback implements slf4j interfaces,
	 * there is no reason to have separate sample files for it.
	 */
	private static final String SAMPLE_FILE_SLF4J = "AvoidConcatenationInSlf4jRule.java";
	private static final String SAMPLE_FILE_LOG4J = "AvoidConcatenationInLog4jRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "avoidConcatenation";

	private AvoidConcatenationInLoggingStatementsRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new AvoidConcatenationInLoggingStatementsRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");

	}

	@Test
	void test_ruleId() {
		assertEquals("AvoidConcatenationInLoggingStatements", rule.getId());
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Avoid Concatenation in Logging Statements", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_1, Tag.PERFORMANCE, Tag.CODING_CONVENTIONS, Tag.READABILITY, Tag.LOGGING),
				description.getTags());
		assertEquals(5, description.getRemediationCost()
			.toMinutes());
		assertEquals(""
				+ "Avoid always evaluating concatenated logging messages by introducing parameters,"
				+ " which only evaluate when the logging level is active.",
				description.getDescription());
	}

	@Test
	public void testTransformationWithSlf4jFile() throws Exception {
		Path preRule = getPreRuleFile(SAMPLE_FILE_SLF4J);
		Path postRule = getPostRuleFile(SAMPLE_FILE_SLF4J, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testTransformationWithLog4jFile() throws Exception {
		Path preRule = getPreRuleFile(SAMPLE_FILE_LOG4J);
		Path postRule = getPostRuleFile(SAMPLE_FILE_LOG4J, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void calculateEnabledForProjectShouldBeEnabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

}
