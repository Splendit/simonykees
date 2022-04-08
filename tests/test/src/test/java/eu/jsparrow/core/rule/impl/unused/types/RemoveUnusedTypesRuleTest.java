package eu.jsparrow.core.rule.impl.unused.types;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.SingleRuleTest;
import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.core.rule.impl.unused.RemoveUnusedTypesRule;
import eu.jsparrow.core.rule.impl.unused.UnusedCodeTestHelper;
import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;
import eu.jsparrow.core.visitor.unused.type.UnusedTypeWrapper;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.exception.RefactoringException;

class RemoveUnusedTypesRuleTest extends SingleRuleTest {

	private static final String PRERULE_UNUSED_PACKAGE = "eu.jsparrow.sample.preRule.unused.types";
	private static final String PRERULE_DIRECTORY = RulesTestUtil.PRERULE_DIRECTORY + "/unused/types";

	private RemoveUnusedTypesRule rule;

	@BeforeEach
	public void setUp() throws Exception {
		rule = new RemoveUnusedTypesRule(Collections.emptyList());
		testProject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@Test
	void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("RemoveUnusedTypes"));
	}

	@Test
	void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getName(), equalTo("Remove Unused Types"));
		assertThat(description.getTags(),
				contains(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS));
		assertThat(description.getRemediationCost(), equalTo(Duration.ofMinutes(2)));
		assertThat(description.getDescription(),
				equalTo("Finds and removes types that are not used."));
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
			"HelloWorld",
			"CompleteCompilationUnitToRemove",
			"ClassUsedAsFieldInSamePackage",
			"ClassUsedAsFieldInOtherPackage",
			"ClassExtendedInSamePackage",
			"ClassExtendedInOtherPackage",
			"ClassUsingClasses",
			"ClassWithNestedClasses",
			"ClassWithLocalClasses",
	})
	void testTransformation(String className) throws Exception {
		String preRuleFilePath = String.format("unused/types/%s.java", className);
		Path preRule = getPreRuleFile(preRuleFilePath);
		Path postRule = getPostRuleFile(className + ".java", "unused/types");

		List<UnusedTypeWrapper> unusedTypes = UnusedCodeTestHelper.findTypesToBeRemoved(PRERULE_UNUSED_PACKAGE,
				PRERULE_DIRECTORY);
		RemoveUnusedTypesRule rule = new RemoveUnusedTypesRule(unusedTypes);

		String refactoring = UnusedCodeTestHelper.applyRemoveUnusedCodeRefactoring(rule,
				"eu.jsparrow.sample.preRule.unused.types", preRule, root);
		String postRulePackage = getPostRulePackage("unused.types");
		String actual = StringUtils.replace(refactoring, "package eu.jsparrow.sample.preRule.unused.types",
				postRulePackage);
		String expected = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);

		assertEquals(expected, actual);
	}

	@Test
	void deleteCompilationUnits_shouldInvokeDelete() throws Exception {
		CompilationUnit compilationUnit = mock(CompilationUnit.class);
		ICompilationUnit icu = mock(ICompilationUnit.class);
		AbstractTypeDeclaration type = mock(TypeDeclaration.class);
		when(type.getName()).thenReturn(mock(SimpleName.class));
		when(compilationUnit.getJavaElement()).thenReturn(icu);
		when(icu.getElementName()).thenReturn("ToBeDeleted");
		List<UnusedTypeWrapper> unusedTypes = new ArrayList<>();
		UnusedTypeWrapper wrapper = new UnusedTypeWrapper(compilationUnit, JavaAccessModifier.PUBLIC, type, true);
		unusedTypes.add(wrapper);
		RemoveUnusedTypesRule rule = new RemoveUnusedTypesRule(unusedTypes);
		rule.deleteEmptyCompilationUnits();
		verify(icu).delete(true, null);
	}

	@Test
	void deleteCompilationUnits_shouldNotDelete() throws Exception {
		List<UnusedTypeWrapper> unusedTypes = new ArrayList<>();
		CompilationUnit compilationUnit = mock(CompilationUnit.class);
		ICompilationUnit icu = mock(ICompilationUnit.class);
		AbstractTypeDeclaration type = mock(TypeDeclaration.class);
		when(type.getName()).thenReturn(mock(SimpleName.class));
		when(compilationUnit.getJavaElement()).thenReturn(icu);
		when(icu.getElementName()).thenReturn("ToBeDeleted");
		UnusedTypeWrapper wrapper = new UnusedTypeWrapper(compilationUnit, JavaAccessModifier.PUBLIC, type, false);
		unusedTypes.add(wrapper);
		RemoveUnusedTypesRule rule = new RemoveUnusedTypesRule(unusedTypes);
		rule.deleteEmptyCompilationUnits();
		verify(compilationUnit, times(1)).getJavaElement();
		verify(icu, never()).delete(true, null);
	}

	@Test
	void deleteCompilationUnits_shouldThrowException() throws Exception {
		List<UnusedTypeWrapper> unusedTypes = new ArrayList<>();
		CompilationUnit compilationUnit = mock(CompilationUnit.class);
		ICompilationUnit icu = mock(ICompilationUnit.class);
		AbstractTypeDeclaration type = mock(TypeDeclaration.class);
		when(type.getName()).thenReturn(mock(SimpleName.class));
		when(compilationUnit.getJavaElement()).thenReturn(icu);
		when(icu.getElementName()).thenReturn("ToBeDeleted");
		doThrow(JavaModelException.class).when(icu).delete(true, null);
		UnusedTypeWrapper wrapper = new UnusedTypeWrapper(compilationUnit, JavaAccessModifier.PUBLIC, type, true);
		unusedTypes.add(wrapper);
		RemoveUnusedTypesRule rule = new RemoveUnusedTypesRule(unusedTypes);
		assertThrows(RefactoringException.class, 
				() -> rule.deleteEmptyCompilationUnits(),  
				() -> "The following compilation units could not be removed:\nToBeDeleted");
	}
}
