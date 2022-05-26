package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.common.util.RulesTestUtil.addToClasspath;
import static eu.jsparrow.common.util.RulesTestUtil.generateMavenEntryFromDepedencyString;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class ReplaceRequestMappingAnnotationRuleTest extends SingleRuleTest {

	private static final String SAMPLE_FILE = "TestReplaceRequestMappingAnnotationRule.java";

	private static final String POSTRULE_SUBDIRECTORY = "requestmapping";

	private ReplaceRequestMappingAnnotationRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new ReplaceRequestMappingAnnotationRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void calculateEnabledForProjectShouldBeEnabled() throws Exception {
		addToClasspath(testProject,
				asList(generateMavenEntryFromDepedencyString("org.springframework", "spring-web", "5.3.19")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@Test
	void calculateEnabledForProjectShouldBeDisabledForJava4() throws Exception {
		addToClasspath(testProject,
				asList(generateMavenEntryFromDepedencyString("org.springframework", "spring-web", "5.3.19")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}

	@Test
	void calculateEnabledForProjectShouldBeDisabledForJava5() throws Exception {
		addToClasspath(testProject,
				asList(generateMavenEntryFromDepedencyString("org.springframework", "spring-web", "4.2.9.RELEASE")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("ReplaceRequestMappingAnnotation"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getName(), equalTo("Replace Request Mapping Annotation"));
		assertThat(description.getTags(),
				contains(Tag.JAVA_1_5, Tag.CODING_CONVENTIONS, Tag.READABILITY));
		assertThat(description.getRemediationCost(), equalTo(Duration.ofMinutes(2)));
		assertThat(description.getDescription(),
				equalTo("Version 4.3 of the Spring Framework introduced annotations like '@GetMapping' or '@PostMapping'"
						+ " to be used instead of '@RequestMapping' when a certain request method is specified."
						+ " Accordingly, this rule tries to replace annotations."
						+ " For example,  '@RequestMapping(value = \"/hello\", method = RequestMethod.GET)'"
						+ " can be replaced by '@GetMapping(value = \"/hello\")'."));
	}

	@Test
	void testTransformation() throws Exception {

		String preRuleFileName = SAMPLE_FILE;

		RulesTestUtil.addToClasspath(testProject, Arrays.asList(
				RulesTestUtil.generateMavenEntryFromDepedencyString("org.springframework", "spring-web", "5.3.19")));
		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(preRuleFileName);
		Path postRule = getPostRuleFile(preRuleFileName, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
}
