package eu.jsparrow.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;

public class StandardLoggerCustomOptionsRuleTest extends SingleRuleTest {

	private static final String SAMPLE_FILE = "TestStandardLoggerCustomOptionsRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "standardLoggerCustomOptions";

	private StandardLoggerRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new StandardLoggerRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		testProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		testProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
	}

	@Test
	public void testTransformationWithDefaultFile() throws Exception {
		root = RulesTestUtil.addSourceContainer(testProject, "/allRulesTestRoot");

		RulesTestUtil.addToClasspath(testProject,
				Arrays.asList(RulesTestUtil.generateMavenEntryFromDepedencyString("org.slf4j", "slf4j-api", "1.7.25")));
		Map<String, String> selectedOptions = new HashMap<>();
		/*
		 * Leave as is
		 */
		selectedOptions.put(StandardLoggerConstants.SYSTEM_OUT_PRINT_KEY, "");
		selectedOptions.put(StandardLoggerConstants.SYSTEM_ERR_PRINT_KEY, "debug");
		selectedOptions.put(StandardLoggerConstants.PRINT_STACKTRACE_KEY, "warn");
		rule.activateOptions(selectedOptions);

		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(SAMPLE_FILE);
		Path postRule = getPostRuleFile(SAMPLE_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void calculateEnabledforProjectShouldBeDisabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}
}
