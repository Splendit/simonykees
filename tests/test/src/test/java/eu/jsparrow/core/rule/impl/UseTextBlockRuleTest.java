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
import eu.jsparrow.rules.java16.UseTextBlockRule;

class UseTextBlockRuleTest extends SingleRuleTest {

	private UseTextBlockRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new UseTextBlockRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("UseTextBlock"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getName(), equalTo("Use Java 15 Text Block"));
		assertThat(description.getTags(),
				contains(Tag.JAVA_15, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY));
		assertThat(description.getRemediationCost(), equalTo(Duration.ofMinutes(5)));
		assertThat(description.getDescription(),
				equalTo("This rule replaces concatenation expressions made up of String literals by Text Block Sting literals which have been introduced in Java 15. Thus readability of String expressions is improved."));
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		assertThat(rule.getRequiredJavaVersion(), equalTo("15"));
	}
}