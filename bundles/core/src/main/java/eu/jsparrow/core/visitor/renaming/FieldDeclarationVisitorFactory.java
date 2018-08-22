package eu.jsparrow.core.visitor.renaming;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
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
 * A factory for creating instances of {@link FieldDeclarationASTVisitor} and
 * for visiting the list of compilation units with it.
 * 
 * @author Ardit Ymeri
 * @since 2.3.1
 *
 */
public class FieldDeclarationVisitorFactory {

	private static final Logger logger = LoggerFactory.getLogger(FieldDeclarationVisitorFactory.class);

	public static final String SCOPE_PROJECT = Messages.RenameFieldsRuleWizardPageModel_scopeOption_project;

	private FieldDeclarationVisitorFactory() {
		/*
		 * Hiding default constructor
		 */
	}

	public static FieldDeclarationASTVisitor visitorFactory(IJavaProject iProject, Map<String, Boolean> options,
			String modelSearchScope) {

		IJavaElement[] scope;
		if (SCOPE_PROJECT.equals(modelSearchScope)) {
			scope = new IJavaElement[] { iProject };

		} else {
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
			scope = projectList.toArray(new IJavaElement[0]);
		}

		FieldDeclarationASTVisitor visitor = new FieldDeclarationASTVisitor(scope);
		visitor.updateOptions(options);

		return visitor;
	}

	public static int prepareRenaming(List<ICompilationUnit> selectedJavaElements, IJavaProject selectedJavaProjekt,
			FieldDeclarationASTVisitor visitor, SubMonitor child) {
		for (ICompilationUnit compilationUnit : selectedJavaElements) {
			if (!compilationUnit.getJavaProject()
				.equals(selectedJavaProjekt)) {
				return Status.WARNING;
			}

			CompilationUnit cu = RefactoringUtil.parse(compilationUnit);
			cu.accept(visitor);

			if (child.isCanceled()) {
				return Status.CANCEL;
			} else {
				child.worked(1);
			}
		}

		return Status.OK;
	}
	
	public static int prepareRenaming(List<ICompilationUnit> selectedJavaElements, IJavaProject selectedJavaProjekt,
			FieldDeclarationASTVisitor visitor) {
		for (ICompilationUnit compilationUnit : selectedJavaElements) {
			if (!compilationUnit.getJavaProject()
				.equals(selectedJavaProjekt)) {
				return Status.WARNING;
			}

			CompilationUnit cu = RefactoringUtil.parse(compilationUnit);
			cu.accept(visitor);
		}

		return Status.OK;
	}

}
