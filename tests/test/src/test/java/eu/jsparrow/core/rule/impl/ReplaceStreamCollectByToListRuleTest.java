package eu.jsparrow.core.rule.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.SingleRuleTest;
import eu.jsparrow.core.util.RulesTestUtil;
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
		assertThat(description.getName(), equalTo("Replace Stream.collect() by Stream.toList()"));
		assertThat(description.getTags(),
				contains(Tag.JAVA_16, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY));
		assertThat(description.getRemediationCost(), equalTo(Duration.ofMinutes(2)));
		assertThat(description.getDescription(),
				equalTo("Java 16 introduced Stream.toList() for simplifying the conversion of a stream into a list." +
						" For example, stream.collect(Collectors.toUnmodifiableList()) can be transformed to stream.toList()."));
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