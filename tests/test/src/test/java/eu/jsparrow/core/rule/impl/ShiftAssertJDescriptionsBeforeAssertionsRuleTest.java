package eu.jsparrow.core.rule.impl;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag; 

class ShiftAssertJDescriptionsBeforeAssertionsRuleTest extends SingleRuleTest {

	private ShiftAssertJDescriptionsBeforeAssertionsRule rule;
	
	@BeforeEach
	void setUp() throws Exception {
		rule = new ShiftAssertJDescriptionsBeforeAssertionsRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}
	
	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("ShiftAssertJDescriptionsBeforeAssertions"));
	}
	
	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getName(), equalTo("Shift AssertJ Descriptions Before Assertions"));
		assertThat(description.getTags(),
				contains(Tag.JAVA_1_5, Tag.TESTING, Tag.ASSERTJ, Tag.CODING_CONVENTIONS));
		assertThat(description.getRemediationCost(), equalTo(Duration.ofMinutes(5)));
		assertThat(description.getDescription(),
				equalTo("AssertJ Description only make senese to be invoked before the assertion itself. Otherwise it has no effect."));
	}
	
	@Test
	void test_requiredLibraries() throws Exception {

		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertThat(rule.requiredLibraries(), isEmptyOrNullString());
	}
	
	@Test
	void test_requiredJavaVersion() throws Exception {
		assertThat(rule.getRequiredJavaVersion(), equalTo("1.5"));
	}

	@Test
	void calculateEnabledForProjectShouldBeDisabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}

	@Test
	void calculateEnabledForProjectShouldBeEnabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

}
