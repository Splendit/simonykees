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

import at.splendit.simonykees.core.rule.impl.ImmutableStaticFinalCollectionsRule;
import at.splendit.simonykees.core.rule.impl.IndexOfToContainsRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.ImmutableStaticFinalCollectionsASTVisitor;
import at.splendit.simonykees.core.visitor.IndexOfToContainsASTVisitor;

@SuppressWarnings("nls")
public class ImmutableStaticFinalCollectionsRulesTest extends SingleRuleTest {
	
	private static final String SAMPLE_FILE = "ImmutableStaticFinalCollectionsRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "immutableStaticFinalCollections";

	private ImmutableStaticFinalCollectionsRule rule;

	@Before
	public void setUp() throws Exception {
		rule = new ImmutableStaticFinalCollectionsRule(ImmutableStaticFinalCollectionsASTVisitor.class);
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
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_2);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@Test
	public void calculateEnabledforProjectShouldBeDisabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}
}
