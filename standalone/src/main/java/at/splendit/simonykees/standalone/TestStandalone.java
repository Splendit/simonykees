package at.splendit.simonykees.standalone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

/**
 * TODO SIM-103 add class description
 * 
 * @author Andreja Sambolec
 * @since 2.1.1
 */
public class TestStandalone {

	public TestStandalone() {
		try {
			// setUp();
			setUpReal();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	IJavaProject testproject = null;
	IPackageFragment packageFragment = null;
	// IFolder target = null;
	static IWorkspace workspace;
	static List<ICompilationUnit> compUnits = new ArrayList<>();
	static IProjectDescription description;

	public IJavaProject getTestproject() {
		return testproject;
	}

	public List<ICompilationUnit> getCompUnits() {
		return compUnits;
	}

	public static void setUpReal() throws CoreException, IllegalStateException, IOException {
		BufferedReader br = null;
		FileReader fr = null;
		
		String userHome = System.getProperty("user.home");
		File directory = new File(userHome + "/temp").getAbsoluteFile();
		if (directory.exists() || directory.mkdirs()) {
			System.setProperty("user.dir", directory.getAbsolutePath());
			System.out.println("Set user.dir to " + directory.getAbsolutePath());
		}

		String path = "";
		try {
			String file = System.getProperty("user.dir") + File.separator + "path.txt";
			System.out.println("file: " + file);

			fr = new FileReader(file);
			br = new BufferedReader(fr);

			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				System.out.println(sCurrentLine);
				path += sCurrentLine;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (fr != null)
					fr.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		workspace = ResourcesPlugin.getWorkspace();
		// org.eclipse.ui.internal.ide.actions.OpenWorkspaceAction.restart

		System.out.println("Created workspace in " + workspace.getRoot().getFullPath());

		description = workspace.loadProjectDescription(new Path(path + File.separator + ".project")); //$NON-NLS-1$

		System.out.println("Project description: " + description.getName()); //$NON-NLS-1$

		final IProject javaProject = workspace.getRoot().getProject(description.getName());
		System.out.println("Project description: " + description.getName()); //$NON-NLS-1$

		javaProject.create(description, new NullProgressMonitor());
		System.out.println("Create project from description: " + description.getName()); //$NON-NLS-1$

		javaProject.open(new NullProgressMonitor());
		System.out.println("Open java project."); //$NON-NLS-1$

		System.out.println(workspace.getRoot().getProjects());

		compUnits = getUnit(javaProject);

		System.out.println("Created project");
	}

	public static List<ICompilationUnit> getUnit(IProject javaProject) {
		List<IPackageFragment> packages = new ArrayList<>();
		List<ICompilationUnit> units = new ArrayList<>();

		System.out.println("CREATING TEST PROJECT");

		try {
			System.out.println("CATCHING PACKAGES");
			IJavaProject testProject = JavaCore.create(javaProject);
			System.out.println("TEST PROJECT: " + testProject);
			try {
				testProject.open(null);
			} catch (JavaModelException e) {
				System.out.println(e.getMessage());
				return new ArrayList<>();
			}
			System.out.println("TEST PROJECT PACKAGE FRAGMENTS: " + testProject.getPackageFragments());
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
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});

					return units;
				}
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		units.stream().forEach(unit -> {
			try {
				unit.open(new NullProgressMonitor());
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		return units;

	}
}
