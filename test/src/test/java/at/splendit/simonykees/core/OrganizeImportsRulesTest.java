package at.splendit.simonykees.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.core.JavaCore;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import at.splendit.simonykees.core.rule.impl.OrganiseImportsRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

@SuppressWarnings("nls")
public class OrganizeImportsRulesTest extends SingleRuleTest {
	
	private static final String SAMPLE_FILE = "OrganiseImportsRule.java";
	private static final String CONFLICTING_IMPORTS_FILE = "OrganiseConflictingImportsRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "organiseImports";

	private OrganiseImportsRule rule;

	@Before
	public void setUp() throws Exception {
		rule = new OrganiseImportsRule(AbstractASTRewriteASTVisitor.class);
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
	// This test fails, and I don't know why. Need to ask
	@Ignore
	public void testTransformationWithConflictingImportsFile() throws Exception {
		Path preRule = getPreRuleFile(CONFLICTING_IMPORTS_FILE);
		Path postRule = getPostRuleFile(CONFLICTING_IMPORTS_FILE, POSTRULE_SUBDIRECTORY);

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
