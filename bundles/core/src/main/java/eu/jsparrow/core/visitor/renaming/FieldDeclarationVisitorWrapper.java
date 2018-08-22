package eu.jsparrow.core.visitor.renaming;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	public int prepareRenaming(List<ICompilationUnit> selectedJavaElements,
			Map<String, Boolean> options, SubMonitor child) {
		visitor.updateOptions(options);
		for (ICompilationUnit compilationUnit : selectedJavaElements) {
			visit(visitor, compilationUnit);

			if (child.isCanceled()) {
				return Status.CANCEL;
			} else {
				child.worked(1);
			}
		}
		return Status.OK;
	}
	
	public int prepareRenaming(List<ICompilationUnit> selectedJavaElements,
			Map<String, Boolean> options) {
		visitor.updateOptions(options);
		for (ICompilationUnit compilationUnit : selectedJavaElements) {
			int status = visit(visitor, compilationUnit);
			if(status == Status.WARNING) {
				return status;
			}
		}
		return Status.OK;
	}

	private int visit(FieldDeclarationASTVisitor visitor, ICompilationUnit compilationUnit) {
		if (!compilationUnit.getJavaProject()
			.equals(javaProject)) {
			return Status.WARNING;
		}
		CompilationUnit cu = RefactoringUtil.parse(compilationUnit);
		cu.accept(visitor);
		return Status.OK;
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
