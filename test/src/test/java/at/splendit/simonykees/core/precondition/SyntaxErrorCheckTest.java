package at.splendit.simonykees.core.precondition;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.util.SimonykeesUtil;

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
		String source = "package at.splendit.simonykees.core.precondition;"+System.lineSeparator()
				+"public class SyntaxErrorCheckTest2 {"+System.lineSeparator()
				+""+System.lineSeparator()
				+"List testproject = null;"+System.lineSeparator()
				+""+System.lineSeparator()
				+""+System.lineSeparator()
				+"}"+System.lineSeparator()
				;
		ICompilationUnit testfile = packageFragment.createCompilationUnit("SyntaxErrorCheckTest2.java", source, true, null);
		Assert.assertFalse(SimonykeesUtil.checkForSyntaxErrors(testfile));
	}
	
	@Test
	public void fileWithOutErrorPresent() throws Exception {
		String source = "package at.splendit.simonykees.core.precondition;"+System.lineSeparator()
				+"import java.util.List;"+System.lineSeparator()
				+"public class SyntaxErrorCheckTest2 {"+System.lineSeparator()
				+""+System.lineSeparator()
				+"List<String> testproject = null;"+System.lineSeparator()
				+""+System.lineSeparator()
				+"}"+System.lineSeparator()
				;
		ICompilationUnit testfile = packageFragment.createCompilationUnit("SyntaxErrorCheckTest2.java", source, true, null);
		Assert.assertFalse(SimonykeesUtil.checkForSyntaxErrors(testfile));
	}

}
