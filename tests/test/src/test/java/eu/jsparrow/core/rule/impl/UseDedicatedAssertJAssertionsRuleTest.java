package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.common.util.RulesTestUtil.addToClasspath;
import static eu.jsparrow.common.util.RulesTestUtil.createJavaProject;
import static eu.jsparrow.common.util.RulesTestUtil.generateMavenEntryFromDepedencyString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class UseDedicatedAssertJAssertionsRuleTest extends SingleRuleTest {

	private UseDedicatedAssertJAssertionsRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new UseDedicatedAssertJAssertionsRule();
		testProject = createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("UseDedicatedAssertJAssertions"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		String ruleDescription = "AssertJ contains a rich API for writing specific assertions about different types of objects. "
				+ "Making use of the appropriate dedicated methods when writing certain assertions will simplify the test code and "
				+ "improve the corresponding failure messages. This rule finds AssertJ assertions that can be simplified and replaces "
				+ "them with equivalent dedicated assertions.";
		assertThat(description.getName(), equalTo("Use Dedicated AssertJ Assertions"));
		assertEquals(Arrays.asList(Tag.JAVA_1_8, Tag.TESTING, Tag.ASSERTJ, Tag.CODING_CONVENTIONS, Tag.READABILITY), description.getTags());
		assertThat(description.getRemediationCost(), equalTo(Duration.ofMinutes(5)));
		assertThat(description.getDescription(),
				equalTo(ruleDescription));
	}

	@Test
	void test_requiredLibraries() throws Exception {
		assertThat(rule.requiredLibraries(), equalTo("AssertJ [3.20.2, 3.22.x]"));
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		assertThat(rule.getRequiredJavaVersion(), equalTo("1.8"));
	}

	@Test
	void calculateEnabledForProject_supportLibraryVersion_3_21_0_shouldAllReturnTrue() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.assertj", "assertj-core", "3.21.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());
		assertTrue(rule.isSatisfiedLibraries());
		assertTrue(rule.isSatisfiedJavaVersion());
	}

	@Test
	void calculateEnabledForProject_supportLibraryVersion_3_22_0_shouldAllReturnTrue() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.assertj", "assertj-core", "3.22.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());
		assertTrue(rule.isSatisfiedLibraries());
		assertTrue(rule.isSatisfiedJavaVersion());
	}

	@Test
	void calculateEnabledForProject_notSupportedLibraryVersion_3_20_1_shouldReturnFalse() throws Exception {
		addToClasspath(testProject, Arrays
			.asList(generateMavenEntryFromDepedencyString("org.assertj", "assertj-core", "3.20.1")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);
		assertFalse(rule.isEnabled());
	}
}
