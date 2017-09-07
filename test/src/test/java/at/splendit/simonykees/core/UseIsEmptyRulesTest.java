package at.splendit.simonykees.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Before;
import org.junit.Test;

import at.splendit.simonykees.core.rule.impl.UseIsEmptyRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.UseIsEmptyRuleASTVisitor;

@SuppressWarnings("nls")
public class UseIsEmptyRulesTest extends AbstractRulesTest {

	private static final String SAMPLE_FILE = "TestUseIsEmptyRule.java";
	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.useIsEmpty";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/useIsEmpty";

	private String fileName;
	private Path preRule, postRule;
	private UseIsEmptyRule rule;
	private IJavaProject testproject;

	@Before
	public void setUp() throws Exception {
		rule = new UseIsEmptyRule(UseIsEmptyRuleASTVisitor.class);
		testproject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	public void testTransformationWithDefaultFile() throws Exception {
		preRule = Paths.get(RulesTestUtil.PRERULE_DIRECTORY, SAMPLE_FILE);
		postRule = Paths.get(POSTRULE_DIRECTORY, SAMPLE_FILE);
		fileName = preRule.getFileName().toString();
		rulesList.add(rule);

		super.testTransformation(postRule, preRule, fileName, POSTRULE_PACKAGE);
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