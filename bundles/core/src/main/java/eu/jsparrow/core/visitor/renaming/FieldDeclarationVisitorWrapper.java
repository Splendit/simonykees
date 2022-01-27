package eu.jsparrow.core.visitor.renaming;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.util.RefactoringUtil;

/**
 * Provides functionality 
 * for visiting compilation units {@link FieldDeclarationASTVisitor} and retrieving 
 * the results. 
 * 
 * @author Ardit Ymeri
 * @since 2.3.1
 *
 */
public class FieldDeclarationVisitorWrapper {

	private static final Logger logger = LoggerFactory.getLogger(FieldDeclarationVisitorWrapper.class);

	public static final String SCOPE_PROJECT = Messages.RenameFieldsRuleWizardPageModel_scopeOption_project;

	private IJavaProject javaProject;
	private FieldDeclarationASTVisitor visitor;

	public FieldDeclarationVisitorWrapper(IJavaProject iProject, String modelSearchScope) {
		this.javaProject = iProject;
		IJavaElement[] scope = createSearchScope(modelSearchScope);
		visitor = new FieldDeclarationASTVisitor(scope);
	}

	private IJavaElement[] createSearchScope(String modelSearchScope) {

		if (SCOPE_PROJECT.equals(modelSearchScope)) {
			return new IJavaElement[] { javaProject };
		}

		List<IJavaProject> projectList = new LinkedList<>();
		try {
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace()
				.getRoot();
			IProject[] projects = workspaceRoot.getProjects();
			for (int i = 0; i < projects.length; i++) {
				IProject project = projects[i];
				if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
					projectList.add(JavaCore.create(project));
				}
			}
		} catch (CoreException e) {
			logger.error(e.getMessage(), e);
		}
		return projectList.toArray(new IJavaElement[0]);
	}

	/**
	 * Updates the options of the {@link FieldDeclarationASTVisitor} and uses it
	 * to visit each of the provided {@link ICompilationUnit}s.
	 * 
	 * @param selectedJavaElements
	 *            the {@link ICompilationUnit}s to be checked for fields that
	 *            need to be renamed.
	 * @param options
	 *            the configuration options for the field search
	 * @param child
	 *            a {@link SubMonitor} for indicating the progress of the search
	 *            process.
	 * @return {@link Status#OK} if the search finished successfully, or
	 *         {@link Status#CANCEL} if the {@link SubMonitor} is cancelled, or
	 *         {@link Status#WARNING} if an {@link ICompilationUnit} does not
	 *         belong to the current project.
	 */
	public int prepareRenaming(List<ICompilationUnit> selectedJavaElements, Map<String, Boolean> options,
			SubMonitor child) {
		visitor.updateOptions(options);
		for (ICompilationUnit compilationUnit : selectedJavaElements) {
			int status = visit(visitor, compilationUnit);
			if (status != IStatus.OK) {
				return status;
			}

			if (child.isCanceled()) {
				return IStatus.CANCEL;
			} else {
				child.worked(1);
			}
		}
		return IStatus.OK;
	}

	/**
	 * Updates the options of the {@link FieldDeclarationASTVisitor} and uses it
	 * to visit each of the provided {@link ICompilationUnit}s.
	 * 
	 * @param selectedJavaElements
	 *            the {@link ICompilationUnit}s to be checked for fields that
	 *            need to be renamed.
	 * @param options
	 *            the configuration options for the field search
	 * @return {@link Status#OK} if the search finished successfully, or
	 *         {@link Status#WARNING} if an {@link ICompilationUnit} does not
	 *         belong to the current project.
	 */
	public int prepareRenaming(List<ICompilationUnit> selectedJavaElements, Map<String, Boolean> options) {
		visitor.updateOptions(options);
		for (ICompilationUnit compilationUnit : selectedJavaElements) {
			int status = visit(visitor, compilationUnit);
			if (status == IStatus.WARNING) {
				return status;
			}
		}
		return IStatus.OK;
	}

	private int visit(FieldDeclarationASTVisitor visitor, ICompilationUnit compilationUnit) {
		if (!compilationUnit.getJavaProject()
			.equals(javaProject)) {
			return IStatus.WARNING;
		}
		CompilationUnit cu = RefactoringUtil.parse(compilationUnit);
		cu.accept(visitor);
		return IStatus.OK;
	}

	public List<FieldMetaData> getFieldsMetaData() {
		return visitor.getFieldMetaData();
	}

	public List<FieldMetaData> getUnmodifiableFieldsMetaData() {
		return visitor.getUnmodifiableFieldMetaData();
	}

	public Set<ICompilationUnit> getTargetIJavaElements() {
		return visitor.getTargetIJavaElements();
	}

}
