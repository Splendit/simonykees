package eu.jsparrow.core;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.core.JavaCore;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.core.rule.impl.ImmutableStaticFinalCollectionsRule;
import eu.jsparrow.core.util.RulesTestUtil;

@SuppressWarnings("nls")
public class ImmutableStaticFinalCollectionsRulesJ7Test extends SingleRuleTest {

	private static final String SAMPLE_FILE = "ImmutableStaticFinalCollectionsRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "immutableStaticFinalCollectionsJ7";

	private ImmutableStaticFinalCollectionsRule rule;

	@Before
	public void setUp() throws Exception {
		rule = new ImmutableStaticFinalCollectionsRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	public void testTransformationWithDefaultFile() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
		rule.ruleSpecificImplementation(testProject);

		Path preRule = getPreRuleFile(SAMPLE_FILE);
		Path postRule = getPostRuleFile(SAMPLE_FILE, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
}
