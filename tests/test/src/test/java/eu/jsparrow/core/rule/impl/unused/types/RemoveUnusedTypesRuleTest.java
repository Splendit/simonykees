package eu.jsparrow.core.rule.impl.unused.types;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.core.rule.impl.unused.RemoveUnusedTypesRule;
import eu.jsparrow.core.rule.impl.unused.UnusedCodeTestHelper;
import eu.jsparrow.core.visitor.unused.type.UnusedTypeWrapper;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class RemoveUnusedTypesRuleTest extends SingleRuleTest {

	private static final String PRERULE_UNUSED_PACKAGE = "eu.jsparrow.sample.preRule.unused.types";
	private static final String PRERULE_DIRECTORY = RulesTestUtil.PRERULE_DIRECTORY + "/unused/types";

	private RemoveUnusedTypesRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new RemoveUnusedTypesRule(Collections.emptyList());
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("RemoveUnusedTypes"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getName(), equalTo("Remove Unused Types"));
		assertThat(description.getTags(),
				contains(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS));
		assertThat(description.getRemediationCost(), equalTo(Duration.ofMinutes(2)));
		assertThat(description.getDescription(),
				equalTo("Finds and removes types that are not used."));
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

	@ParameterizedTest
	@ValueSource(strings = {
			"HelloWorld"
	})
	void testTransformation(String className) throws Exception {
		String preRuleFilePath = String.format("unused/types/%s.java", className);
		Path preRule = getPreRuleFile(preRuleFilePath);
		Path postRule = getPostRuleFile(className + ".java", "unused/types");

		List<UnusedTypeWrapper> unusedTypes = UnusedCodeTestHelper.findTypesToBeRemoved(PRERULE_UNUSED_PACKAGE,
				PRERULE_DIRECTORY);
		RemoveUnusedTypesRule rule = new RemoveUnusedTypesRule(unusedTypes);

		String refactoring = UnusedCodeTestHelper.applyRemoveUnusedCodeRefactoring(rule,
				"eu.jsparrow.sample.preRule.unused.types", preRule, root);
		String postRulePackage = getPostRulePackage("unused.types");
		String actual = StringUtils.replace(refactoring, "package eu.jsparrow.sample.preRule.unused.types",
				postRulePackage);
		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);

		assertEquals(expected, actual);
	}

}
