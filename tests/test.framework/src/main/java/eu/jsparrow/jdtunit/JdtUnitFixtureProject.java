package eu.jsparrow.jdtunit;

import java.util.HashMap;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;

import eu.jsparrow.jdtunit.util.JavaProjectBuilder;
import eu.jsparrow.jdtunit.util.PackageFragmentBuilder;

/**
 * Class for stubbing a fixture project with the possibility to add several
 * compilation units.
 *
 */
@SuppressWarnings({ "nls" })
public class JdtUnitFixtureProject {

	private static final String PROJECT_FIXTURE_NAME = "FixtureProject";
	private static final String PACKAGE_FIXTURE_NAME = "fixturepackage";

	private IJavaProject javaProject;
	private IPackageFragment packageFragment;

	protected final HashMap<String, String> options = new HashMap<>();

	private final HashMap<String, JdtUnitFixtureClass> classes = new HashMap<>();

	public JdtUnitFixtureProject() {
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
	}

	/**
	 * Creates the fixture project. Elements set up are:
	 * <ul>
	 * <li>A stub java project
	 * <li>A stub package within that project
	 * </ul>
	 * 
	 * @throws JdtUnitException
	 * 
	 * @throws Exception
	 */
	public void setUp() throws JdtUnitException {
		javaProject = new JavaProjectBuilder().name(PROJECT_FIXTURE_NAME)
			.options(options)
			.build();
		
		packageFragment = addPackageFragment(PACKAGE_FIXTURE_NAME);
	}

	/**
	 * Removes all classes from the stubbed project
	 * 
	 * @throws JavaModelException
	 */
	public void clear() throws JavaModelException {
		for (JdtUnitFixtureClass clazz : classes.values()) {
			clazz.delete();
		}

		classes.clear();
	}

	/**
	 * Removes the fixture by deleting the stubbed elements.
	 * 
	 * @throws CoreException
	 */
	public void tearDown() throws CoreException {
		javaProject.getProject()
			.delete(true, null);
	}

	/**
	 * Adds a new package fragment to the stubbed project
	 * 
	 * @param name
	 *            name of the new package
	 * @return
	 * @throws JdtUnitException
	 */
	public IPackageFragment addPackageFragment(String name) throws JdtUnitException {
		return new PackageFragmentBuilder(javaProject).setName(name)
			.build();
	}

	/**
	 * Adds a new compilation unit to the default stubbed package in the stubbed
	 * project
	 * 
	 * @param className
	 *            name of the new compilation unit
	 * @return
	 * @throws JdtUnitException
	 * @throws BadLocationException 
	 * @throws JavaModelException 
	 */
	public JdtUnitFixtureClass addCompilationUnit(String className) throws JdtUnitException, JavaModelException, BadLocationException {
		return addCompilationUnit(packageFragment, className);
	}

	/**
	 * Adds a new compilation unit to the specified package of the stubbed
	 * project
	 * 
	 * @param packageFragment
	 *            package for adding the compilation unit
	 * @param className
	 *            name of the new compilation unit
	 * @return
	 * @throws JdtUnitException
	 * @throws BadLocationException 
	 * @throws JavaModelException 
	 */
	public JdtUnitFixtureClass addCompilationUnit(IPackageFragment packageFragment, String className)
			throws JdtUnitException, JavaModelException, BadLocationException {
		JdtUnitFixtureClass clazz = new JdtUnitFixtureClass(this, packageFragment, className);
		classes.put(className, clazz);
		return clazz;
	}

	/**
	 * Returns the specified compilation unit, if it exists
	 * 
	 * @param className
	 *            name of the compilation unit
	 * @return
	 */
	public Optional<JdtUnitFixtureClass> getCompilationUnit(String className) {
		return Optional.ofNullable(classes.get(className));
	}

	public HashMap<String, String> getOptions() {
		return options;
	}

	public IJavaProject getJavaProject() {
		return javaProject;
	}
}
