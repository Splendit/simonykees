package at.splendit.simonykees.core;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa, Andreja Sambolec
 * @since 0.9.2
 */
public abstract class AbstractRulesTest {

	protected List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rulesList = new ArrayList<>();

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

		/*
		 * A default progress monitor implementation, used just for testing
		 * purposes
		 */
		IProgressMonitor monitor = new NullProgressMonitor();

		refactorer.prepareRefactoring(monitor);
		refactorer.doRefactoring(monitor);
		refactorer.commitRefactoring();

		return compilationUnit.getSource();
	}

	protected void testTransformation(Path postRule, Path preRule, String fileName, String postRulePackage)
			throws Exception {
		String expectedSource = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		String content = new String(Files.readAllBytes(preRule), StandardCharsets.UTF_8);

		String compilationUnitSource = processFile(fileName, content, rulesList);

		// Replace the package for comparison
		compilationUnitSource = StringUtils.replace(compilationUnitSource, RulesTestUtil.PRERULE_PACKAGE,
				postRulePackage);

		// TODO check if tabs and newlines make a difference
		assertEquals(expectedSource, compilationUnitSource);
	}
}