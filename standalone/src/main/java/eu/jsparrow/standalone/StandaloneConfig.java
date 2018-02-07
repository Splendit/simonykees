package eu.jsparrow.standalone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;

/**
 * Class that contains all configuration needed to run headless version of
 * jSparrow Eclipse plugin.
 * 
 * @author Andreja Sambolec, Matthias Webhofer
 * @since 2.1.1
 */
public class StandaloneConfig {

	private static final Logger logger = LoggerFactory.getLogger(StandaloneConfig.class);

	private static final String ECLIPSE_MAVEN_NAME = "eclipse"; //$NON-NLS-1$
	private static final String ECLIPSE_CLEAN_GOAL = "clean"; //$NON-NLS-1$

	private String path;
	private String name;
	private String compilerCompliance;
	private String mavenHome;

	private boolean descriptionGenerated = false;
	private boolean cleanUpAlreadyDone = false;

	private IJavaProject javaProject = null;

	private List<ICompilationUnit> compUnits = new ArrayList<>();

	private IClasspathEntry[] oldEntries;

	private MavenInvoker mavenInovker;

	/**
	 * Constructor that calls setting up of the project and collecting the
	 * compilation units.
	 * 
	 * @param name
	 *            of the maven project
	 * @param path
	 *            to the folder of the project
	 * @throws MavenInvocationException
	 * @throws CoreException
	 */
	public StandaloneConfig(String name, String path, String compilerCompliance, String mavenHome)
			throws CoreException, MavenInvocationException {
		this(name, path, compilerCompliance, mavenHome, false);
	}

	public StandaloneConfig(String name, String path, String compilerCompliance, String mavenHome, boolean testMode)
			throws CoreException, MavenInvocationException {
		this.name = name;
		this.path = path;
		this.compilerCompliance = compilerCompliance;
		this.mavenHome = mavenHome;

		this.mavenInovker = getMavenInvoker();

		if (!testMode) {
			setUp();
		}
	}

	/**
	 * Create workspace and load project into it. If a .project file does not
	 * exist, one is generated with Java and maven natures.
	 * 
	 * @throws CoreException
	 * @throws MavenInvocationException
	 */
	public void setUp() throws CoreException, MavenInvocationException {
		IProjectDescription projectDescription = getProjectDescription();
		IProject project = this.initProject(projectDescription);
		this.initJavaProject(project);
		List<IClasspathEntry> mavenClasspathEntries = this.collectMavenDependenciesAsClasspathEntries();
		this.addToClasspath(mavenClasspathEntries);
		compUnits = getCompilationUnits();
	}

	/**
	 * if a project description already exists, it is already an eclipse project
	 * and the project description gets loaded. otherwise a project description
	 * for an eclipse project is created from the source code.
	 * 
	 * @return a project description for an eclipse project
	 * @throws CoreException
	 * @throws MavenInvocationException
	 */
	IProjectDescription getProjectDescription() throws CoreException, MavenInvocationException {
		IWorkspace workspace = getWorkspace();

		IProjectDescription description = null;
		File projectDescription = new File(getProjectDescriptionPath());

		if (!projectDescription.exists()) {
			logger.debug(Messages.StandaloneConfig_executeMavenEclipseEclipseGoal);
			mavenInovker.invoke(ECLIPSE_MAVEN_NAME, ECLIPSE_MAVEN_NAME, null);
			descriptionGenerated = true;
		}

		if (projectDescription.exists()) {
			logger.debug(Messages.StandaloneConfig_UseExistingProjectDescription);
			description = workspace.loadProjectDescription(new Path(getProjectDescriptionPath()));
		} else {
			logger.error(Messages.StandaloneConfig_projectDescriptionDoesNotExist);
			throw new IllegalStateException(Messages.StandaloneConfig_projectIsNotAnEclipseProjectAndCouldNotConvert);
		}

		return description;
	}

	/**
	 * this method creates and opens a new {@link IProject}
	 * 
	 * @param description
	 *            project description of the new project
	 * @return a newly created and opened project
	 * @throws CoreException
	 */
	IProject initProject(IProjectDescription description) throws CoreException {
		IWorkspace workspace = getWorkspace();

		IProject project = getProject(workspace, description.getName());
		project.create(description, new NullProgressMonitor());

		String loggerInfo = NLS.bind(Messages.StandaloneConfig_debug_createProject, description.getName());
		logger.debug(loggerInfo);

		project.open(new NullProgressMonitor());

		logger.debug(Messages.StandaloneConfig_debug_createdProject);

		return project;
	}

