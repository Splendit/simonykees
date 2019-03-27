package eu.jsparrow.standalone.renaming;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.jsparrow.core.rule.impl.FieldsRenamingRule;
import eu.jsparrow.core.visitor.renaming.FieldDeclarationVisitorWrapper;
import eu.jsparrow.core.visitor.renaming.FieldMetaData;
import eu.jsparrow.standalone.CompilationUnitProvider;
import eu.jsparrow.standalone.exceptions.StandaloneException;

public class FieldsRenamingInstantiatorTest {

	private FieldsRenamingInstantiator fieldsRenamingWrapper;
	private IJavaProject javaProject;
	private FieldDeclarationVisitorWrapper fieldDeclarationVisitorWrapper;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() {
		javaProject = mock(IJavaProject.class);
		fieldDeclarationVisitorWrapper = mock(FieldDeclarationVisitorWrapper.class);
		fieldsRenamingWrapper = new FieldsRenamingInstantiator(javaProject, fieldDeclarationVisitorWrapper);
	}

	@Test
	public void createRule_cannotCheckIfTargetIsExcluded_shouldThrowException() throws Exception {
		FieldMetaData metadata = mock(FieldMetaData.class);
		CompilationUnitProvider compilationUnitProvider = mock(CompilationUnitProvider.class);
		when(metadata.getReferences()).thenReturn(emptyList());
		when(compilationUnitProvider.containsExcludedFiles(any())).thenThrow(new StandaloneException("")); //$NON-NLS-1$

		expectedException.expect(StandaloneException.class);
		fieldsRenamingWrapper.createRule(singletonList(metadata), compilationUnitProvider);
	}

	@Test
	public void createRule_referencesInExcludedFiles_shouldReturnRuleWithNoMetaData() throws Exception {
		FieldMetaData metadata = mock(FieldMetaData.class);
		CompilationUnitProvider compilationUnitProvider = mock(CompilationUnitProvider.class);
		when(metadata.getReferences()).thenReturn(emptyList());
		when(compilationUnitProvider.containsExcludedFiles(any())).thenReturn(true);

		FieldsRenamingRule rule = fieldsRenamingWrapper.createRule(singletonList(metadata),
				compilationUnitProvider);
		assertThat(rule.getMetaData(), equalTo(emptyList()));
	}

	@Test
	public void createRule_noReferenceToExcludedFiles_shouldReturnRuleWithMetaData() throws Exception {
		FieldMetaData metadata = mock(FieldMetaData.class);
		CompilationUnitProvider compilationUnitProvider = mock(CompilationUnitProvider.class);
		when(metadata.getReferences()).thenReturn(emptyList());
		when(compilationUnitProvider.containsExcludedFiles(any())).thenReturn(false);

		FieldsRenamingRule rule = fieldsRenamingWrapper.createRule(singletonList(metadata),
				compilationUnitProvider);
		assertThat(rule.getMetaData(), hasSize(1));
	}

	@Test
	public void findFields() throws Exception {
		List<ICompilationUnit> selectedElements = new ArrayList<>();
		Map<String, Boolean> options = new HashMap<>();

		when(fieldDeclarationVisitorWrapper.prepareRenaming(selectedElements, options)).thenReturn(1);

		expectedException.expect(StandaloneException.class);
		fieldsRenamingWrapper.findFields(selectedElements, options);
	}

	@Test
	public void findFields_prepareRenamingOk_shouldInvokeGetMetaData() throws Exception {
		List<ICompilationUnit> selectedElements = new ArrayList<>();
		Map<String, Boolean> options = new HashMap<>();
		when(fieldDeclarationVisitorWrapper.prepareRenaming(selectedElements, options)).thenReturn(0);

		fieldsRenamingWrapper.findFields(selectedElements, options);

		verify(fieldDeclarationVisitorWrapper, times(1)).getFieldsMetaData();
	}
}
