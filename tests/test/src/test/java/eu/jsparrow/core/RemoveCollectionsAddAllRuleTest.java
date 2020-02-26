package eu.jsparrow.core;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.rule.impl.RemoveCollectionsAddAllRule;
import eu.jsparrow.core.util.RulesTestUtil;

@SuppressWarnings("nls")
public class RemoveCollectionsAddAllRuleTest extends SingleRuleTest {
	
	private static final String SAMPLE_FILE = "RemoveCollectionsAddAllRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "collectionsAddAll";

	private RemoveCollectionsAddAllRule rule;
	
	@BeforeEach
	public void setUp() throws Exception {
		rule = new RemoveCollectionsAddAllRule();
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
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_2);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}
	
	@Test
	public void calculateEnabledForProjectShouldBeDisabled() {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertFalse(rule.isEnabled());
	}

}
