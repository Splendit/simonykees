package eu.jsparrow.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.rule.impl.StringUtilsRule;
import eu.jsparrow.core.util.RulesTestUtil;

@SuppressWarnings("nls")
public class StringUtilsRulesTest extends SingleRuleTest {

	private static final String SAMPLE_FILE = "StringUtilsRefactorRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "stringUtils";

	private StringUtilsRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new StringUtilsRule();
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
	public void calculateEnabledForProject_missingLibraryOnClassPath_shouldReturnFalse() throws Exception {
		// 2.7 is not supported
		RulesTestUtil.addToClasspath(testProject, Arrays
			.asList(RulesTestUtil.generateMavenEntryFromDepedencyString("commons-lang", "commons-lang", "2.6")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
		assertFalse(rule.isSatisfiedLibraries());
		assertTrue(rule.isSatisfiedJavaVersion());
	}

	@Test
	public void calculateEnabledForProject_minimumSupportedLibraryVersion_shouldReturnTrue() throws Exception {
		RulesTestUtil.addToClasspath(testProject, Arrays
			.asList(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons", "commons-lang3", "3.0")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}
	
	@Test
	public void calculateEnabledForProject_initialSupportedLibraryVersion_shouldReturnTrue() throws Exception {
		RulesTestUtil.addToClasspath(testProject, Arrays
			.asList(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons", "commons-lang3", "3.1")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}
	
	@Test
	public void calculateEnabledForProject_requestedSupportLibraryVersion_shouldReturnTrue() throws Exception {
		RulesTestUtil.addToClasspath(testProject, Arrays
			.asList(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons", "commons-lang3", "3.4")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@Test
	public void calculateEnabledForProject_maximumSupportedLibraryVersion_shouldReturnTrue() throws Exception {
		RulesTestUtil.addToClasspath(testProject, Arrays
			.asList(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons", "commons-lang3", "3.7")));
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

}
