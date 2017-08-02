package at.splendit.simonykees.core;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

public class TestStandalone {

	public TestStandalone() {
		try {
			setUp();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	IJavaProject testproject = null;
	IPackageFragment packageFragment = null;

	public void setUp() throws Exception {
		testproject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
		IPackageFragmentRoot root = RulesTestUtil.addSourceContainer(testproject, "/allRulesTestRoot");

		RulesTestUtil.addToClasspath(testproject, RulesTestUtil.getClassPathEntries(root));

		packageFragment = root.createPackageFragment("at.splendit.simonykees", true, null);

		String source = "package at.splendit.simonykees.core.precondition;" + System.lineSeparator()
				+ "import java.util.List;" + System.lineSeparator() + "public class SyntaxErrorCheckTest2 {"
				+ System.lineSeparator() + "" + System.lineSeparator() + "public SyntaxErrorCheckTest2() { int i = 6; i= i+2; 	}"
				+ System.lineSeparator() + "" + System.lineSeparator() + "}" + System.lineSeparator();
		ICompilationUnit testfile = packageFragment.createCompilationUnit("SyntaxErrorCheckTest2.java", source, true,
				null);

	}

	public IJavaProject getTestproject() {
		return testproject;
	}
}
