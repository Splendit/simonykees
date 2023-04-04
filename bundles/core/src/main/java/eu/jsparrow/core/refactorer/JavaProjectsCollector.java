package eu.jsparrow.core.refactorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

public class JavaProjectsCollector {

	public static List<IJavaProject> collectJavaProjectsToRefactor() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();

		return Arrays.stream(projects)
			.map(JavaProjectsCollector::findJavaProjectNature)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
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

	public static List<IPackageFragmentRoot> collectSourcePackageFragmentRoots(IJavaProject javaProject)
			throws JavaModelException {
		IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
		List<IPackageFragmentRoot> sourcePackageFragmentRoots = new ArrayList<>();
		for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
			if (isSourcePackageFragmentRoot(packageFragmentRoot)) {
				sourcePackageFragmentRoots.add(packageFragmentRoot);
			}
		}
		return sourcePackageFragmentRoots;
	}

	private static boolean isSourcePackageFragmentRoot(IPackageFragmentRoot packageFragmentRoot)
			throws JavaModelException {

		return packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE &&
				!packageFragmentRoot.isExternal() &&
				!packageFragmentRoot.isArchive();
	}

	private JavaProjectsCollector() {
		/*
		 * private default constructor hiding implicit public one.
		 */
	}

}
