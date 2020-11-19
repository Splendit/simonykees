package eu.jsparrow.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.rule.impl.UseComparatorMethodsRule;
import eu.jsparrow.core.util.RulesTestUtil;

@SuppressWarnings("nls")
public class UseComparatorMethodsRulesTest extends SingleRuleTest {

	private static final String TEST_USE_COMPARATOR_METHODS_RULE = "TestUseComparatorMethodsRule.java";

	private static final String POSTRULE_SUBDIRECTORY = "useComparatorMethods";

	private UseComparatorMethodsRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new UseComparatorMethodsRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	public void testUseComparatorMethods() throws Exception {
		loadUtilities();

		Path preRule = getPreRuleFile(TEST_USE_COMPARATOR_METHODS_RULE);
		Path postRule = getPostRuleFile(TEST_USE_COMPARATOR_METHODS_RULE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void calculateEnabledForProjectShouldNotBeEnabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}

	@Test
	public void calculateEnabledForProjectShouldBeEnabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}
}
