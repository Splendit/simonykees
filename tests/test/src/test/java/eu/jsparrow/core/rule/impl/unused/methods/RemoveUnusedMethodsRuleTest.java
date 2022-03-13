package eu.jsparrow.core.rule.impl.unused.methods;

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
import eu.jsparrow.core.rule.impl.unused.RemoveUnusedMethodsRule;
import eu.jsparrow.core.rule.impl.unused.UnusedFieldsTestHelper;
import eu.jsparrow.core.visitor.unused.method.UnusedMethodWrapper;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class RemoveUnusedMethodsRuleTest extends SingleRuleTest {
	
	private static final String PRERULE_UNUSED_PACKAGE = "eu.jsparrow.sample.preRule.unused.methods";
	private static final String PRERULE_DIRECTORY = RulesTestUtil.PRERULE_DIRECTORY + "/unused/methods";
	
	private RemoveUnusedMethodsRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new RemoveUnusedMethodsRule(Collections.emptyList());
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("RemoveUnusedMethods"));
	}
	
	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getName(), equalTo("Remove Unused Methods"));
		assertThat(description.getTags(),
				contains(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS));
		assertThat(description.getRemediationCost(), equalTo(Duration.ofMinutes(2)));
		assertThat(description.getDescription(),
				equalTo("Finds and removes methods that are never used actively."));
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
			"UnusedPublicMethods", 
			"UnusedProtectedMethods", 
			"UnusedPackagePrivateMethods", 
			"UnusedPrivateMethods",
			"Circle",
			"ColoredCircle",
			"Square",
			"ClassOne",
			"ParameterizedType",
			"ReferencedInTests",
			"EnumConstantAnonymousClasses",
			"AnonymousClass",
			"JUnit3Test"
			})
	void testTransformation(String className) throws Exception {
		String preRuleFilePath = String.format("unused/methods/%s.java", className);
		Path preRule = getPreRuleFile(preRuleFilePath);
		Path postRule = getPostRuleFile(className + ".java", "unused/methods");
		
		List<UnusedMethodWrapper> unusedFields = UnusedFieldsTestHelper.findMethodsToBeRemoved(PRERULE_UNUSED_PACKAGE, PRERULE_DIRECTORY);
		RemoveUnusedMethodsRule rule = new RemoveUnusedMethodsRule(unusedFields);
		
		String refactoring = UnusedFieldsTestHelper.applyRemoveUnusedCodeRefactoring(rule, "eu.jsparrow.sample.preRule.unused.methods", preRule, root);
		String postRulePackage = getPostRulePackage("unused.methods");
		String actual = StringUtils.replace(refactoring, "package eu.jsparrow.sample.preRule.unused.methods",
				postRulePackage); 
		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		
		assertEquals(expected, actual);
	}

}
