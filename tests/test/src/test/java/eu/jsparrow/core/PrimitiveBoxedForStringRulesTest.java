package eu.jsparrow.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.core.rule.impl.PrimitiveBoxedForStringRule;

@SuppressWarnings("nls")
public class PrimitiveBoxedForStringRulesTest extends SingleRuleTest {

	private static final String WITH_CONSTANTS_FILE = "TestPrimitiveBoxedForStringWithConstantsRule.java";
	private static final String WITH_EXPRESSIONS_FILE = "TestPrimitiveBoxedForStringWithExpressionsRule.java";
	private static final String WITH_OBJECT_VARIABLES_FILE = "TestPrimitiveBoxedForStringWithObjectVariablesRule.java";
	private static final String WITH_VARIABLES_FILE = "TestPrimitiveBoxedForStringWithVariablesRule.java";

	private static final String POSTRULE_SUBDIRECTORY = "primitiveBoxed";

	private PrimitiveBoxedForStringRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new PrimitiveBoxedForStringRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	public void testTransformationWithConstantsFile() throws Exception {
		Path preRule = getPreRuleFile(WITH_CONSTANTS_FILE);
		Path postRule = getPostRuleFile(WITH_CONSTANTS_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testTransformationWithExpressionsFile() throws Exception {
		Path preRule = getPreRuleFile(WITH_EXPRESSIONS_FILE);
		Path postRule = getPostRuleFile(WITH_EXPRESSIONS_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testTransformationWithObjectVariablesFile() throws Exception {
		Path preRule = getPreRuleFile(WITH_OBJECT_VARIABLES_FILE);
		Path postRule = getPostRuleFile(WITH_OBJECT_VARIABLES_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testTransformationWithVariablesFile() throws Exception {
		Path preRule = getPreRuleFile(WITH_VARIABLES_FILE);
		Path postRule = getPostRuleFile(WITH_VARIABLES_FILE, POSTRULE_SUBDIRECTORY);

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