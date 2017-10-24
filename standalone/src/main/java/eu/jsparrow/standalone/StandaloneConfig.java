package eu.jsparrow.standalone;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * @author Andreja Sambolec
 * @since 2.1.1
 */
public class StandaloneConfig {

	private static final Logger logger = LoggerFactory.getLogger(StandaloneConfig.class);

	private String path;
	private String name;

	private boolean descriptionGenerated = false;

	private IJavaProject javaProject = null;

	private List<ICompilationUnit> compUnits = new ArrayList<>();

	/**
	 * Constructor that calls setting up of the project and collecting the
	 * compilation units.
	 * 
	 * @param name
	 *            of the maven project
	 * @param path
	 *            to the folder of the project
	 */
	public StandaloneConfig(String name, String path) {
		try {
			this.name = name;
			this.path = path;
			setUp();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
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

	/**
	 * Create workspace and load project into it. If .project file does not
	 * exist, one is generated with Java and maven natures.
	 * 
	 * @throws CoreException
	 */
	public void setUp() throws CoreException {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		logger.debug(Messages.StandaloneConfig_debug_createWorkspace);

		IProjectDescription description = null;
		File projectDescription = new File(path + File.separator + Activator.PROJECT_DESCRIPTION_CONSTANT);
		if (!projectDescription.exists()) {
			description = workspace.newProjectDescription(name);

			String[] oldNatures = description.getNatureIds();
			String[] newNatures = Arrays.copyOf(oldNatures, oldNatures.length + 2);
			newNatures[newNatures.length - 2] = JavaCore.NATURE_ID;
			// add maven nature to the project
			newNatures[newNatures.length - 1] = Activator.MAVEN_NATURE_CONSTANT;

			description.setNatureIds(newNatures);

			description.setLocation(new Path(path));

			descriptionGenerated = true;
		} else {
			description = workspace
				.loadProjectDescription(new Path(path + File.separator + Activator.PROJECT_DESCRIPTION_CONSTANT));
		}

		IProject project = workspace.getRoot()
			.getProject(description.getName());
		project.create(description, new NullProgressMonitor());

		String loggerInfo = NLS.bind(Messages.StandaloneConfig_debug_createProject, description.getName());
		logger.debug(loggerInfo);

		project.open(new NullProgressMonitor());

		compUnits = getCompilationUnits(project);

		logger.debug(Messages.StandaloneConfig_debug_createdProject);
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
	public List<ICompilationUnit> getCompilationUnits(IProject project) throws JavaModelException {
		List<ICompilationUnit> units = new ArrayList<>();

		logger.debug(Messages.StandaloneConfig_debug_createJavaProject);

		javaProject = JavaCore.create(project);

		// set compiler compliance level from the project
		String compilerCompliance = javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, compilerCompliance);
		javaProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, compilerCompliance);
		javaProject.setOption(JavaCore.COMPILER_SOURCE, compilerCompliance);

		javaProject.open(new NullProgressMonitor());

		addMavenDependenciesToClasspath();

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
	 * dependencies. Creates {@link IClasspathEntry} for each jar and adds them
	 * all to project classpath.
	 */
	private void addMavenDependenciesToClasspath() {
		logger.debug(Messages.StandaloneConfig_debug_collectDependencies);
		File depsFolder = new File(
				System.getProperty(Activator.USER_DIR) + File.separator + Activator.DEPENDENCIES_FOLDER_CONSTANT);
		File[] listOfFiles = depsFolder.listFiles();
		List<IClasspathEntry> collectedEntries = new ArrayList<>();

		for (File file : listOfFiles) {
			String jarPath = file.toString();
			IClasspathEntry jarEntry = JavaCore.newLibraryEntry(new Path(jarPath), null, null);
			collectedEntries.add(jarEntry);
		}
		try {
			addToClasspath(javaProject, collectedEntries);
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), e);
		}
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
	public void addToClasspath(IJavaProject javaProject, List<IClasspathEntry> classpathEntries)
			throws JavaModelException {
		if (!classpathEntries.isEmpty()) {
			IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
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
	 * @return true if .project deleted, is not programmatically generated or
	 *         doesn't exist any more, false otherwise
	 */
	public boolean cleanUp() {
		logger.debug(Messages.StandaloneConfig_debug_cleanUp);
		if (descriptionGenerated) {
			File projectDescription = new File(path + File.separator + Activator.PROJECT_DESCRIPTION_CONSTANT);
			if (projectDescription.exists()) {
				return projectDescription.delete();
			} else {
				return true;
			}
		} else {
			return true;
		}
	}

	/**
	 * Getter for IJavaProject
	 * 
	 * @return generated IJavaProject
	 */
	public IJavaProject getJavaProject() {
		return javaProject;
	}
}
