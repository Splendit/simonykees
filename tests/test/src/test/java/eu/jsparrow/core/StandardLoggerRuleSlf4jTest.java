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

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;

public class StandardLoggerRuleSlf4jTest extends SingleRuleTest {

	private static final String STANDARD_FILE = "TestStandardLoggerRule.java";
	private static final String CONFLICT_FILE = "TestStandardLoggerConflictRule.java";
	private static final String EXISTING_LOGGER_FILE = "TestStandardLoggerExistingSlf4jLogger.java";
	private static final String EXISTING_NONSTATIC_LOGGER_FILE = "TestStandardLoggerExistingNonStaticSlf4jLogger.java";
	private static final String EXISTING_NOMODIFIERS_LOGGER_FILE = "TestStandardLoggerExistingNoModifiersSlf4jLogger.java";
	private static final String POSTRULE_SUBDIRECTORY = "standardLoggerSlf4j";

	private StandardLoggerRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new StandardLoggerRule();
		Map<String, String> replaceOptions = rule.getDefaultOptions();

		replaceOptions.put("new-logging-statement", "error");
		replaceOptions.put("system-out-print-exception", "error");
		rule.activateOptions(replaceOptions);

		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	public void testTransformationWithDefaultFile() throws Exception {
		testTransformationWithFile(STANDARD_FILE);
	}

	@Test
	public void testTransformationWithConflictFile() throws Exception {
		testTransformationWithFile(CONFLICT_FILE);
	}

	@Test
	public void testTransformationWithExistingLoggerFile() throws Exception {
		testTransformationWithFile(EXISTING_LOGGER_FILE);
	}

	@Test
	public void testTransformationWithExistingNonStaticLoggerFile() throws Exception {
		testTransformationWithFile(EXISTING_NONSTATIC_LOGGER_FILE);
	}

	@Test
	public void testTransformationWithExistingNoModifiersLoggerFile() throws Exception {
		testTransformationWithFile(EXISTING_NOMODIFIERS_LOGGER_FILE);
	}

	@Test
	public void calculateEnabledforProjectWithoutLoggerLibsShouldBeDisabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}

	@Test
	public void calculateEnabledForProjectShouldBeEnabled() throws Exception {
		RulesTestUtil.addToClasspath(testProject, Arrays.asList(
				RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.logging.log4j", "log4j-api", "2.7")));

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	private void testTransformationWithFile(String file) throws Exception {
		Path preRule = getPreRuleFile(file);
		Path postRule = getPostRuleFile(file, POSTRULE_SUBDIRECTORY);

		RulesTestUtil.addSourceContainer(testProject, "/allRulesTestRoot");
		RulesTestUtil.addToClasspath(testProject,
				Arrays.asList(RulesTestUtil.generateMavenEntryFromDepedencyString("org.slf4j", "slf4j-api", "1.7.25")));
		rule.calculateEnabledForProject(testProject);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
}
