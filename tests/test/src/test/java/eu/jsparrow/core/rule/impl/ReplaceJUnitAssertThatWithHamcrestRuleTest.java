package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.core.util.RulesTestUtil.addToClasspath;
import static eu.jsparrow.core.util.RulesTestUtil.createJavaProject;
import static eu.jsparrow.core.util.RulesTestUtil.generateMavenEntryFromDepedencyString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
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

public class ReplaceJUnitAssertThatWithHamcrestRuleTest extends SingleRuleTest {
	
	private static final String STANDARD_FILE = "ReplaceJUnitAssertThatWithHamcrestRule.java";
	private static final String ON_DEMAND_IMPORTS = "ReplaceJUnitAssertThatWithHamcrestOnDemandImportsRule.java";
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
		assertThat(ruleId, equalTo("ReplaceJUnitAssertThatWIthHamcrest"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getName(), equalTo("Replace JUnit assertThat with Hamcrest"));
		assertThat(description.getTags(),
				contains(Tag.JAVA_1_5, Tag.TESTING));
		assertThat(description.getRemediationCost(), equalTo(Duration.ofMinutes(2)));
		assertThat(description.getDescription(),
				equalTo("JUnit Assert.assertThat is deprecated. The recommended alternative is to use the equivalent assertion in the Hamcrest library."));
	}

	@Test
	void test_requiredLibraries() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-library", "1.3"),
					generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-core", "1.3")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);

		rule.calculateEnabledForProject(testProject);

		assertThat(rule.requiredLibraries(), equalTo("Hamcrest 1.3 or later"));
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-library", "1.3"),
					generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-core", "1.3")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertThat(rule.getRequiredJavaVersion(), equalTo("1.5"));
	}

	@Test
	void calculateEnabledForProject_supportHamcrestVersion_1_3_shouldReturnTrue() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-library", "1.3"),
					generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-core", "1.3")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
		assertTrue(rule.isSatisfiedLibraries());
		assertTrue(rule.isSatisfiedJavaVersion());
	}

	@Test
	void testTransformationWithDefaultFile() throws Exception {
		root = RulesTestUtil.addSourceContainer(testProject, "/allRulesTestRoot");

		RulesTestUtil.addToClasspath(testProject, Arrays.asList(
				generateMavenEntryFromDepedencyString("junit", "junit", "4.13"),
				generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-library", "1.3"),
				generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-core", "1.3")));
		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(STANDARD_FILE);
		Path postRule = getPostRuleFile(STANDARD_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule),
				getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
	
	@Test
	void testTransformationWithOnDemandImportsFile() throws Exception {
		root = RulesTestUtil.addSourceContainer(testProject, "/allRulesTestRoot");
		loadUtilities();
		RulesTestUtil.addToClasspath(testProject, Arrays.asList(
				generateMavenEntryFromDepedencyString("junit", "junit", "4.13"),
				generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-library", "1.3"),
				generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-core", "1.3")));

		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(ON_DEMAND_IMPORTS);
		Path postRule = getPostRuleFile(ON_DEMAND_IMPORTS, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule),
				getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
}
