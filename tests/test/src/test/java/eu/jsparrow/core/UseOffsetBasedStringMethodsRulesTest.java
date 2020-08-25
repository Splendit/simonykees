package eu.jsparrow.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.rule.impl.UseOffsetBasedStringMethodsRule;
import eu.jsparrow.core.util.RulesTestUtil;

@SuppressWarnings("nls")
public class UseOffsetBasedStringMethodsRulesTest extends SingleRuleTest {

	private static final String SAMPLE_FILE_IMPORT_CLASH_MAX_ON_DEMAND = "UseOffsetBasedStringMethodsImportMaxClashRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "useOffsetBasedStringMethods";

	private UseOffsetBasedStringMethodsRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new UseOffsetBasedStringMethodsRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	public void testTransformationWithDefaultFile() throws Exception {
		// Test file references classes from utilities package
		loadUtilities();

		Path preRule = getPreRuleFile(SAMPLE_FILE_IMPORT_CLASH_MAX_ON_DEMAND);
		Path postRule = getPostRuleFile(SAMPLE_FILE_IMPORT_CLASH_MAX_ON_DEMAND, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

}
