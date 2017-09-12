package at.splendit.simonykees.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Before;
import org.junit.Test;

import at.splendit.simonykees.core.rule.impl.standardLogger.StandardLoggerRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.semiAutomatic.StandardLoggerASTVisitor;

@SuppressWarnings("nls")
public class StandardLoggerRuleLog4jTest extends SingleRuleTest {

	private static final String STANDARD_FILE = "TestStandardLoggerRule.java";
	private static final String CONFLICT_FILE = "TestStandardLoggerConflictRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "standardLoggerLog4j";

	private StandardLoggerRule rule;

	@Before
	public void setUp() throws Exception {
		rule = new StandardLoggerRule(StandardLoggerASTVisitor.class);
		rule.activateDefaultOptions();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	public void testTransformationWithDefaultFile() throws Exception {
		root = RulesTestUtil.addSourceContainer(testProject, "/allRulesTestRoot");

		RulesTestUtil.addToClasspath(testProject, Arrays.asList(
				RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.logging.log4j", "log4j-api", "2.7")));
		RulesTestUtil.addToClasspath(testProject, RulesTestUtil.getClassPathEntries(root));
		rule.calculateEnabledForProject(testProject);

		Path preRule = getPreRuleFile(STANDARD_FILE);
		Path postRule = getPostRuleFile(STANDARD_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testTransformationWithConflictFile() throws Exception {
		Path preRule = getPreRuleFile(CONFLICT_FILE);
		Path postRule = getPostRuleFile(CONFLICT_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void calculateEnabledForProjectShouldBeEnabled() throws Exception {
		RulesTestUtil.addToClasspath(testProject, Arrays.asList(
				RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.logging.log4j", "log4j-api", "2.7")));

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@Test
	public void calculateEnabledforProjectShouldBeDisabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}
}
