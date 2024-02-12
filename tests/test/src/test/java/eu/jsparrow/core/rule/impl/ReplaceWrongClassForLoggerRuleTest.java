package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.common.util.RulesTestUtil.createJavaProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

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
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("ReplaceWrongClassForLogger"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getName(), equalTo("Replace Wrong Class for Logger"));
		assertThat(description.getTags(),
				contains(Tag.JAVA_1_1, Tag.READABILITY, Tag.LOGGING));
		assertThat(description.getRemediationCost(), equalTo(Duration.ofMinutes(5)));
		assertThat(description.getDescription(),
				equalTo("If a given logger is initialized with a class which is different from the class where it is declared, then this rule will replace the wrong initialization argument by the correct one. For example, if a logger for the class 'Employee' is initialized with 'User.class', then the argument of the initialization will be replaced by 'Employee.class'."));
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertThat(rule.getRequiredJavaVersion(), equalTo("1.1"));
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
