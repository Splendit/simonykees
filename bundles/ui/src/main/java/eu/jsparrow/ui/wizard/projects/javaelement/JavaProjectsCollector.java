package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @since 4.17.0
 */
public class JavaProjectsCollector {

	private List<JavaProjectWrapper> javaProjectWrapperList;

	public void collectJavaProjects(IProgressMonitor monitor) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();
		
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100)
				.setWorkRemaining(projects.length);
			subMonitor.setTaskName(""); //$NON-NLS-1$

		javaProjectWrapperList = new ArrayList<>();

		for (IProject project : projects) {
			subMonitor.subTask(project.getName());
			try {
				findJavaProjectWithPackage(project).ifPresent(javaProjectWrapperList::add);
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			subMonitor.worked(1);
		}
	}

	private static Optional<JavaProjectWrapper> findJavaProjectWithPackage(IProject project) throws JavaModelException {
		IJavaProject javaProject = findJavaProjectNature(project).orElse(null);
		if (javaProject == null) {
			return Optional.empty();
		}

		IPackageFragmentRoot[] packageFragmentRootArray = javaProject.getPackageFragmentRoots();
		for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRootArray) {
			if (JavaProjectWrapper.isPackageFragmentRootWithPackage(packageFragmentRoot)) {
				return Optional.of(new JavaProjectWrapper(javaProject));
			}
		}
		return Optional.empty();
	}

	private static Optional<IJavaProject> findJavaProjectNature(IProject project) {
		IProjectDescription projectDescription;
		try {
			projectDescription = project.getDescription();
		} catch (CoreException e) {
			e.printStackTrace();
			return Optional.empty();
		}
		String[] natureIds = projectDescription.getNatureIds();
		for (String natureID : natureIds) {
			IProjectNature projectNature;
			try {
				projectNature = project.getNature(natureID);
			} catch (CoreException e) {
				e.printStackTrace();
				return Optional.empty();
			}
			if (projectNature instanceof IJavaProject) {
				return Optional.of((IJavaProject) projectNature);
			}
		}
		return Optional.empty();
	}

	public List<JavaProjectWrapper> getJavaProjectWrapperList() {
		if(javaProjectWrapperList == null) {
			return Collections.emptyList();
		}
		return javaProjectWrapperList;
	}

	public JavaProjectsCollector() {
		/*
		 * private default constructor hiding implicit public one.
		 */
	}

}
