package eu.jsparrow.core.rule.impl;

import static eu.jsparrow.core.util.RulesTestUtil.addToClasspath;
import static eu.jsparrow.core.util.RulesTestUtil.generateMavenEntryFromDepedencyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.SingleRuleTest;
import eu.jsparrow.core.util.RulesTestUtil;

class ReplaceJUnit4AssumptionsWithHamcrestJUnitRuleTest extends SingleRuleTest {
	private static final String SAMPLE_FILE_TRANSFORM_IMPORTS = "ReplaceJUnit4AssumptionsWithHamcrestJUnitTransformImportsRule.java";

	private static final String POSTRULE_SUBDIRECTORY = "migrateJUnitToJupiter";

	private ReplaceJUnit4AssumptionsWithHamcrestJUnitRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new ReplaceJUnit4AssumptionsWithHamcrestJUnitRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void testTransformImports() throws Exception {
		loadUtilities();

		addToClasspath(testProject, Arrays.asList(
				generateMavenEntryFromDepedencyString("junit", "junit", "4.13"),
				generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-library", "1.3"),
				generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-core", "1.3"),
				generateMavenEntryFromDepedencyString("org.hamcrest", "hamcrest-junit", "1.0.0.0")));

		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		rule.calculateEnabledForProject(testProject);
		assertTrue(rule.isEnabled());

		Path preRule = getPreRuleFile(SAMPLE_FILE_TRANSFORM_IMPORTS);
		Path postRule = getPostRuleFile(SAMPLE_FILE_TRANSFORM_IMPORTS, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

}
