package eu.jsparrow.independent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.project.IMavenProjectImportResult;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.LocalProjectScanner;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.independent.exceptions.MavenImportException;

/**
 * Helper for importing maven projects into the eclipse workspace.
 *
 * @since 3.3.0
 */
public class MavenProjectImporter {

	private static final Logger logger = LoggerFactory.getLogger(MavenProjectImporter.class);

	private EclipseProjectFileManager eclipseProjectFileManager;

	public MavenProjectImporter() {
		this.eclipseProjectFileManager = new EclipseProjectFileManager();
	}

	public MavenProjectImporter(EclipseProjectFileManager eclipseProjectFileManager) {
		this.eclipseProjectFileManager = eclipseProjectFileManager;
	}

	/**
	 * This method looks for maven projects in the given directories, imports
	 * them into the given workspace and creates {@link IJavaProject}s for the
	 * imported projects.
	 * 
	 * @param workspaceRoot
	 *            the root path to the workspace, into which the projects should
	 *            be imported
	 * @param folder
	 *            path to the directory, where the maven projects for importing
	 *            are located
	 * @return a list of {@link IJavaProject}s for the imported projects
	 * @throws MavenImportException
	 *             if something goes wrong with the import
	 */
	public List<IJavaProject> importProjects(File workspaceRoot, String folder) throws MavenImportException {
		return importProjects(workspaceRoot, Collections.singletonList(folder));
	}

	/**
	 * This method looks for maven projects in the given directories, imports
	 * them into the given workspace and creates {@link IJavaProject}s for the
	 * imported projects.
	 * 
	 * @param workspaceRoot
	 *            the root path to the workspace, into which the projects should
	 *            be imported
	 * @param folders
	 *            paths to the directories, where the maven projects for
	 *            importing are located
	 * @return a list of {@link IJavaProject}s for the imported projects
	 * @throws MavenImportException
	 *             if something goes wrong with the import
	 */
	public List<IJavaProject> importProjects(File workspaceRoot, List<String> folders) throws MavenImportException {

		String logMsg = NLS.bind(Messages.MavenProjectImporter_startImport, String.join("; ", folders), //$NON-NLS-1$
				workspaceRoot.getAbsolutePath());
		logger.debug(logMsg);

		try {
			List<MavenProjectInfo> projectInfos = findMavenProjects(workspaceRoot, folders);
			List<String> projectRootPaths = projectInfos.stream()
				.map(i -> i.getPomFile()
					.getParentFile()
					.getAbsolutePath())
				.collect(Collectors.toList());
			eclipseProjectFileManager.addProjects(projectRootPaths);
			eclipseProjectFileManager.backupExistingEclipseFiles();

			List<IProject> importedProjects = importMavenProjects(projectInfos);
			List<IJavaProject> importedJavaProjects = createJavaProjects(importedProjects);

			return importedJavaProjects;
		} catch (InterruptedException | CoreException | IOException e) {
			throw new MavenImportException(e.getMessage(), e);
		}
	}

	private List<MavenProjectInfo> findMavenProjects(File workspaceRoot, List<String> folders)
			throws InterruptedException {
		logger.debug(Messages.MavenProjectImporter_searchingMavenProjects);

		MavenModelManager modelManager = MavenPlugin.getMavenModelManager();

		LocalProjectScanner lps = getLocalProjectScanner(workspaceRoot, folders, false, modelManager);
		lps.run(new NullProgressMonitor());
		List<MavenProjectInfo> projects = lps.getProjects();

		logger.debug(Messages.MavenProjectImporter_collectingProjectInfo);
		return collectMavenProjectInfo(projects);
	}

	private List<MavenProjectInfo> collectMavenProjectInfo(Collection<MavenProjectInfo> input) {

		List<MavenProjectInfo> toRet = new ArrayList<>();
		for (MavenProjectInfo info : input) {
			toRet.add(info);
			toRet.addAll(collectMavenProjectInfo(info.getProjects()));
		}
		return toRet;
	}

	private List<IProject> importMavenProjects(List<MavenProjectInfo> projectInfos) throws CoreException {

		logger.debug(Messages.MavenProjectImporter_importingMavenProject);

		ProjectImportConfiguration projectImportConfig = new ProjectImportConfiguration();
		IProjectConfigurationManager projectConfigurationManager = getProjectConfigurationManager();
		NullProgressMonitor nullMonitor = new NullProgressMonitor();
		
		List<IMavenProjectImportResult> results = projectConfigurationManager.importProjects(projectInfos, projectImportConfig, nullMonitor);
		logger.debug(Messages.MavenProjectImporter_importedMavenProjectResults);
		for(IMavenProjectImportResult result : results) {
			MavenProjectInfo info = result.getMavenProjectInfo();
			String logMessage = NLS.bind(Messages.MavenProjectImporter_importedProjectModel, info.getModel());
			logger.debug(logMessage);
		}

		return results.stream()
			.map(IMavenProjectImportResult::getProject)
			.collect(Collectors.toList());
	}

	private List<IJavaProject> createJavaProjects(List<IProject> projects) throws MavenImportException {
		try {
			logger.debug(Messages.MavenProjectImporter_createingJavaProjects);
			List<IJavaProject> javaProjects = new LinkedList<>();
			for (IProject project : projects) {
				if (!project.isOpen()) {
					logger.debug(Messages.MavenProjectImporter_eclipseIProjectNotOpened, project.getName());
					project.open(new NullProgressMonitor());
				}
				doCreateJavaProject(project).ifPresent(javaProjects::add);
			}
			return javaProjects;
		} catch (CoreException e) {
			throw new MavenImportException("The maven project could not be imported!", e); //$NON-NLS-1$
		}
	}

	private Optional<IJavaProject> doCreateJavaProject(IProject project) throws CoreException {
		logProjectInfo(project);
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

	protected void logProjectInfo(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natureIds = description.getNatureIds();
		String projectNatures = Arrays.stream(natureIds).collect(Collectors.joining(",")); //$NON-NLS-1$
		String logMessage = NLS.bind(Messages.MavenProjectImporter_projectNatureIds, description.getName(), projectNatures);
		logger.debug(logMessage);
	}

	public void cleanUp() {
		eclipseProjectFileManager.revertEclipseProjectFiles();
	}

	protected LocalProjectScanner getLocalProjectScanner(File workspaceRoot, List<String> folders,
			boolean basedirRenameRequired, MavenModelManager modelManager) {
		return new LocalProjectScanner(workspaceRoot, folders, basedirRenameRequired, modelManager);
	}

	protected IProjectConfigurationManager getProjectConfigurationManager() {
		return MavenPlugin.getProjectConfigurationManager();
	}

	protected IJavaProject createJavaProject(IProject project) {
		return JavaCore.create(project);
	}
}
