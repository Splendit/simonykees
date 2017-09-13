package at.splendit.simonykees.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.JavaCore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import at.splendit.simonykees.core.rule.impl.LambdaToMethodReferenceRule;
import at.splendit.simonykees.core.rule.impl.UseIsEmptyRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.LambdaToMethodReferenceASTVisitor;
import at.splendit.simonykees.core.visitor.UseIsEmptyRuleASTVisitor;

@SuppressWarnings("nls")
public class LambdaToMethodReferenceRulesTest extends SingleRuleTest {
	
	private static final String SAMPLE_FILE = "LambdaToMethodReferenceRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "lambdaToMethodReference";

	private LambdaToMethodReferenceRule rule;

	@Before
	public void setUp() throws Exception {
		rule = new LambdaToMethodReferenceRule(LambdaToMethodReferenceASTVisitor.class);
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	public void testTransformationWithDefaultFile() throws Exception {
		// Test file references classes from utilities package
		loadUtilities();
		
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
