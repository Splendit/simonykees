package eu.jsparrow.standalone.renaming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;

import eu.jsparrow.core.rule.impl.PublicFieldsRenamingRule;
import eu.jsparrow.core.visitor.renaming.FieldDeclarationVisitorWrapper;
import eu.jsparrow.core.visitor.renaming.FieldMetaData;
import eu.jsparrow.core.visitor.renaming.ReferenceSearchMatch;
import eu.jsparrow.standalone.CompilationUnitProvider;
import eu.jsparrow.standalone.exceptions.StandaloneException;

public class PublicFieldsRenamingWrapper {

	private IJavaProject javaProject;
	private FieldDeclarationVisitorWrapper visitorWrapper;
	private static final String SEARCH_SCOPE = "workspace"; //$NON-NLS-1$

	public PublicFieldsRenamingWrapper(IJavaProject javaProject) {
		this.javaProject = javaProject;
		this.visitorWrapper = new FieldDeclarationVisitorWrapper(javaProject, SEARCH_SCOPE);
	}

	/**
	 * 
	 * @param javaProject
	 *            the current IJavaProject
	 * @param iCompilationUnits
	 *            all compilation units of the project except for the excluded
	 *            ones
	 * @param options
	 * @return
	 * @throws StandaloneException
	 */
	public PublicFieldsRenamingRule createRule(List<FieldMetaData> metadata,
			CompilationUnitProvider compilationUnitProvider) throws StandaloneException {

		List<FieldMetaData> filteredmetadata = new ArrayList<>();
		for (FieldMetaData md : metadata) {
			if (!hasReferencesToUnmodifiableFiles(md, compilationUnitProvider)) {
				filteredmetadata.add(md);
			}
		}
		return new PublicFieldsRenamingRule(filteredmetadata, Collections.emptyList());

	}

	public List<FieldMetaData> findFields(List<ICompilationUnit> selectedJavaElements, Map<String, Boolean> options)
			throws StandaloneException {
		int status = visitorWrapper.prepareRenaming(selectedJavaElements, options);

		if (status != 0) {
			throw new StandaloneException("It is not safe to run the renaming rule"); //$NON-NLS-1$
		}
		return visitorWrapper.getFieldsMetaData();
	}

	private boolean hasReferencesToUnmodifiableFiles(FieldMetaData fieldMetaData,
			CompilationUnitProvider compilationUnitProvider) throws StandaloneException {
		List<ICompilationUnit> compilationUnitsWithReferences = fieldMetaData.getReferences()
			.stream()
			.map(ReferenceSearchMatch::getICompilationUnit)
			.collect(Collectors.toList());
		if (compilationUnitProvider.containsExcludedFiles(compilationUnitsWithReferences)) {
			return true;
		}
		String currentProjectName = javaProject.getElementName();
		return compilationUnitsWithReferences.stream()
			.map(icu -> icu.getJavaProject()
				.getElementName())
			.anyMatch(name -> !currentProjectName.equals(name));
	}
}
