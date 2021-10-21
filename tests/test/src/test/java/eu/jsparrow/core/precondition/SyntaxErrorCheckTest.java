package eu.jsparrow.core.precondition;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.rules.common.util.RefactoringUtil;

/**
 * TestSuite to check if a {@link ICompilationUnit} got Error markers in the
 * current Eclipse environment
 * 
 * @author Martin Huter
 * @since 1.2
 */
public class SyntaxErrorCheckTest {

	IJavaProject testproject = null;
	IPackageFragment packageFragment = null;

	@BeforeEach
	public void setUp() throws Exception {
		testproject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
		IPackageFragmentRoot root = RulesTestUtil.addSourceContainer(testproject, "/allRulesTestRoot");
		packageFragment = root.createPackageFragment("eu.jsparrow", true, null);
	}

	@AfterEach
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
		assertFalse(RefactoringUtil.checkForSyntaxErrors(testfile));
	}

	@Test
	public void fileWithOutErrorPresent() throws Exception {
		String source = "package eu.jsparrow.core.precondition;" + System.lineSeparator() + "import java.util.List;"
				+ System.lineSeparator() + "public class SyntaxErrorCheckTest2 {" + System.lineSeparator() + ""
				+ System.lineSeparator() + "List<String> testproject = null;" + System.lineSeparator() + ""
				+ System.lineSeparator() + "}" + System.lineSeparator();
		ICompilationUnit testfile = packageFragment.createCompilationUnit("SyntaxErrorCheckTest2.java", source, true,
				null);
		assertFalse(RefactoringUtil.checkForSyntaxErrors(testfile));
	}

}
