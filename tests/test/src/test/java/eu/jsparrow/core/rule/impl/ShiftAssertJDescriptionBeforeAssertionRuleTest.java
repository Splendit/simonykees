package eu.jsparrow.core.rule.impl;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static eu.jsparrow.common.util.RulesTestUtil.*;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.SingleRuleTest;

import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag; 

class ShiftAssertJDescriptionBeforeAssertionRuleTest extends SingleRuleTest {

	private ShiftAssertJDescriptionBeforeAssertionRule rule;
	
	@BeforeEach
	void setUp() throws Exception {
		rule = new ShiftAssertJDescriptionBeforeAssertionRule();
		testProject = createJavaProject("javaVersionTestProject", "bin");
	}
	
	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("ShiftAssertJDescriptionBeforeAssertion"));
	}
	
	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getName(), equalTo("Shift AssertJ Description Before Assertion"));
		assertThat(description.getTags(),
				contains(Tag.JAVA_1_5, Tag.TESTING, Tag.ASSERTJ, Tag.CODING_CONVENTIONS));
		assertThat(description.getRemediationCost(), equalTo(Duration.ofMinutes(5)));
		assertThat(description.getDescription(),
				equalTo("AssertJ provides methods for setting descriptions or error messages of assertions, e.g.: as, describedAs, withFailMessage, overridingErrorMessage. "
						+ "\nThese methods should always be invoked before the actual assertion they intend to describe, otherwise, they have no effect. "
						+ "\nThis rule, swaps the invocation of the assertion methods with the invocation of the methods setting descriptions or the error "
						+ "messages for the corresponding assertions."));
	}
	
	@Test
	void test_requiredLibraries() throws Exception {

		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertThat(rule.requiredLibraries(), equalTo("AssertJ"));
	}
	
	@Test
	void test_requiredJavaVersion() throws Exception {
		assertThat(rule.getRequiredJavaVersion(), equalTo("1.5"));
	}

	@Test
	void calculateEnabledForProject_ShouldBeDisabled() throws Exception {
		addToClasspath(testProject, Arrays
				.asList(generateMavenEntryFromDepedencyString("org.assertj", "assertj-core", "3.21.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}

	@Test
	void calculateEnabledForProject_ShouldBeEnabled() throws Exception {
		addToClasspath(testProject, Arrays
				.asList(generateMavenEntryFromDepedencyString("org.assertj", "assertj-core", "3.21.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@Test
	void calculateEnabledForProject_missingDependency_ShouldBeEnabled() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}
}
