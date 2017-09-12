package at.splendit.simonykees.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.core.JavaCore;
import org.junit.Before;
import org.junit.Test;

import at.splendit.simonykees.core.rule.impl.RemoveNewStringConstructorRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.RemoveNewStringConstructorASTVisitor;

@SuppressWarnings("nls")
public class RemoveNewStringConstructorRulesTest extends SingleRuleTest {

	private static final String SAMPLE_FILE = "TestRemoveNewStringConstructorRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "removeNewString";

	private RemoveNewStringConstructorRule rule;

	@Before
	public void setUp() throws Exception {
		rule = new RemoveNewStringConstructorRule(RemoveNewStringConstructorASTVisitor.class);
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
	public void calculateEnabledForProjectShouldBeEnabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}
}