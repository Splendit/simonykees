package eu.jsparrow.core.rule.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.time.Duration;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class ReplaceSetRemoveAllWithForEachRuleTest extends SingleRuleTest {

	private ReplaceSetRemoveAllWithForEachRule rule;

	@BeforeEach
	void setUp() throws Exception {
		rule = new ReplaceSetRemoveAllWithForEachRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo( "ReplaceSetRemoveAllWithForEach"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getName(), equalTo("Replace Set.removeAll With ForEach"));
		assertThat(description.getTags(),
				contains(Tag.JAVA_1_8, Tag.PERFORMANCE));
		assertThat(description.getRemediationCost(), equalTo(Duration.ofMinutes(5)));
		assertThat(description.getDescription(),
				equalTo("Using the method 'removeAll(Collection)' in order to removing elements from a Set may lead to performance problems because of a possible O(n^2) complexity."
						+ " This rule replaces 'removeAll' invocations by corresponding 'forEach' constructs."
						+ " For example 'mySet.removeAll(myList);' is replaced by 'myList.forEach(mySet::remove);'"));
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		assertThat(rule.getRequiredJavaVersion(), equalTo("1.8"));
	}

	@Test
	void calculateEnabledForProject_shouldReturnFalse() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
		rule.calculateEnabledForProject(testProject);
		assertFalse(rule.isEnabled());
	}

	@Test
	void calculateEnabledForProject_shouldReturnTrue() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());
	}
}