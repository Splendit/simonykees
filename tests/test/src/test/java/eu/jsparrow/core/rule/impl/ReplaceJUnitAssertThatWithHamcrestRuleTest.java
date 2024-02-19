package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.common.util.RulesTestUtil.addToClasspath;
import static eu.jsparrow.common.util.RulesTestUtil.createJavaProject;
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

class ReplaceJUnitAssertThatWithHamcrestRuleTest extends SingleRuleTest {

	private static final String STANDARD_FILE = "ReplaceJUnitAssertThatWithHamcrestRule.java";
	private static final String ON_DEMAND_IMPORTS = "ReplaceJUnitAssertThatWithHamcrestOnDemandImportsRule.java";
	private static final String DUPLICATE_IMPORTS = "ReplaceJUnitAssertThatWithHamcrestDuplicateImportRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "assertThat";

	private ReplaceJUnitAssertThatWithHamcrestRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new ReplaceJUnitAssertThatWithHamcrestRule();
		testProject = createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("ReplaceJUnitAssertThatWithHamcrest"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Replace JUnit assertThat with Hamcrest", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_5, Tag.TESTING, Tag.JUNIT), description.getTags());
		assertEquals(2, description.getRemediationCost()
			.toMinutes());
		assertEquals(""
				+ "The JUnit Assert.assertThat method is deprecated."
				+ " Its sole purpose is to forward the call to the MatcherAssert.assertThat method defined in Hamcrest 1.3."
				+ " Therefore, it is recommended to directly use the equivalent assertion defined in the third party Hamcrest library.",
				description.getDescription());
	}

	@Test
	void test_requiredLibraries() throws Exception {
		addToClasspath(testProject,
				Arrays.asList(generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-library", "1.3"),
						generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-core", "1.3")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);

		rule.calculateEnabledForProject(testProject);

		assertThat(rule.requiredLibraries(), equalTo("Hamcrest 1.3 or later"));
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		addToClasspath(testProject,
				Arrays.asList(generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-library", "1.3"),
						generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-core", "1.3")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertThat(rule.getRequiredJavaVersion(), equalTo("1.5"));
	}

	@Test
	void calculateEnabledForProject_supportHamcrestVersion_1_3_shouldReturnTrue() throws Exception {
		addToClasspath(testProject,
				Arrays.asList(generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-library", "1.3"),
						generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-core", "1.3")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
		assertTrue(rule.isSatisfiedLibraries());
		assertTrue(rule.isSatisfiedJavaVersion());
	}

	@ParameterizedTest
	@ValueSource(strings = { STANDARD_FILE, ON_DEMAND_IMPORTS, DUPLICATE_IMPORTS })
	void testTransformationWithDefaultFile(String preRuleFileName) throws Exception {
		root = RulesTestUtil.addSourceContainer(testProject, "/allRulesTestRoot");
		loadUtilities();
		RulesTestUtil.addToClasspath(testProject,
				Arrays.asList(generateMavenEntryFromDepedencyString("junit", "junit", "4.13"),
						generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-library", "1.3"),
						generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-core", "1.3")));
		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(preRuleFileName);
		Path postRule = getPostRuleFile(preRuleFileName, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
}
