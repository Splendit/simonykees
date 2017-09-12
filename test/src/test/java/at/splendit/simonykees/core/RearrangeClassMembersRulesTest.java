package at.splendit.simonykees.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.core.JavaCore;
import org.junit.Before;
import org.junit.Test;

import at.splendit.simonykees.core.rule.impl.RearrangeClassMembersRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.RearrangeClassMembersASTVisitor;

@SuppressWarnings("nls")
public class RearrangeClassMembersRulesTest extends SingleRuleTest {
	
	private static final String SAMPLE_FILE = "RearrangeClassMembersRule.java";
	private static final String PARTIALLY_ARRANGED_FILE = "RearrangeClassMembersPartiallyArrangedRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "rearrangeClassMembers";

	private RearrangeClassMembersRule rule;

	@Before
	public void setUp() throws Exception {
		rule = new RearrangeClassMembersRule(RearrangeClassMembersASTVisitor.class);
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
	public void testTransformationWithPartiallyArrangedFile() throws Exception {
		Path preRule = getPreRuleFile(PARTIALLY_ARRANGED_FILE);
		Path postRule = getPostRuleFile(PARTIALLY_ARRANGED_FILE, POSTRULE_SUBDIRECTORY);
		
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
