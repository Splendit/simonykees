package eu.jsparrow.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.core.JavaCore;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.core.rule.impl.StringLiteralEqualityCheckRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.StringLiteralEqualityCheckASTVisitor;

@SuppressWarnings("nls")
public class StringLiteralEqualityCheckRuleTest extends SingleRuleTest {

	private static final String SAMPLE_FILE = "StringLiteralEqualityCheckRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "stringLiteralEqualityCheck";

	private StringLiteralEqualityCheckRule rule;

	@Before
	public void setUp() throws Exception {
		rule = new StringLiteralEqualityCheckRule(StringLiteralEqualityCheckASTVisitor.class);
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
