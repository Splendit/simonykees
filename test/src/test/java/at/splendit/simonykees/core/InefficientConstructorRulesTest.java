package at.splendit.simonykees.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.splendit.simonykees.core.rule.impl.InefficientConstructorRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.InefficientConstructorASTVisitor;

@SuppressWarnings("nls")
public class InefficientConstructorRulesTest extends AbstractRulesTest {

	private static final String BOOLEANRULE_SAMPLEFILE = "TestInefficientConstructorBooleanRule.java";
	private static final String PRIMITIVE_SAMPLEFILE = "TestInefficientConstructorPrimitiveRule.java";
	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.inefficientConstructor";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/inefficientConstructor";

	private String fileName;
	private Path preRule, postRule;

	private InefficientConstructorRule rule;
	private IJavaProject testproject;

	@Before
	public void setUp() throws Exception {
		rule = new InefficientConstructorRule(InefficientConstructorASTVisitor.class);
		testproject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@After
	public void tearDown() {
		rulesList.clear();
	}

	@Test
	public void testTransformationWithBoolean() throws Exception {
		preRule = Paths.get(RulesTestUtil.PRERULE_DIRECTORY, BOOLEANRULE_SAMPLEFILE);
		postRule = Paths.get(POSTRULE_DIRECTORY, BOOLEANRULE_SAMPLEFILE);
		fileName = preRule.getFileName().toString();
		rulesList.add(rule);

		super.testTransformation(postRule, preRule, fileName, POSTRULE_PACKAGE);
	}

	@Test
	public void testTransformationWithPrimitive() throws Exception {
		preRule = Paths.get(RulesTestUtil.PRERULE_DIRECTORY, PRIMITIVE_SAMPLEFILE);
		postRule = Paths.get(POSTRULE_DIRECTORY, PRIMITIVE_SAMPLEFILE);
		fileName = preRule.getFileName().toString();
		rulesList.add(rule);

		super.testTransformation(postRule, preRule, fileName, POSTRULE_PACKAGE);
	}
	
	@Test
	public void calculateEnabledForProjectShouldBeEnabled() {
		testproject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);

		rule.calculateEnabledForProject(testproject);

		assertTrue(rule.isEnabled());
	}

	@Test
	public void calculateEnabledforProjectShouldBeDisabled() {
		testproject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);

		rule.calculateEnabledForProject(testproject);

		assertFalse(rule.isEnabled());
	}
	
	
}