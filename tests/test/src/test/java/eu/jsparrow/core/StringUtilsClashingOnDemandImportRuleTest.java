package eu.jsparrow.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.core.rule.impl.StringUtilsRule;

@SuppressWarnings("nls")
public class StringUtilsClashingOnDemandImportRuleTest extends SingleRuleTest {

	private static final String SAMPLE_FILE_ON_DEMAND_IMPORT = "StringUtilsOnDemandClashingImportCornerCaseRule.java";
	private static final String POSTRULE_SUBDIRECTORY = "stringUtilsOnDemandClashinImport";

	private StringUtilsRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new StringUtilsRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");

	}

	@Test
	public void testTransformationWithDefaultFile() throws Exception {
		Path preRule = getPreRuleFile(SAMPLE_FILE_ON_DEMAND_IMPORT);
		Path postRule = getPostRuleFile(SAMPLE_FILE_ON_DEMAND_IMPORT, POSTRULE_SUBDIRECTORY);

		// A utility class is imported in
		// StringUtilsClashingImportCornerCaseRule.java
		loadUtilities();
		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
}
