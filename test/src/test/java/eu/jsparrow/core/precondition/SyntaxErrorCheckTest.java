package eu.jsparrow.core.precondition;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.core.util.RefactoringUtil;
import eu.jsparrow.core.util.RulesTestUtil;

/**
 * TestSuite to check if a {@link ICompilationUnit} got Error markers in the
 * current Eclipse environment
 * 
 * @author Martin Huter
 * @since 1.2
 */
@SuppressWarnings("nls")
public class SyntaxErrorCheckTest {

	IJavaProject testproject = null;
	IPackageFragment packageFragment = null;

	@Before
	public void setUp() throws Exception {
		testproject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
		IPackageFragmentRoot root = RulesTestUtil.addSourceContainer(testproject, "/allRulesTestRoot");

		RulesTestUtil.addToClasspath(testproject, RulesTestUtil.getClassPathEntries(root));

		packageFragment = root.createPackageFragment("at.splendit.simonykees", true, null);
	}

	@After
	public void tearDown() {
		testproject = null;
		packageFragment = null;
	}

	@Test
	public void fileWithErrorPresent() throws Exception {
		String source = "package eu.jsparrow.core.precondition;" + System.lineSeparator()
				+ "public class SyntaxErrorCheckTest2 {" + System.lineSeparator() + "" + System.lineSeparator()
				+ "List testproject = null;" + System.lineSeparator() + "" + System.lineSeparator() + ""
				+ System.lineSeparator() + "}" + System.lineSeparator();
		ICompilationUnit testfile = packageFragment.createCompilationUnit("SyntaxErrorCheckTest2.java", source, true,
				null);
		Assert.assertFalse(RefactoringUtil.checkForSyntaxErrors(testfile));
	}

	@Test
	public void fileWithOutErrorPresent() throws Exception {
		String source = "package eu.jsparrow.core.precondition;" + System.lineSeparator()
				+ "import java.util.List;" + System.lineSeparator() + "public class SyntaxErrorCheckTest2 {"
				+ System.lineSeparator() + "" + System.lineSeparator() + "List<String> testproject = null;"
				+ System.lineSeparator() + "" + System.lineSeparator() + "}" + System.lineSeparator();
		ICompilationUnit testfile = packageFragment.createCompilationUnit("SyntaxErrorCheckTest2.java", source, true,
				null);
		Assert.assertFalse(RefactoringUtil.checkForSyntaxErrors(testfile));
	}

}
