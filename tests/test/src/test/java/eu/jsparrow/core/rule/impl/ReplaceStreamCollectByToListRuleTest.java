package eu.jsparrow.core.rule.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class ReplaceStreamCollectByToListRuleTest extends SingleRuleTest {

	private ReplaceStreamCollectByToListRule rule;

	@BeforeEach
	void setUp() throws Exception {
		rule = new ReplaceStreamCollectByToListRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("ReplaceStreamCollectByToList"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Replace Stream.collect() by Stream.toList()", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_16, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY), description.getTags());
		assertEquals(2, description.getRemediationCost()
			.toMinutes());
		assertEquals(""
				+ "Java 16 introduced 'Stream.toList()' as a shorthand method for converting a Stream into an unmodifiable List. "
				+ "This rule replaces invocations of 'collect(Collectors.toUnmodifiableList())' by the new method 'toList()'. \n"
				+ "In case 'Collectors.toList()' is used as a collector, the rule makes additional verifications whether the generated "
				+ "list is modified by the context or not. In the latter case invocations of 'collect(Collectors.toList())' are also "
				+ "replaced by invocations of the simpler method 'toList()'.",
				description.getDescription());
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		assertThat(rule.getRequiredJavaVersion(), equalTo("16"));
	}

	@Test
	void calculateEnabledForProject_shouldReturnFalse() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_15);
		rule.calculateEnabledForProject(testProject);
		assertFalse(rule.isEnabled());
	}

	@Test
	void calculateEnabledForProject_shouldReturnTrue() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_16);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());
	}
}