package eu.jsparrow.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.core.util.RulesTestUtil;

@SuppressWarnings("nls")
public class StandardLoggerRuleSlf4jTest extends SingleRuleTest {

	private static final String STANDARD_FILE = "TestStandardLoggerRule.java";
	private static final String CONFLICT_FILE = "TestStandardLoggerConflictRule.java";
	private static final String EXISTING_LOGGER_FILE = "TestStandardLoggerExistingSlf4jLogger.java";
	private static final String POSTRULE_SUBDIRECTORY = "standardLoggerSlf4j";

	private StandardLoggerRule rule;

	@Before
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
		Path preRule = getPreRuleFile(STANDARD_FILE);
		Path postRule = getPostRuleFile(STANDARD_FILE, POSTRULE_SUBDIRECTORY);

		RulesTestUtil.addToClasspath(testProject,
				Arrays.asList(RulesTestUtil.generateMavenEntryFromDepedencyString("org.slf4j", "slf4j-api", "1.7.25")));
		RulesTestUtil.addToClasspath(testProject, RulesTestUtil.getClassPathEntries(root));
		rule.calculateEnabledForProject(testProject);

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
	public void testTransformationWithExistingLoggerFile() throws Exception {
		Path preRule = getPreRuleFile(EXISTING_LOGGER_FILE);
		Path postRule = getPostRuleFile(EXISTING_LOGGER_FILE, POSTRULE_SUBDIRECTORY);

		RulesTestUtil.addToClasspath(testProject,
				Arrays.asList(RulesTestUtil.generateMavenEntryFromDepedencyString("org.slf4j", "slf4j-api", "1.7.25")));
		RulesTestUtil.addToClasspath(testProject, RulesTestUtil.getClassPathEntries(root));
		rule.calculateEnabledForProject(testProject);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
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

}
