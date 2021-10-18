package eu.jsparrow.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.rules.imports.OrganizeImportsRule;

@SuppressWarnings("nls")
public class OrganizeImportsRulesTest extends SingleRuleTest {

	private static final String SAMPLE_FILE = "OrganiseImportsRule.java";
	private static final String CONFLICTING_IMPORTS_FILE = "OrganiseConflictingImportsRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "organiseImports";

	private OrganizeImportsRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new OrganizeImportsRule();
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
	@Disabled
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
