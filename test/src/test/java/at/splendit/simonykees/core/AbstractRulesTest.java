package at.splendit.simonykees.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;

import at.splendit.simonykees.core.refactorer.AbstractRefactorer;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa
 * @since 0.9.2
 */
public class AbstractRulesTest {

	public AbstractRulesTest() {
		super();
	}

	/**
	 * loads all pairs of Paths for the postRule domain defined by the
	 * postRuleDirectory to assure that only pairs are loaded that are defined
	 * in the realm of the postRuleDirectory.
	 * 
	 * @param postRuleDirectory
	 *            directory of the reference sources
	 * @return the object array list used for tests
	 * @throws IOException
	 *             if path could not be found junit test default
	 */
	protected static List<Object[]> load(String postRuleDirectory) throws IOException {
		List<Object[]> data = new ArrayList<>();
		for (Path postRulePath : Files.newDirectoryStream(Paths.get(postRuleDirectory), RulesTestUtil.RULE_SUFFIX)) {
			Path preRulePath = Paths.get(RulesTestUtil.PRERULE_DIRECTORY, postRulePath.getFileName().toString());
			data.add(new Object[] { preRulePath.getFileName().toString(), preRulePath, postRulePath });
		}
		return data;
	}

	protected String processFile(String fileName, String content,
			List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules) throws Exception {

		IPackageFragment packageFragment = RulesTestUtil.getPackageFragement();
		ICompilationUnit compilationUnit = packageFragment.createCompilationUnit(fileName, content, true, null);

		List<IJavaElement> javaElements = new ArrayList<>();
		javaElements.add(compilationUnit);

		AbstractRefactorer refactorer = new AbstractRefactorer(javaElements, rules) {
		};

		refactorer.prepareRefactoring();
		refactorer.doRefactoring();
		refactorer.commitRefactoring();

		return compilationUnit.getSource();
	}

}