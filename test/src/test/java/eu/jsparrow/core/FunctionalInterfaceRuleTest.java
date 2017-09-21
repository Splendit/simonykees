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

import eu.jsparrow.core.rule.impl.FunctionalInterfaceRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.functionalinterface.FunctionalInterfaceASTVisitor;

@SuppressWarnings("nls")
public class FunctionalInterfaceRuleTest extends SingleRuleTest {
	
	private static final String SAMPLE_FILE1 = "TestFunctionalInterfaceRule.java";
	private static final String SAMPLE_FILE2 = "TestFunctionalInterface2Rule.java";
	private static final String SAMPLE_FILE3 = "TestFunctionalInterface3Rule.java";
	private static final String POSTRULE_SUBDIRECTORY = "functionalInterface";

	private FunctionalInterfaceRule rule;

	@Before
	public void setUp() throws Exception {
		rule = new FunctionalInterfaceRule(FunctionalInterfaceASTVisitor.class);
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	public void testTransformationWithDefaultFile() throws Exception {
		Path preRule = getPreRuleFile(SAMPLE_FILE1);
		Path postRule = getPostRuleFile(SAMPLE_FILE1, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
	
	@Test
	public void testTransformationWithAnotherFile() throws Exception {
		Path preRule = getPreRuleFile(SAMPLE_FILE2);
		Path postRule = getPostRuleFile(SAMPLE_FILE2, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
	
	@Test
	public void testTransformationWithYetAnotherFile() throws Exception {
		Path preRule = getPreRuleFile(SAMPLE_FILE3);
		Path postRule = getPostRuleFile(SAMPLE_FILE3, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void calculateEnabledForProjectShouldBeEnabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@Test
	public void calculateEnabledforProjectShouldBeDisabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}
}
