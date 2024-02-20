package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.common.util.RulesTestUtil.createJavaProject;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class ReplaceWrongClassForLoggerRuleTest extends SingleRuleTest {

	private static final String SAMPLE_FILE_JAVA_LOGGING = "TestReplaceWrongClassForLoggerJavaLoggingRule.java";
	private static final String SAMPLE_FILE_SLF4J_LOGGER = "TestReplaceWrongClassForLoggerSlf4jRule.java";
	private static final String SAMPLE_FILE_APACHE_LOG4J = "TestReplaceWrongClassForLoggerApacheLog4jRule.java";
	private static final String SAMPLE_FILE_APACHE_LOGGING_LOG4J = "TestReplaceWrongClassForLoggerApacheLoggingLog4jRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "loggingWithForeignClass";
	private ReplaceWrongClassForLoggerRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new ReplaceWrongClassForLoggerRule();
		testProject = createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		assertEquals("ReplaceWrongClassForLogger", rule.getId());
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Replace Wrong Class for Logger", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.LOGGING), description.getTags());
		assertEquals(5, description.getRemediationCost()
			.toMinutes());
		assertEquals(""
				+ "If a given logger is initialized with a class which is different from the class where it is declared,"
				+ " then this rule will replace the wrong initialization argument by the correct one. For example,"
				+ " if a logger for the class 'Employee' is initialized with 'User.class',"
				+ " then the argument of the initialization will be replaced by 'Employee.class'.", //
				description.getDescription());
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertEquals("1.1", rule.getRequiredJavaVersion());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			SAMPLE_FILE_JAVA_LOGGING,
			SAMPLE_FILE_SLF4J_LOGGER,
			SAMPLE_FILE_APACHE_LOG4J,
			SAMPLE_FILE_APACHE_LOGGING_LOG4J
	})
	void testTransformationWithDefaultFile(String fileName) throws Exception {
		loadUtilities();

		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(fileName);
		Path postRule = getPostRuleFile(fileName, POSTRULE_SUBDIRECTORY);
		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));
		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
}
