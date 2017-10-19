package eu.jsparrow.core;

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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.AbstractRefactoringRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa, Andreja Sambolec
 * @since 0.9.2
 */
public abstract class AbstractRulesTest {

	private static final String UTILITY_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/utilities"; //$NON-NLS-1$

	protected static IPackageFragmentRoot root = null;

	protected static String javaVersion = JavaCore.VERSION_1_8;

	protected List<AbstractRefactoringRule<? extends AbstractASTRewriteASTVisitor>> rulesList = new ArrayList<>();

	protected IJavaProject testproject;

	public AbstractRulesTest() {
		super();
	}

	@BeforeClass
	public static void classSetUp() throws Exception {
		if (root == null) {
			root = RulesTestUtil.getPackageFragementRoot(javaVersion);
			String packageString = "eu.jsparrow.sample.utilities"; //$NON-NLS-1$
			IPackageFragment packageFragment = root.createPackageFragment(packageString, true, null);
			for (Path utilityPath : loadUtilityClasses(UTILITY_DIRECTORY)) {
				String utilityClassName = utilityPath.getFileName().toString();
				String utilitySource = new String(Files.readAllBytes(utilityPath), StandardCharsets.UTF_8);
				packageFragment.createCompilationUnit(utilityClassName, utilitySource, true, null);
			}
		}
	}

	@AfterClass
	public static void classTearDown() throws Exception {
		root = null;
		javaVersion = JavaCore.VERSION_1_8;
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

	protected static List<Path> loadUtilityClasses(String utilityDirectory) throws IOException {
		List<Path> data = new ArrayList<>();
		for (Path utilityPath : Files.newDirectoryStream(Paths.get(utilityDirectory), "*.java")) { //$NON-NLS-1$
			data.add(utilityPath);
		}
		return data;
	}

	protected String processFile(String fileName, String content,
			List<AbstractRefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules) throws Exception {

		String packageString = "eu.jsparrow.sample.preRule"; //$NON-NLS-1$
		IPackageFragment packageFragment = root.createPackageFragment(packageString, true, null);

		ICompilationUnit compilationUnit = packageFragment.createCompilationUnit(fileName, content, true, null);

		List<IJavaElement> javaElements = new ArrayList<>();
		javaElements.add(compilationUnit);

		RefactoringPipeline refactoringPipeline = new RefactoringPipeline(rules, true);

		/*
		 * A default progress monitor implementation, used just for testing
		 * purposes
		 */
		IProgressMonitor monitor = new NullProgressMonitor();

		rules.stream().forEach(rule -> rule.calculateEnabledForProject(packageFragment.getJavaProject()));

		refactoringPipeline.prepareRefactoring(javaElements, monitor);
		refactoringPipeline.doRefactoring(monitor);
		refactoringPipeline.commitRefactoring();

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