package eu.jsparrow.core.visitor.utils;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Constructs the scope for the Eclipse' search engine. We use it for finding
 * references of a field in external compilation units.
 * 
 * @since 4.8.0
 */
public class SearchScopeFactory {

	private static final Logger logger = LoggerFactory.getLogger(SearchScopeFactory.class);

	private SearchScopeFactory() {
		/*
		 * Hide default constructor.
		 */
	}

	public static IJavaElement[] createWorkspaceSearchScope() {

		List<IJavaProject> projectList = new ArrayList<>();
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
}
