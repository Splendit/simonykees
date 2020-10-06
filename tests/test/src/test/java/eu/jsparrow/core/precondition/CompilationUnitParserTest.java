package eu.jsparrow.core.precondition;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.rules.common.util.RefactoringUtil;

/**
 * Parsing {@link ICompilationUnit} to {@link CompilationUnit}.
 * 
 * @since 3.3.0
 *
 */
public class CompilationUnitParserTest {
	IJavaProject testproject = null;
	IPackageFragment packageFragment = null;

	@BeforeEach
	public void setUp() throws Exception {
		testproject = RulesTestUtil.createJavaProject("compilationUnitParserProject", "bin");
		IPackageFragmentRoot root = RulesTestUtil.addSourceContainer(testproject, "/allRulesTestRoot");
		packageFragment = root.createPackageFragment("eu.jsparrow", true, null);
	}

	@Test
	public void parseICompilationUnit() throws Exception {
		String source = "package eu.jsparrow.core.precondition;" + System.lineSeparator() + "import java.util.List;"
				+ System.lineSeparator() + "public class CompilationUnitParserTest {" + System.lineSeparator()
				+ "	List<String> testproject = null;" + System.lineSeparator() + "}";
		ICompilationUnit testfile = packageFragment.createCompilationUnit("CompilationUnitParserTest.java", source,
				true, null);

		CompilationUnit parsedCompilationUnit = RefactoringUtil.parse(testfile);

		assertNotNull(parsedCompilationUnit);
		AST ast = parsedCompilationUnit.getAST();
		assertTrue(ast.hasResolvedBindings());
	}
}
