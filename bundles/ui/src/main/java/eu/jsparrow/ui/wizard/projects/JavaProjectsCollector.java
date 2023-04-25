package eu.jsparrow.ui.wizard.projects;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import eu.jsparrow.ui.wizard.projects.javaelement.JavaProjectWrapper;

/**
 * @since 4.17.0
 */
public class JavaProjectsCollector {

	public static List<JavaProjectWrapper> collectJavaProjects() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();

		List<JavaProjectWrapper> javaProjectWrapperList = new ArrayList<>();

		for (IProject project : projects) {
			try {
				findJavaProjectWithPackage(project).ifPresent(javaProjectWrapperList::add);
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		return javaProjectWrapperList;

	}

	private static Optional<JavaProjectWrapper> findJavaProjectWithPackage(IProject project) throws JavaModelException {
		IJavaProject javaProject = findJavaProjectNature(project).orElse(null);
		if(javaProject == null) {
			return Optional.empty();
		}

		IPackageFragmentRoot[] packageFragmentRootArray = javaProject.getPackageFragmentRoots();
		for(IPackageFragmentRoot packageFragmentRoot : packageFragmentRootArray) {
			if(JavaProjectWrapper.isPackageFragmentRootWithPackage(packageFragmentRoot)) {
				return Optional.of(new JavaProjectWrapper(javaProject, packageFragmentRoot));
			}
		}
		

		// isPackageFragmentRootWithPackage
		// return javaProject.map(JavaProjectWrapper::new);

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

	private JavaProjectsCollector() {
		/*
		 * private default constructor hiding implicit public one.
		 */
	}

}
