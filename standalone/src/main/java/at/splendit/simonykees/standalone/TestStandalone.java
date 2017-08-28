package at.splendit.simonykees.standalone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

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

	static IJavaProject testproject = null;
	static IPackageFragment packageFragment = null;
	static // IFolder target = null;
	IWorkspace workspace;
	static List<ICompilationUnit> compUnits = new ArrayList<>();
	static IProjectDescription description;

	public static void setUp() throws Exception {
		testproject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
		System.out.println("tempTestproject: " + testproject);

		IPackageFragmentRoot root = RulesTestUtil.addSourceContainer(testproject, "/allRulesTestRoot");
		System.out.println("root: " + root);

		RulesTestUtil.addToClasspath(testproject, RulesTestUtil.getClassPathEntries(root));

		packageFragment = root.createPackageFragment("at.splendit.simonykees", true, null);
		System.out.println("packageFragment: " + packageFragment);

		String source = "package at.splendit.simonykees.core.precondition;" + System.lineSeparator()
				+ "import java.util.List;" + System.lineSeparator() + "public class SyntaxErrorCheckTest2 {"
				+ System.lineSeparator() + "" + System.lineSeparator()
				+ "public SyntaxErrorCheckTest2() { int i = 6; i= i+2; 	}" + System.lineSeparator() + ""
				+ System.lineSeparator() + "}" + System.lineSeparator();
		ICompilationUnit testfile = packageFragment.createCompilationUnit("SyntaxErrorCheckTest2.java", source, true,
				null);

		// IPath pathProj = new
		// Path("/home/andreja/workspaces/runtime-jSparrow/jfreechart-fse/src/main/java");
		// target = ((IFolder) tempTestproject).getFolder(pathProj);

	}

	public IJavaProject getTestproject() {
		return testproject;
	}

	public static List<ICompilationUnit> getCompUnits() {
		return compUnits;
	}

	public static void setUpReal() throws CoreException {
		BufferedReader br = null;
		FileReader fr = null;

		String path = "";

		try {

			String file = System.getProperty("user.home") + File.separator + "path.txt";

			System.out.println("file: " + file);

			// br = new BufferedReader(new FileReader(FILENAME));
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
		
		// final IProjectDescription description =
		// theWorkspace.loadProjectDescription(path);
		// IPath path = new Path(this.path.get(0));
		// IWorkspaceRoot workspaceRoot =
		// ResourcesPlugin.getWorkspace().getRoot();
		// IProject project = workspaceRoot.getProject(projectName);

		System.out.println("Created workspace");

		// IWorkspaceRoot workspaceRoot =
		// ResourcesPlugin.getWorkspace().getRoot();

		IPath pathProj = new Path("/home/andreja/workspaces/runtime-jSparrow/jfreechart-fse/");

		// IFolder folder = JavaCore.create(pathProj);
		// JavaCore.create(folder);
		// IJavaProject javaProject = createJavaProject(this.project.getName(),
		// "target");
		description = workspace.loadProjectDescription(new Path("/home/andreja/workspaces/runtime-jSparrow/jfreechart-fse/.project")); // (path)); //$NON-NLS-1$
		System.out.println("Project description: " + description.getName()); //$NON-NLS-1$

		// try {
		

//		final IProjectDescription copyDescription = workspace
//				.loadProjectDescription(new Path("./workspace/" + description.getName() + "/.project")); // (path)); //$NON-NLS-1$
		
		
		final IProject javaProject = workspace.getRoot().getProject(description.getName());
		javaProject.create(new NullProgressMonitor());

		copyAll(description.getName(), workspace.getRoot().getLocation());
		javaProject.open(new NullProgressMonitor());

		// final IProject javaProject =
		// workspace.getRoot().getProject(description.getName());
		System.out.println("Project IProject: " + javaProject.getName());
		// javaProject.create(description, null);
		// javaProject.open(null);

		// testproject =
		// JavaCore.create(workspace.getRoot().getProject(description.getName()));

		System.out.println(workspace.getRoot().getProjects());

		// javaProject.create(null);
		// javaProject.open(null);

		compUnits = getUnit(javaProject);

		System.out.println("Created project");

		// testproject = JavaCore.create(javaProject);
		// testproject.open(null);

		// System.out.println(testproject.getPath());
		// testproject.open(null);

		// testproject =
		// JavaCore.create(workspace.getRoot().getProject(description.getName()));
		// testproject.open(null);
		// } catch (JavaModelException e) {
		// System.out.println(e.getMessage());
		// }

		System.out.println("Created project");
	}

	private static void copyAll(String projectName, IPath iPath) {
		File source = new File("/home/andreja/workspaces/runtime-jSparrow/jfreechart-fse");
		try {
			FileUtils.copyDirectory(source, iPath.append(projectName).toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<ICompilationUnit> getUnit(IProject javaProject)  {
		List<IPackageFragment> packages = new ArrayList<>();
		List<ICompilationUnit> units = new ArrayList<>();

		System.out.println("CREATING TEST PROJECT");
		// TestStandalone test = new TestStandalone();
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
			// testProject.open(null);
			System.out.println("TEST PROJECT PACKAGE FRAGMENTS: " + testProject.getPackageFragments());
			packages = Arrays.asList(testProject.getPackageFragments());
			System.out.println("PACKAGES: " + packages);

			for (IPackageFragment mypackage : packages) {
				if (mypackage.containsJavaResources() && 0 != mypackage.getCompilationUnits().length) {
					mypackage.open(null);

					units = Arrays.asList(mypackage.getCompilationUnits());
					System.out.println(units.get(0).toString());
				}
			}

			units.get(0).open(null);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return units;

	}
}
