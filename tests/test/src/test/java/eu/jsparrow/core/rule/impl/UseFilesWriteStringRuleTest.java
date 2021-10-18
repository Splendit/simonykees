package eu.jsparrow.core.rule.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class UseFilesWriteStringRuleTest {

	private UseFilesWriteStringRule rule;

	private IJavaProject testProject;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new UseFilesWriteStringRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	public void calculateEnabledForProjectShouldBeDisabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_10);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}

	@Test
	public void calculateEnabledForProjectShouldBeEnabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_11);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@Test
	public void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("UseFilesWriteString"));
	}

	@Test
	public void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getTags(),
				contains(Tag.JAVA_11, Tag.PERFORMANCE, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.IO_OPERATIONS));
		assertThat(description.getTags(), hasSize(4));
		assertThat(description.getName(), equalTo("Use Files.writeString"));
		assertThat(description.getDescription(), equalTo(
				"Java 11 introduced 'Files.writeString(Path, CharSequence, Charset, OpenOption...)' and "
						+ "'Files.writeString(Path, CharSequence, OpenOption...)' for writing text into a file by one "
						+ "single invocation and in an efficient non-blocking manner. \nThis rule replaces 'BufferedWriters' "
						+ "that are used to write a single value into a file, with 'Files.write(...)'."));
	}
}
