package eu.jsparrow.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.core.util.RulesTestUtil;

public class StandardLoggerRuleLog4jTest extends SingleRuleTest {

	private static final String STANDARD_FILE = "TestStandardLoggerRule.java";
	private static final String CONFLICT_FILE = "TestStandardLoggerConflictRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "standardLoggerLog4j";

	private StandardLoggerRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new StandardLoggerRule();
		Map<String, String> options = rule.getDefaultOptions();
		options.put("new-logging-statement", "error");
		options.put("system-out-print-exception", "error");
		rule.activateOptions(options);
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		testProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		testProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
	}

	@Test
	public void testTransformationWithDefaultFile() throws Exception {
		root = RulesTestUtil.addSourceContainer(testProject, "/allRulesTestRoot");

		RulesTestUtil.addToClasspath(testProject, Arrays.asList(
				RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.logging.log4j", "log4j-api", "2.7")));
		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(STANDARD_FILE);
		Path postRule = getPostRuleFile(STANDARD_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testTransformationWithConflictFile() throws Exception {
		Path preRule = getPreRuleFile(CONFLICT_FILE);
		Path postRule = getPostRuleFile(CONFLICT_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void calculateEnabledForProjectShouldBeEnabled() throws Exception {
		RulesTestUtil.addToClasspath(testProject, Arrays.asList(
				RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.logging.log4j", "log4j-api", "2.7")));

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@Test
	public void calculateEnabledforProjectShouldBeDisabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}
}
