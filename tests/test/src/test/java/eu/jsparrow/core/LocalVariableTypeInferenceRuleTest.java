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

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.rules.java10.LocalVariableTypeInferenceRule;

public class LocalVariableTypeInferenceRuleTest extends SingleRuleTest {

	private static final String SAMPLE_FILE = "LocalVariableTypeInferenceRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "localvar";

	private LocalVariableTypeInferenceRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new LocalVariableTypeInferenceRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
		root = RulesTestUtil.getPackageFragementRoot(JavaCore.VERSION_10);
		
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
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_10);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@Test
	public void calculateEnabledForProjectShouldBeDisabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_9);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}

}
