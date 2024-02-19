package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.common.util.RulesTestUtil.addToClasspath;
import static eu.jsparrow.common.util.RulesTestUtil.generateMavenEntryFromDepedencyString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

class ReplaceJUnit3TestCasesRuleTest extends SingleRuleTest {

	private static final String REPLACE_WITH_J_UNIT_4 = "ReplaceJUnit3TestCasesWithJUnit4Rule.java";
	private static final String REPLACE_WITH_JUPITER = "ReplaceJUnit3TestCasesWithJupiterRule.java";
	private static final String USING_J_UNIT_3_TEST_RESULT_GETTER = "ReplaceJUnit3TestCasesUsingJUnit3TestResultGetterRule.java";
	private static final String USING_J_UNIT_3_TEST_RESULT_FIELD = "ReplaceJUnit3TestCasesUsingJUnit3TestResultFieldRule.java";
	private static final String IMPORT_OF_NOT_SUPPORTED_CONSTANT = "ReplaceJUnit3TestCasesImportOfNotSupportedConstantRule.java";
	private static final String QUALIFIED_NAME_OF_NOT_SUPPORTED_CONSTANT = "ReplaceJUnit3TestCasesQualifiedNameOfNotSupportedConstantRule.java";
	private static final String IMPORT_OF_NOT_SUPPORTED_STATIC_METHOD = "ReplaceJUnit3TestCasesImportOfNotSupportedStaticMethodRule.java";
	private static final String MAIN_METHOD_NOT_REMOVED = "ReplaceJUnit3TestCasesMainMethodNotRemovedRule.java";
	private static final String AMBIGUOUS_SUPER_METHOD_RETURN_TYPE = "ReplaceJUnit3TestCasesAmbiguousSuperMethodReturnTypeRule.java";
	private static final String REMOVE_MAIN_WITH_RUN_TEST = "ReplaceJUnit3TestCasesRemoveMainWithRunTestRule.java";
	private static final String REMOVE_RUN_TEST_IN_MAIN = "ReplaceJUnit3TestCasesRemoveRunTestInMainRule.java";
	private static final String RUN_UNEXPECTED_TEST_IN_MAIN = "ReplaceJUnit3TestCasesRunUnexpectedTestInMainRule.java";
	private static final String MAIN_METHOD_OF_TESTCASE_NOT_CHANGED = "ReplaceJUnit3TestCasesMainMethodOfTestCaseNotChangedRule.java";
	private static final String TEST_RUNNER_RUN_WITH_CLASS_VARIABLE = "ReplaceJUnit3TestCasesTestRunnerRunWithClassVariableRule.java";

	private static final String POSTRULE_SUBDIRECTORY = "migrateJUnit3";

	private ReplaceJUnit3TestCasesRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new ReplaceJUnit3TestCasesRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("ReplaceJUnit3TestCases"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Replace JUnit 3 Test Cases", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_5, Tag.TESTING, Tag.JUNIT), description.getTags());
		assertEquals(15, description.getRemediationCost().toMinutes());
		assertThat(description.getDescription(), equalTo(
				"This rule migrates JUnit 3 tests to either JUnit JUpiter or JUnit 4 depending on the most up-to-date JUnit version available in the classpath."));
	}

