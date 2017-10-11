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

import eu.jsparrow.core.rule.impl.InefficientConstructorRule;
import eu.jsparrow.core.util.RulesTestUtil;

@SuppressWarnings("nls")
public class InefficientConstructorRulesTest extends SingleRuleTest {

	private static final String BOOLEANRULE_SAMPLEFILE = "TestInefficientConstructorBooleanRule.java";
	private static final String PRIMITIVE_SAMPLEFILE = "TestInefficientConstructorPrimitiveRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "inefficientConstructor";

	private InefficientConstructorRule rule;

	@Before
	public void setUp() throws Exception {
		rule = new InefficientConstructorRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}
	

	@Test
	public void testTransformationWithDefaultFile() throws Exception {
		Path preRule = getPreRuleFile(BOOLEANRULE_SAMPLEFILE);
		Path postRule = getPostRuleFile(BOOLEANRULE_SAMPLEFILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
	

	@Test
	public void testTransformationWithPrimitiveFile() throws Exception {
		Path preRule = getPreRuleFile(PRIMITIVE_SAMPLEFILE);
		Path postRule = getPostRuleFile(PRIMITIVE_SAMPLEFILE, POSTRULE_SUBDIRECTORY);

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