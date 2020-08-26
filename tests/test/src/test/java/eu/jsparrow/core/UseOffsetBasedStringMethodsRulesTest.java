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

	private static final String SAMPLE_FILE_IMPORT_MAX_CLASHING = "UseOffsetBasedStringMethodsImportMaxClashRule.java";
	private static final String SAMPLE_FILE_ALL_IMPORTS_CLASHING = "UseOffsetBasedStringMethodsAllImportsClashRule.java";
	private static final String SAMPLE_FILE_ALL_IMPORTS_ON_DEMAND_CLASHING = "UseOffsetBasedStringMethodsAllImportsOnDemandClashRule.java";
	private static final String SAMPLE_FILE_AMBIGUOUS_IMPORTS_ON_DEMAND = "UseOffsetBasedStringMethodsAmbiguousImportsOnDemandRule.java";
	
	private static final String POSTRULE_SUBDIRECTORY = "useOffsetBasedStringMethods";

	private UseOffsetBasedStringMethodsRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new UseOffsetBasedStringMethodsRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	public void testImportMaxClashing() throws Exception {
		// Test file references classes from utilities package
		loadUtilities();

		Path preRule = getPreRuleFile(SAMPLE_FILE_IMPORT_MAX_CLASHING);
		Path postRule = getPostRuleFile(SAMPLE_FILE_IMPORT_MAX_CLASHING, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	public void testAllImportsClashing() throws Exception {
		// Test file references classes from utilities package
		loadUtilities();

		Path preRule = getPreRuleFile(SAMPLE_FILE_ALL_IMPORTS_CLASHING);
		Path postRule = getPostRuleFile(SAMPLE_FILE_ALL_IMPORTS_CLASHING, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
	
	@Test
	public void testAllImportsOnDemandClashing() throws Exception {
		// Test file references classes from utilities package
		loadUtilities();

		Path preRule = getPreRuleFile(SAMPLE_FILE_ALL_IMPORTS_ON_DEMAND_CLASHING);
		Path postRule = getPostRuleFile(SAMPLE_FILE_ALL_IMPORTS_ON_DEMAND_CLASHING, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
	
	@Test
	public void testAmbiguousImportsOnDemandClashing() throws Exception {
		// Test file references classes from utilities package
		loadUtilities();

		Path preRule = getPreRuleFile(SAMPLE_FILE_AMBIGUOUS_IMPORTS_ON_DEMAND);
		Path postRule = getPostRuleFile(SAMPLE_FILE_AMBIGUOUS_IMPORTS_ON_DEMAND, POSTRULE_SUBDIRECTORY);

		String actual = replacePackageName(applyRefactoring(rule, preRule), getPostRulePackage(POSTRULE_SUBDIRECTORY));

		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
	
}
