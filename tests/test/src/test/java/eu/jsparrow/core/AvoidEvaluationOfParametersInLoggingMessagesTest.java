package eu.jsparrow.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.rule.impl.AvoidEvaluationOfParametersInLoggingMessagesRule;
import eu.jsparrow.core.util.RulesTestUtil;

public class AvoidEvaluationOfParametersInLoggingMessagesTest extends SingleRuleTest {
	
	private static final String SAMPLE_FILE = "avoidEvaluation/slf4j/AvoidEvaluationOfParametersInLoggingMessages.java";
	private static final String POSTRULE_SUBDIRECTORY = "avoidEvaluation/slf4j";

	private AvoidEvaluationOfParametersInLoggingMessagesRule rule;
	
	@BeforeEach
	public void setUp() throws Exception {
		rule = new AvoidEvaluationOfParametersInLoggingMessagesRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
		
	}
	
	@Test
	public void testTransformationWithDefaultFile() throws Exception {
		Path preRule = getPreRuleFile(SAMPLE_FILE);
		Path postRule = getPostRuleFile(SAMPLE_FILE, POSTRULE_SUBDIRECTORY);

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