	/**
	 * takes an {@link IProject} and converts it in a java project of type
	 * {@link IJavaProject}. The java version is set here.
	 * 
	 * @param project
	 *            project to convert in a java project
	 * @return a java project
	 * @throws JavaModelException
	 */
	IJavaProject initJavaProject(IProject project) throws JavaModelException {
		logger.debug(Messages.StandaloneConfig_debug_createJavaProject);

		javaProject = createJavaProject(project);

		// set compiler compliance level from the project
		javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, compilerCompliance);
		javaProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, compilerCompliance);
		javaProject.setOption(JavaCore.COMPILER_SOURCE, compilerCompliance);

		String loggerInfo = NLS.bind(Messages.StandaloneConfig_CompilerComplianceSetTo, compilerCompliance);
		logger.debug(loggerInfo);

		javaProject.open(new NullProgressMonitor());

		return javaProject;
	}

	/**
	 * Method that creates {@link IJavaProject} from {@link IProject}. First it
	 * creates Java project, sets appropriate compiler compliance level and adds
	 * all maven dependencies to the classpath. Then collects all packages and
	 * {@link ICompilationUnit}s from them.
	 * 
	 * @param project
	 *            loaded into workspace from path
	 * @return list of {@link ICompilationUnit}s on project
	 * @throws JavaModelException
	 */
	List<ICompilationUnit> getCompilationUnits() throws JavaModelException {
		List<ICompilationUnit> units = new ArrayList<>();

		logger.debug(Messages.StandaloneConfig_collectCompilationUnits);
		List<IPackageFragment> packages = Arrays.asList(javaProject.getPackageFragments());
		for (IPackageFragment mypackage : packages) {
			if (mypackage.containsJavaResources() && 0 != mypackage.getCompilationUnits().length) {
				mypackage.open(new NullProgressMonitor());

				units.addAll(Arrays.asList(mypackage.getCompilationUnits()));
			}
		}
		return units;
	}

	/**
	 * Collects all jars from tmp folder in which maven plugin copied
	 * dependencies. Creates {@link IClasspathEntry} for each jar and returns
	 * them.
	 */
	List<IClasspathEntry> collectMavenDependenciesAsClasspathEntries() {
		logger.debug(Messages.StandaloneConfig_debug_collectDependencies);

		List<IClasspathEntry> collectedEntries = new ArrayList<>();

		File depsFolder = getMavenDependencyFolder();
		File[] listOfFiles = depsFolder.listFiles();

		if (null != listOfFiles) {
			logger.debug(Messages.StandaloneConfig_CreateClasspathEntriesForDependencies);
			for (File file : listOfFiles) {
				String jarPath = file.toString();
				IClasspathEntry jarEntry = createLibraryClasspathEntry(jarPath);
				collectedEntries.add(jarEntry);
			}
		}

		return collectedEntries;
	}

	/**
	 * Adds all classpath entries on classpath of received java project
	 * 
	 * @param javaProject
	 *            project to which classpath entries should be added
	 * @param classpathEntries
	 *            new entries to be added to classpath
	 * @throws JavaModelException
	 */
	void addToClasspath(List<IClasspathEntry> classpathEntries) throws JavaModelException {

		logger.debug(Messages.StandaloneConfig_ConfigureClasspath);

		if (!classpathEntries.isEmpty()) {
			oldEntries = javaProject.getRawClasspath();
			IClasspathEntry[] newEntries;

			if (oldEntries.length != 0) {
				Set<IClasspathEntry> set = new HashSet<>(Arrays.asList(oldEntries));
				set.addAll(classpathEntries);
				newEntries = set.toArray(new IClasspathEntry[set.size()]);
			} else {
				newEntries = classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]);
			}

			javaProject.setRawClasspath(newEntries, null);
		}
	}

	/**
	 * On stop, checks if .project file was programmatically generated and
	 * deletes it if it is.
	 * 
	 * @throws JavaModelException
	 * @throws IOException
	 * @throws MavenInvocationException
	 */
	public void cleanUp() throws JavaModelException, MavenInvocationException {
		if (!cleanUpAlreadyDone) {
			logger.debug(Messages.StandaloneConfig_debug_cleanUp);
			if (descriptionGenerated) {
				mavenInovker.invoke(ECLIPSE_MAVEN_NAME, ECLIPSE_CLEAN_GOAL, null);
			} else {
				revertClasspath();
			}
			cleanUpAlreadyDone = true;
		}
	}

	private void revertClasspath() throws JavaModelException {
		logger.debug(Messages.StandaloneConfig_RevertClasspath);
		if (null != oldEntries) {
			javaProject.setRawClasspath(oldEntries, null);
		}
	}

	protected IWorkspace getWorkspace() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		String loggerInfo = NLS.bind(Messages.StandaloneConfig_debug_createWorkspace, workspace.getRoot()
			.getLocation()
			.toString());
		logger.debug(loggerInfo);

		return workspace;
	}

	protected String getProjectDescriptionPath() {
		return path + File.separator + RefactorUtil.PROJECT_DESCRIPTION_CONSTANT;
	}

	protected String getPomFilePath() {
		return path + File.separator + "pom.xml"; //$NON-NLS-1$
	}

	protected IProject getProject(IWorkspace workspace, String name) {
		return workspace.getRoot()
			.getProject(name);
	}

	protected IJavaProject createJavaProject(IProject project) {
		return JavaCore.create(project);
	}

	protected File getMavenDependencyFolder() {
		return new File(
				System.getProperty(RefactorUtil.USER_DIR) + File.separator + RefactorUtil.DEPENDENCIES_FOLDER_CONSTANT);
	}

	protected IClasspathEntry createLibraryClasspathEntry(String jarPath) {
		return JavaCore.newLibraryEntry(new Path(jarPath), null, null);
	}

	/**
	 * Getter for IJavaProject
	 * 
	 * @return generated IJavaProject
	 */
	public IJavaProject getJavaProject() {
		return javaProject;
	}

	protected void setJavaProject(IJavaProject javaProject) {
		this.javaProject = javaProject;
	}

	protected boolean isDescriptionGenerated() {
		return descriptionGenerated;
	}
	
	protected MavenInvoker getMavenInvoker() {
		File mavenHomeFile = new File(this.mavenHome);
		File pomFile = new File(getPomFilePath());
		return new MavenInvoker(mavenHomeFile, pomFile);
	}

	/**
	 * Getter method for list of {@link ICompilationUnit}s collected from the
	 * project.
	 * 
	 * @return list of {@link ICompilationUnit}s collected from the project
	 */
	public List<ICompilationUnit> getCompUnits() {
		return compUnits;
	}
}
