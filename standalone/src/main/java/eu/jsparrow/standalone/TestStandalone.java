package eu.jsparrow.standalone;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
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

	private static final Logger logger = LoggerFactory.getLogger(TestStandalone.class);

	public TestStandalone(String path) {
		try {
			this.path = path;
			setUpReal();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	IProjectDescription description = null;
	IJavaProject testProject = null;
	IPackageFragment packageFragment = null;
	IProject javaProject = null;

	private List<ICompilationUnit> compUnits = new ArrayList<>();

	public List<ICompilationUnit> getCompUnits() {
		return compUnits;
	}

	public void setUpReal() throws CoreException {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		logger.info("Created workspace in " + workspace.getRoot().getFullPath()); //$NON-NLS-1$

		description = workspace
				.loadProjectDescription(new Path(path + File.separator + ".project")); //$NON-NLS-1$
		logger.info("Project description: " + description.getName()); //$NON-NLS-1$

		javaProject = workspace.getRoot().getProject(description.getName());
		logger.info("Project description: " + description.getName()); //$NON-NLS-1$

		javaProject.create(description, new NullProgressMonitor());
		logger.info("Create project from description: " + description.getName()); //$NON-NLS-1$

		javaProject.open(new NullProgressMonitor());
		logger.info("Open java project: " + javaProject.getName()); //$NON-NLS-1$

		compUnits = getUnit(javaProject);

		logger.info("Created project"); //$NON-NLS-1$
	}

	public List<ICompilationUnit> getUnit(IProject javaProject) {
		List<IPackageFragment> packages = new ArrayList<>();
		List<ICompilationUnit> units = new ArrayList<>();

		logger.info("CREATING TEST PROJECT");

		try {
			logger.info("CATCHING PACKAGES");
			testProject = JavaCore.create(javaProject);
			logger.info("TEST PROJECT: " + testProject);
			try {
				testProject.open(null);
			} catch (JavaModelException e) {
				logger.error(e.getMessage(), e);
				return new ArrayList<>();
			}
			logger.info("TEST PROJECT PACKAGE FRAGMENTS: " + testProject.getPackageFragments());
			packages = Arrays.asList(testProject.getPackageFragments());

			for (IPackageFragment mypackage : packages) {
				if (mypackage.containsJavaResources() && 0 != mypackage.getCompilationUnits().length) {
					mypackage.open(new NullProgressMonitor());

					units.addAll(Arrays.asList(mypackage.getCompilationUnits()));

					// USED TO AVOID OutOfMemory
					units.stream().forEach(unit -> {
						try {
							unit.open(new NullProgressMonitor());
						} catch (JavaModelException e) {
							logger.error(e.getMessage(), e);
						}
					});

					return units;
				}
			}
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), e);
		}
		units.stream().forEach(unit -> {
			try {
				unit.open(new NullProgressMonitor());
			} catch (JavaModelException e) {
				logger.error(e.getMessage(), e);
			}
		});

		return units;
	}
	
	public void clear() throws CoreException {
		javaProject.close(new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName()).delete(true, new NullProgressMonitor());
	}
}
