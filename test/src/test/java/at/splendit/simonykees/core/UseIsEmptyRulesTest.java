package at.splendit.simonykees.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.core.JavaCore;
import org.junit.Before;
import org.junit.Test;

import at.splendit.simonykees.core.rule.impl.UseIsEmptyRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.UseIsEmptyRuleASTVisitor;

@SuppressWarnings("nls")
public class UseIsEmptyRulesTest extends SingleRuleTest {

	private static final String SAMPLE_FILE = "TestUseIsEmptyRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "useIsEmpty";

	private Path preRule, postRule;

	private UseIsEmptyRule rule;

	@Before
	public void setUp() throws Exception {
		rule = new UseIsEmptyRule(UseIsEmptyRuleASTVisitor.class);
		testproject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	public void testTransformationWithDefaultFile() throws Exception {
		preRule = getPreRuleFile(SAMPLE_FILE);
		postRule = getPostRuleFile(SAMPLE_FILE, POSTRULE_SUBDIRECTORY);
		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		assertEquals(expected, actual);
	}

	@Test
	public void calculateEnabledForProjectShouldBeEnabled() {
		testproject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);

		rule.calculateEnabledForProject(testproject);

		assertTrue(rule.isEnabled());
	}

	@Test
	public void calculateEnabledforProjectShouldBeDisabled() {
		testproject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);

		rule.calculateEnabledForProject(testproject);

		assertFalse(rule.isEnabled());
	}
}