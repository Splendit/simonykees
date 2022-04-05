package eu.jsparrow.core.rule.impl.unused;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.core.visitor.unused.UnusedFieldWrapper;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class RemoveUnusedFieldsRuleTest extends SingleRuleTest {

	private static final String PRERULE_UNUSED_PACKAGE = "eu.jsparrow.sample.preRule.unused";
	private static final String PRERULE_DIRECTORY = RulesTestUtil.PRERULE_DIRECTORY + "/unused";
	
	private RemoveUnusedFieldsRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new RemoveUnusedFieldsRule(Collections.emptyList());
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("RemoveUnusedFields"));
	}
	
	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getName(), equalTo("Remove Unused Fields"));
		assertThat(description.getTags(),
				contains(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS));
		assertThat(description.getRemediationCost(), equalTo(Duration.ofMinutes(2)));
		assertThat(description.getDescription(),
				equalTo("Finds and remove fields that are never used actively."));
	}
	
	@Test
	void test_requiredLibraries() throws Exception {

		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertThat(rule.requiredLibraries(), nullValue());
	}
	
	@Test
	void test_requiredJavaVersion() throws Exception {
		assertThat(rule.getRequiredJavaVersion(), equalTo("1.1"));
	}

	@Test
	void calculateEnabledForProject_ShouldBeEnabled() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}
	
	@Test
	void testTransformation() throws Exception {
		Path preRule = getPreRuleFile("unused/UnusedFields.java");
		Path postRule = getPostRuleFile("UnusedFields.java", "unused");
		
		List<UnusedFieldWrapper> unusedFields = UnusedCodeTestHelper.findFieldsToBeRemoved(PRERULE_UNUSED_PACKAGE, PRERULE_DIRECTORY);
		RemoveUnusedFieldsRule rule = new RemoveUnusedFieldsRule(unusedFields);
		
		String refactoring = UnusedCodeTestHelper.applyRemoveUnusedCodeRefactoring(rule, "eu.jsparrow.sample.preRule.unused", preRule, root);
		String postRulePackage = getPostRulePackage("unused");
		String actual = StringUtils.replace(refactoring, "package eu.jsparrow.sample.preRule.unused",
				postRulePackage);
		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		
		assertEquals(expected, actual);
	}
	

}
