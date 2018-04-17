package eu.jsparrow.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.core.JavaCore;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.core.rule.impl.ForToForEachRule;
import eu.jsparrow.core.util.RulesTestUtil;

@SuppressWarnings("nls")
public class ForToForEachRulesTest extends SingleRuleTest {

	private static final String SAMPLE_FILE = "TestForToForEachRule.java";
	private static final String ARRAY_ITERATING_FILE = "TestForToForEachArrayIteratingIndexRule.java";
	private static final String LIST_ITERATING_FILE = "TestForToForEachListIteratingIndexRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "forToForEach";

	private ForToForEachRule rule;

	@Before
	public void setUp() throws Exception {
		rule = new ForToForEachRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	public void testTransformationWithDefaultFile() throws Exception {
		Path preRule = getPreRuleFile(SAMPLE_FILE);
		Path postRule = getPostRuleFile(SAMPLE_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testTransformationWithArrayIteratingFile() throws Exception {
		Path preRule = getPreRuleFile(ARRAY_ITERATING_FILE);
		Path postRule = getPostRuleFile(ARRAY_ITERATING_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testTransformationWithListIteratingFile() throws Exception {
		Path preRule = getPreRuleFile(LIST_ITERATING_FILE);
		Path postRule = getPostRuleFile(LIST_ITERATING_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void calculateEnabledForProjectShouldBeEnabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@Test
	public void calculateEnabledforProjectShouldBeDisabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}
}
