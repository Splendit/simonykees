package eu.jsparrow.standalone.renaming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;

import eu.jsparrow.core.rule.impl.FieldsRenamingRule;
import eu.jsparrow.core.visitor.renaming.FieldDeclarationASTVisitor;
import eu.jsparrow.core.visitor.renaming.FieldDeclarationVisitorWrapper;
import eu.jsparrow.core.visitor.renaming.FieldMetaData;
import eu.jsparrow.core.visitor.renaming.ReferenceSearchMatch;
import eu.jsparrow.standalone.CompilationUnitProvider;
import eu.jsparrow.standalone.exceptions.StandaloneException;

/**
 * Contains functionality for creating an instance of
 * {@link FieldsRenamingRule} based on the search result from
 * {@link FieldDeclarationVisitorWrapper}. Additionally, checks if any of the
 * references of the fields to be renamed falls in the excluded files.
 * 
 * @since 2.6.0
 */
public class FieldsRenamingInstantiator {

	private IJavaProject javaProject;
	private FieldDeclarationVisitorWrapper visitorWrapper;

	public FieldsRenamingInstantiator(IJavaProject javaProject, FieldDeclarationVisitorWrapper visitorWrapper) {
		this.javaProject = javaProject;
		this.visitorWrapper = visitorWrapper;
	}

	/**
	 * Creates an instance of the {@link FieldsRenamingRule} with the
	 * provided {@link FieldMetaData}. Skips the fields having references in the
	 * excluded files.
	 * 
	 * @param metadata
	 *            the meta data of the fields to be renamed.
	 * @param compilationUnitProvider
	 *            used for verifying references in excluded files.
	 * @return the created instance of {@link FieldsRenamingRule}.
	 * @throws StandaloneException
	 *             if the {@link CompilationUnitProvider} cannot decide if any
	 *             of the compilation units having at least one reference
	 *             belongs to the list of excluded files.
	 */
	public FieldsRenamingRule createRule(List<FieldMetaData> metadata,
			CompilationUnitProvider compilationUnitProvider) throws StandaloneException {

		List<FieldMetaData> filteredmetadata = new ArrayList<>();
		for (FieldMetaData md : metadata) {
			if (!hasReferencesToUnmodifiableFiles(md, compilationUnitProvider)) {
				filteredmetadata.add(md);
			}
		}
		return new FieldsRenamingRule(filteredmetadata, Collections.emptyList());

	}

	/**
	 * Makes use of the {@link FieldDeclarationVisitorWrapper} for finding the
	 * fields which need to be renamed and all their references.
	 * 
	 * @param selectedJavaElements
	 *            list of the {@link ICompilationUnit}s to be check for fields
	 *            that need to be renamed.
	 * @param options
	 *            the options for the {@link FieldDeclarationASTVisitor}
	 * @return list of the {@link FieldMetaData} resulting from the search
	 *         process.
	 * @throws StandaloneException
	 *             if the search did not succeed. See
	 *             {@link FieldDeclarationVisitorWrapper#prepareRenaming(List, Map)}
	 */
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
