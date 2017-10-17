package eu.jsparrow.ui.test.jdtunit;

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * <p>
 * Fixture class that stubs a JDT compilation unit. Within that compilation unit
 * ASTNodes can be inserted and deleted. In order to get working type bindings
 * for any AST created within the stubbed compilation unit a full java project
 * is created in code.
 * </p>
 * 
 * @author Hans-Jörg Schrödl
 *
 */
@SuppressWarnings({ "nls"})
public class JdtUnitFixture {

	private static final String PROJECT_FIXTURE_NAME = "FixtureProject";

	private IProject project;

	private IJavaProject javaProject;

	private final HashMap<String, String> options = new HashMap<>();

	public JdtUnitFixture() {
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
	}

	/**
	 * Creates the fixture. Elements set up are:
	 * <ul>
	 * <li>A stub java project
	 * <li>A stub package within that project
	 * <li>A stub file within that package
	 * <li>A class containing a single method within that file
	 * </ul>
	 * 
	 * @throws Exception
	 */
	public void setUp() throws Exception {
		createJavaProject();


		
	}
	
	/**
	 * Removes the fixture by deleting the stubbed elements.
	 * 
	 * @throws CoreException
	 */
	public void tearDown() throws CoreException {
		project.delete(true, null);
	}

	private void createJavaProject() throws CoreException {

		project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_FIXTURE_NAME);
		project.create(null);
		project.open(null);

		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID });
		project.setDescription(description, null);

		javaProject = JavaCore.create(project);

		// build path is: project as source folder and JRE container
		IClasspathEntry[] cpentry = new IClasspathEntry[] { JavaCore.newSourceEntry(javaProject.getPath()),
				JavaRuntime.getDefaultJREContainerEntry() };
		javaProject.setRawClasspath(cpentry, javaProject.getPath(), null);
		javaProject.setOptions(options);
	}

	public IPackageFragment addPackageFragment(String name) throws JavaModelException {
		if(javaProject != null) {
			IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(project);
			return root.createPackageFragment(name, false, null);
		}
		return null;
	}
	
	public ICompilationUnit addCompilationUnit(IPackageFragment packageFragment, String name) throws JavaModelException {
		if(packageFragment != null) {
			return packageFragment.createCompilationUnit(name, "", false, null);
		}
		return null;
	}

	public IJavaProject getJavaProject() {
		return javaProject;
	}

	public void setJavaProject(IJavaProject javaProject) {
		this.javaProject = javaProject;
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}
}
