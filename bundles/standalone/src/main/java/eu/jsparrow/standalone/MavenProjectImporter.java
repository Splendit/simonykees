package eu.jsparrow.standalone;

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

import org.apache.maven.model.Model;
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
import eu.jsparrow.standalone.exceptions.MavenImportException;

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
			logMavenProjectInfos(projectInfos);
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

	private void logMavenProjectInfos(List<MavenProjectInfo> projectInfos) {
		for(MavenProjectInfo mavenProjectInfo : projectInfos) {
			Model model = mavenProjectInfo.getModel();
			int prefixLength = Math.min(model.toString().length() - 1, 200);
			String modelValue = model.toString().substring(0, prefixLength) + "..."; //$NON-NLS-1$
			logger.debug("Project model: {}", modelValue); //$NON-NLS-1$
			
			String pomPath = mavenProjectInfo.getPomFile().getPath();
			logger.debug("Pom path: {}", pomPath); //$NON-NLS-1$
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
		logger.debug("Maven project import results: "); //$NON-NLS-1$
		for(IMavenProjectImportResult result : results) {
			MavenProjectInfo info = result.getMavenProjectInfo();
			logger.debug("Project model: {}", info.getModel()); //$NON-NLS-1$
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
					logger.debug("The project {} is not opened. Opening the project.", project.getName()); //$NON-NLS-1$
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
				logger.debug("The Java Project {} is not opened. Opening the Java project.", project.getName()); //$NON-NLS-1$
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
		logger.debug("Project nature ids of {}: {}.", description.getName(), projectNatures); //$NON-NLS-1$
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
