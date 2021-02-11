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

import eu.jsparrow.core.rule.impl.MigrateJUnit4ToJupiterRule;
import eu.jsparrow.core.util.RulesTestUtil;

public class TestMigrateJUnit4ToJupiterRule extends SingleRuleTest {

	private static final String SAMPLE_FILE_ALWAYS_TRANSFORMED = "MigrateJUnit4ToJupiterAlwaysTransformedRule.java";
	private static final String SAMPLE_FILE_CONDITIONALLY_TRANSFORMED = "MigrateJUnit4ToJupiterConditionallyTransformedRule.java";

	private static final String POSTRULE_SUBDIRECTORY = "migrateJUnitToJupiter";

	private MigrateJUnit4ToJupiterRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new MigrateJUnit4ToJupiterRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}
	
	@Test
	public void testAlwaysTransformed() throws Exception {
		loadUtilities();

		Path preRule = getPreRuleFile(SAMPLE_FILE_ALWAYS_TRANSFORMED);
		Path postRule = getPostRuleFile(SAMPLE_FILE_ALWAYS_TRANSFORMED, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
	
	@Test
	public void testConditionallyTransformed() throws Exception {
		loadUtilities();

		Path preRule = getPreRuleFile(SAMPLE_FILE_CONDITIONALLY_TRANSFORMED);
		Path postRule = getPostRuleFile(SAMPLE_FILE_CONDITIONALLY_TRANSFORMED, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}


	@Disabled("TODO discuss")
	@Test
	public void calculateEnabledForProjectShouldBeEnabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}
}
