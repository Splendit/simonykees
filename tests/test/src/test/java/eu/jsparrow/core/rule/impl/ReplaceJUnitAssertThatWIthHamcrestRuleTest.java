package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.core.util.RulesTestUtil.addToClasspath;
import static eu.jsparrow.core.util.RulesTestUtil.createJavaProject;
import static eu.jsparrow.core.util.RulesTestUtil.generateMavenEntryFromDepedencyString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.SingleRuleTest;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class ReplaceJUnitAssertThatWIthHamcrestRuleTest extends SingleRuleTest {

	private static final String STANDARD_FILE = "ReplaceJUnitAssertThatWithHamcrestRule.java";
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
		assertThat(ruleId, equalTo("ReplaceJunitAssertThatWIthHamcrest"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getName(), equalTo("Replace JUnit Timeout Annotation Property with assertTimeout"));
		assertThat(description.getTags(),
				contains(Tag.JAVA_1_8, Tag.TESTING));
		assertThat(description.getRemediationCost(), equalTo(Duration.ofMinutes(5)));
		assertThat(description.getDescription(),
				equalTo("JUnit Jupiter API provides timeout assertions, i.e., assertions that execution of some code completes before a timeout exceeds. In JUnit 4 this is achieved by using the 'timeout' property of '@Test(timeout=...)' annotation. \nThis rule removes the 'timeout' annotation property and inserts an  'assertTimeout' instead."));
	}

	@Test
	void test_requiredLibraries() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-all",
					"1.3")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertThat(rule.requiredLibraries(), equalTo("Hamcrest"));
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		addToClasspath(testProject, Arrays
				.asList(generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-all",
						"1.3")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertThat(rule.getRequiredJavaVersion(), equalTo("1.8"));
	}

	@Test
	void calculateEnabledForProject_supportLibraryVersion_4_13_shouldReturnTrue_shouldReturnTrue() throws Exception {
		addToClasspath(testProject, Arrays
				.asList(generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-all",
						"1.3")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
		assertFalse(rule.isSatisfiedLibraries());
		assertTrue(rule.isSatisfiedJavaVersion());
	}

	@Test
	void testTransformationWithJupiter() throws Exception {
		root = RulesTestUtil.addSourceContainer(testProject, "/allRulesTestRoot");

		RulesTestUtil.addToClasspath(testProject, Arrays.asList(
				RulesTestUtil.generateMavenEntryFromDepedencyString("junit", "junit", "4.13"),
				RulesTestUtil.generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-library","1.3"), 
				RulesTestUtil.generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-core","1.3")));
		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(STANDARD_FILE);
		Path postRule = getPostRuleFile(STANDARD_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule),
				getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
	
	
}
