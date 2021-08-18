package eu.jsparrow.core.rule.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.SingleRuleTest;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.java16.UsePatternMatchingForInstanceofRule;

class UsePatternMatchingForInstanceofRuleTest extends SingleRuleTest {

	private UsePatternMatchingForInstanceofRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new UsePatternMatchingForInstanceofRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("UsePatternMatchingForInstanceof"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getName(), equalTo("Use Pattern Matching for Instanceof"));
		assertThat(description.getTags(),
				contains(Tag.JAVA_16, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY));
		assertThat(description.getRemediationCost(), equalTo(Duration.ofMinutes(5)));
		assertThat(description.getDescription(),
				equalTo("This rule replaces instanceof expressions by Pattern Matching for Instanceof expressions that are introduced in Java 16. For example, a piece of code like \"if(o instanceof String)\" can be transformed to \"if(o instanceof String value)\"."));
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		assertThat(rule.getRequiredJavaVersion(), equalTo("16"));
	}
}