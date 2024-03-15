package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;

/**
 * @since 4.17.0
 */
public class JavaProjectsCollector {

	private static final Logger logger = LoggerFactory.getLogger(JavaProjectsCollector.class);

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
			} catch (Exception e) {
				logger.error("Exception for findJavaProjectWithPackage", e);
			}
			subMonitor.worked(1);
		}
	}

	private static Optional<JavaProjectWrapper> findJavaProjectWithPackage(IProject project) throws CoreException {
		IJavaProject javaProject = doCreateJavaProject(project).orElse(null);
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

	public List<JavaProjectWrapper> getJavaProjectWrapperList() {
		if (javaProjectWrapperList == null) {
			return Collections.emptyList();
		}
		return javaProjectWrapperList;
	}

	private static Optional<IJavaProject> doCreateJavaProject(IProject project)
			throws JavaModelException, CoreException {
		String logMsg;
		if (project.hasNature(JavaCore.NATURE_ID)) {
			logMsg = NLS.bind(Messages.MavenProjectImporter_creatingSingleJavaProject, project.getName());
			logger.debug(logMsg);

			IJavaProject javaProject = createJavaProject(project);

			if (!javaProject.isOpen()) {
				logMsg = NLS.bind(Messages.MavenProjectImporter_javaProjectNotOpened, project.getName());
				logger.debug(logMsg);
				javaProject.open(new NullProgressMonitor());
			}
			return Optional.ofNullable(javaProject);
		} else {
			logMsg = NLS.bind(Messages.MavenProjectImporter_skippingJavaProjectCreation, project.getName());
			logger.debug(logMsg);
		}

		return Optional.empty();
	}

	private static IJavaProject createJavaProject(IProject project) {
		return JavaCore.create(project);
	}

	public JavaProjectsCollector() {
		/*
		 * private default constructor hiding implicit public one.
		 */
	}

}
