package eu.jsparrow.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.rule.impl.AvoidEvaluationOfParametersInLoggingMessagesRule;
import eu.jsparrow.core.util.RulesTestUtil;

public class AvoidEvaluationOfParametersInLoggingMessagesRuleTest extends SingleRuleTest {

	/*
	 * Note: We support logback but since logback implements slf4j interfaces,
	 * there is no reason to have separate sample files for it.
	 */
	private static final String SAMPLE_FILE_SLF4J = "AvoidEvaluationOfParametersInSlf4jRule.java";
	private static final String SAMPLE_FILE_LOG4J = "AvoidEvaluationOfParametersInLog4jRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "avoidEvaluation";

	private AvoidEvaluationOfParametersInLoggingMessagesRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new AvoidEvaluationOfParametersInLoggingMessagesRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");

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