	@Test
	void test_requiredLibraries() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertThat(rule.requiredLibraries(), equalTo("JUnit 4 or JUnit 5"));
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertThat(rule.getRequiredJavaVersion(), equalTo("1.5"));
	}

	@Test
	void calculateEnabledForProject_supportLibraryVersion_4_12_shouldAllReturnTrue() throws Exception {
		addToClasspath(testProject, Arrays.asList(generateMavenEntryFromDepedencyString("junit", "junit", "4.12")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
		assertTrue(rule.isSatisfiedLibraries());
		assertTrue(rule.isSatisfiedJavaVersion());
	}

	@Test
	void calculateEnabledForProject_supportLibraryVersion_Jupiter_shouldAllReturnTrue() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
		assertTrue(rule.isSatisfiedLibraries());
		assertTrue(rule.isSatisfiedJavaVersion());
	}

	@Test
	void calculateEnabledForProject_supportJunitJupiter_5_0_shouldReturnTrue() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());
	}

	@Test
	void testTransformationToJUnit4() throws Exception {
		loadUtilities();

		addToClasspath(testProject, Arrays.asList(generateMavenEntryFromDepedencyString("junit", "junit", "4.12")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());

		Path preRule = getPreRuleFile(REPLACE_WITH_J_UNIT_4);
		Path postRule = getPostRuleFile(REPLACE_WITH_J_UNIT_4, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);

	}

	@Test
	void testTransformationToJupiter() throws Exception {
		loadUtilities();
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.0.0")));

		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());

		Path preRule = getPreRuleFile(REPLACE_WITH_JUPITER);
		Path postRule = getPostRuleFile(REPLACE_WITH_JUPITER, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@ParameterizedTest
	@ValueSource(strings = { USING_J_UNIT_3_TEST_RESULT_GETTER, USING_J_UNIT_3_TEST_RESULT_FIELD })
	void testUsingImplicitJUnit3TestResult(String preRuleFileName) throws Exception {
		loadUtilities();
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());

		Path preRule = getPreRuleFile(preRuleFileName);
		Path postRule = getPostRuleFile(preRuleFileName, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@ParameterizedTest
	@ValueSource(strings = { IMPORT_OF_NOT_SUPPORTED_CONSTANT, QUALIFIED_NAME_OF_NOT_SUPPORTED_CONSTANT,
			IMPORT_OF_NOT_SUPPORTED_STATIC_METHOD })
	void testUsingNotSupportedStaticMembers(String preRuleFileName) throws Exception {
		loadUtilities();
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());

		Path preRule = getPreRuleFile(preRuleFileName);
		Path postRule = getPostRuleFile(preRuleFileName, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	void testAmbiguousSuperMethodReturnType() throws Exception {
		loadUtilities();
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());

		Path preRule = getPreRuleFile(AMBIGUOUS_SUPER_METHOD_RETURN_TYPE);
		Path postRule = getPostRuleFile(AMBIGUOUS_SUPER_METHOD_RETURN_TYPE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	void testMainMethodNotRemoved() throws Exception {
		loadUtilities();
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());

		Path preRule = getPreRuleFile(MAIN_METHOD_NOT_REMOVED);
		Path postRule = getPostRuleFile(MAIN_METHOD_NOT_REMOVED, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	void testRemoveMainWithRunTest() throws Exception {
		loadUtilities();
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());

		Path preRule = getPreRuleFile(REMOVE_MAIN_WITH_RUN_TEST);
		Path postRule = getPostRuleFile(REMOVE_MAIN_WITH_RUN_TEST, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	void testRemoveRunTestInMain() throws Exception {
		loadUtilities();
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());

		Path preRule = getPreRuleFile(REMOVE_RUN_TEST_IN_MAIN);
		Path postRule = getPostRuleFile(REMOVE_RUN_TEST_IN_MAIN, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	void testRunUnexpectedTestInMain() throws Exception {
		loadUtilities();
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());

		Path preRule = getPreRuleFile(RUN_UNEXPECTED_TEST_IN_MAIN);
		Path postRule = getPostRuleFile(RUN_UNEXPECTED_TEST_IN_MAIN, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	void testMainMethodOfTestCaseNotChanged() throws Exception {
		loadUtilities();
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());

		Path preRule = getPreRuleFile(MAIN_METHOD_OF_TESTCASE_NOT_CHANGED);
		Path postRule = getPostRuleFile(MAIN_METHOD_OF_TESTCASE_NOT_CHANGED, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	void testTestRunnerRunWithClassVariable() throws Exception {
		loadUtilities();
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.junit.jupiter", "junit-jupiter-api", "5.0.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());

		Path preRule = getPreRuleFile(TEST_RUNNER_RUN_WITH_CLASS_VARIABLE);
		Path postRule = getPostRuleFile(TEST_RUNNER_RUN_WITH_CLASS_VARIABLE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
}