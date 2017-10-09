package eu.jsparrow.standalone;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO SIM-103 add class description
 * 
 * @author Andreja Sambolec
 * @since 2.1.1
 */
public class TestStandalone {

	private String path;
	private String name;
	private String projectDependencies;

	private static final Logger logger = LoggerFactory.getLogger(TestStandalone.class);

	public TestStandalone(String name, String path, String projectDependencies) {
		try {
			this.name = name;
			this.path = path;
			this.projectDependencies = projectDependencies;
			setUpReal();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	IProjectDescription description = null;
	IJavaProject testProject = null;
	IPackageFragment packageFragment = null;
	IProject javaProject = null;
	IWorkspace workspace = null;

	private List<ICompilationUnit> compUnits = new ArrayList<>();

	public List<ICompilationUnit> getCompUnits() {
		return compUnits;
	}

	public void setUpReal() throws CoreException {

		workspace = ResourcesPlugin.getWorkspace();
		logger.info("Created workspace in " + workspace.getRoot().getFullPath()); //$NON-NLS-1$

		File projectDescription = new File(path + File.separator + ".project");
		if (!projectDescription.exists()) {
			description = workspace.newProjectDescription(name);

			String[] oldNatures = description.getNatureIds();
			String[] newNatures = Arrays.copyOf(oldNatures, oldNatures.length + 2);
			newNatures[newNatures.length - 2] = JavaCore.NATURE_ID;
			// add maven nature to the project
			newNatures[newNatures.length - 1] = "org.eclipse.m2e.core.maven2Nature";

			description.setNatureIds(newNatures);

			// ICommand[] commands = description.getBuildSpec();
			// List<ICommand> commandList = Arrays.asList(commands);
			// ICommand build = new BuildCommand();
			// // add maven builder to the project
			// build.setBuilderName("org.eclipse.m2e.core.maven2Builder");
			// List<ICommand> modList = new ArrayList<>(commandList);
			// modList.add(build);
			// description.setBuildSpec(modList.toArray(new ICommand[] {}));

			description.setLocation(new Path(path));
		} else {
			description = workspace.loadProjectDescription(new Path(path + File.separator + ".project")); //$NON-NLS-1$
			logger.info("Project description: " + description.getName()); //$NON-NLS-1$
		}

		javaProject = workspace.getRoot().getProject(description.getName());
		logger.info("Project description: " + description.getName()); //$NON-NLS-1$

		javaProject.create(description, new NullProgressMonitor());
		logger.info("Create project from description: " + description.getName()); //$NON-NLS-1$

		javaProject.open(new NullProgressMonitor());
		logger.info("Open java project: " + javaProject.getName()); //$NON-NLS-1$

		compUnits = getUnit(javaProject);

		logger.info("Created project"); //$NON-NLS-1$
	}

	public List<ICompilationUnit> getUnit(IProject javaProject) throws CoreException {
		List<IPackageFragment> packages = new ArrayList<>();
		List<ICompilationUnit> units = new ArrayList<>();

		logger.info("CREATING TEST PROJECT");

		try {
			logger.info("CATCHING PACKAGES");
			testProject = JavaCore.create(javaProject);
			String compilerCompliance = testProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
			logger.info("Project compiler compliance: " + compilerCompliance);
			testProject.setOption(JavaCore.COMPILER_COMPLIANCE, compilerCompliance);
			testProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, compilerCompliance);
			testProject.setOption(JavaCore.COMPILER_SOURCE, compilerCompliance);

			logger.info("TEST PROJECT: " + testProject);
			try {
				testProject.open(null);
			} catch (JavaModelException e) {
				logger.error(e.getMessage(), e);
				return new ArrayList<>();
			}

			File depsFolder = new File(System.getProperty("user.dir") + File.separator + "deps");
			File[] listOfFiles = depsFolder.listFiles();
			IClasspathEntry[] rawClasspath = testProject.getRawClasspath();
			List list = new LinkedList(java.util.Arrays.asList(rawClasspath));

			for (File file : listOfFiles) {
				String jarPath = file.toString();
				logger.info("JAR: " + jarPath);
				boolean isAlreadyAdded = false;
				for (IClasspathEntry cpe : rawClasspath) {
					isAlreadyAdded = cpe.getPath().toOSString().equals(jarPath);
					if (isAlreadyAdded)
						break;
				}
				if (!isAlreadyAdded) {
					IClasspathEntry jarEntry = JavaCore.newLibraryEntry(new Path(jarPath), null, null);
					list.add(jarEntry);
				}

			}
			IClasspathEntry[] newClasspath = (IClasspathEntry[]) list.toArray(new IClasspathEntry[0]);
			testProject.setRawClasspath(newClasspath, new NullProgressMonitor());

//			List<IClasspathEntry> collectedEntries = new ArrayList<>();
//			Arrays.asList(projectDependencies.split(";")).stream().forEach(entry -> {
//				String[] classpathValues = entry.split("/");
//				try {
//					collectedEntries.add(MavenUtil.generateMavenEntryFromDepedencyString(classpathValues[0],
//							classpathValues[1], classpathValues[2]));
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			});
//			try {
//
//				MavenUtil.addToClasspath(testProject, collectedEntries);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

			logger.info("TEST PROJECT PACKAGE FRAGMENTS: " + testProject.getPackageFragments());
			packages = Arrays.asList(testProject.getPackageFragments());

			for (IPackageFragment mypackage : packages) {
				if (mypackage.containsJavaResources() && 0 != mypackage.getCompilationUnits().length) {
					mypackage.open(new NullProgressMonitor());

					units.addAll(Arrays.asList(mypackage.getCompilationUnits()));
				}
			}
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), e);
		}
		return units;
	}

}
