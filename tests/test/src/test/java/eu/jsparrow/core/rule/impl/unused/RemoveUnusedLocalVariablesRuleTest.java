package eu.jsparrow.core.rule.impl.unused;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class RemoveUnusedLocalVariablesRuleTest extends SingleRuleTest {

	private RemoveUnusedLocalVariablesRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new RemoveUnusedLocalVariablesRule();
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("RemoveUnusedLocalVariables"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Remove Unused Local Variables", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS), description.getTags());
		assertEquals(2, description.getRemediationCost()
			.toMinutes());
		assertEquals("Finds and removes local variables that are never used actively.",
				description.getDescription());
	}

	@Test
	void test_requiredLibraries() throws Exception {

		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);

		rule.calculateEnabledForProject(testProject);

		assertThat(rule.requiredLibraries(), nullValue());
	}

	@Test
	void test_requiredJavaVersion() throws Exception {
		assertThat(rule.getRequiredJavaVersion(), equalTo("1.1"));
	}

	@Test
	void calculateEnabledForProject_ShouldBeEnabled() throws Exception {
		testProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_1);

		rule.calculateEnabledForProject(testProject);

		assertTrue(rule.isEnabled());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"UnusedLocalVariables",
			"UnusedLocalVariablesWithAnnotation"
	})
	void testTransformation(String javaClassName) throws Exception {
		String javaFileName = javaClassName + ".java";
		Path preRule = getPreRuleFile("unused/" + javaFileName);
		Path postRule = getPostRuleFile(javaFileName, "unused");

		RemoveUnusedLocalVariablesRule rule = new RemoveUnusedLocalVariablesRule();

		String refactoring = applyRemoveUnusedFieldRefactoring(rule, "eu.jsparrow.sample.preRule.unused", preRule,
				root);
		String postRulePackage = getPostRulePackage("unused");
		String actual = StringUtils.replace(refactoring, "package eu.jsparrow.sample.preRule.unused",
				postRulePackage);
		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);

		assertEquals(expected, actual);
	}

	private String applyRemoveUnusedFieldRefactoring(RemoveUnusedLocalVariablesRule rule, String packageString,
			Path preFile, IPackageFragmentRoot root) throws Exception {

		IPackageFragment packageFragment = root.createPackageFragment(packageString, true, null);
		String fileName = preFile.getFileName()
			.toString();
		String content = new String(Files.readAllBytes(preFile), StandardCharsets.UTF_8);
		ICompilationUnit compilationUnit = packageFragment.createCompilationUnit(fileName, content, true, null);
		List<ICompilationUnit> compilationUnits = new ArrayList<>();
		compilationUnits.add(compilationUnit);

		RefactoringPipeline refactoringPipeline = new RefactoringPipeline(Arrays.asList(rule));
		IProgressMonitor monitor = new NullProgressMonitor();

		refactoringPipeline.prepareRefactoring(compilationUnits, monitor);
		refactoringPipeline.doRefactoring(monitor);
		refactoringPipeline.commitRefactoring();

		return compilationUnit.getSource();
	}

}
