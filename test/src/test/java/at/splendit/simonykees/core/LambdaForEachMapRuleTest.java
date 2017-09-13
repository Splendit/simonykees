package at.splendit.simonykees.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import org.eclipse.jdt.core.JavaCore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import at.splendit.simonykees.core.rule.impl.LambdaForEachIfWrapperToFilterRule;
import at.splendit.simonykees.core.rule.impl.LambdaForEachMapRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.lambdaForEach.LambdaForEachIfWrapperToFilterASTVisitor;
import at.splendit.simonykees.core.visitor.lambdaForEach.LambdaForEachMapASTVisitor;

@SuppressWarnings("nls")
public class LambdaForEachMapRuleTest extends SingleRuleTest {
	private static final String SAMPLE_FILE = "LambdaForEachMapRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "lambdaForEachMap";

	private LambdaForEachMapRule rule;

	@Before
	public void setUp() throws Exception {
		rule = new LambdaForEachMapRule(LambdaForEachMapASTVisitor.class);
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
