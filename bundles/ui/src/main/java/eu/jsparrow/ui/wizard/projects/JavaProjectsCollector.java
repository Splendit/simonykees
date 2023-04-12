package eu.jsparrow.ui.wizard.projects;

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

/**
 * @since 4.17.0
 */
public class JavaProjectsCollector {

	public static List<JavaProjectNode> collectJavaProjectsNodes() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();

		return Arrays.stream(projects)
			.map(JavaProjectsCollector::findJavaProjectNode)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
	}

	private static Optional<JavaProjectNode> findJavaProjectNode(IProject project) {
		return findJavaProjectNature(project).map(JavaProjectNode::new);
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
