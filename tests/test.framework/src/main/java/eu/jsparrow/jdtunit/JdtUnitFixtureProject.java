package eu.jsparrow.jdtunit;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import eu.jsparrow.jdtunit.util.CompilationUnitBuilder;
import eu.jsparrow.jdtunit.util.JavaProjectBuilder;
import eu.jsparrow.jdtunit.util.PackageFragmentBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

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
@SuppressWarnings({ "nls", "unchecked" })
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
	 * Creates the fixture. Elements set up are:
	 * <ul>
	 * <li>A stub java project
	 * <li>A stub package within that project
	 * <li>A stub file within that package
	 * <li>A class containing a single method within that file
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
	 * Removes the fixture by deleting the stubbed elements.
	 * 
	 * @throws CoreException
	 */
	public void tearDown() throws CoreException {
		javaProject.getProject()
			.delete(true, null);
	}

	public IPackageFragment addPackageFragment(String name) throws JdtUnitException {
		return new PackageFragmentBuilder(javaProject).setName(name)
			.build();
	}

	public JdtUnitFixtureClass addCompilationUnit(String className) throws JdtUnitException {
		return addCompilationUnit(packageFragment, className);
	}
	
	public JdtUnitFixtureClass addCompilationUnit(IPackageFragment packageFragment, String className) throws JdtUnitException {
		JdtUnitFixtureClass clazz = new JdtUnitFixtureClass(this, packageFragment, className);
		classes.put(className, clazz);
		return clazz;
	}

	public HashMap<String, String> getOptions() {
		return options;
	}

}
