package eu.jsparrow.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.core.rule.impl.ReplaceJUnit4CategoryWithJupiterTagRule;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Base class for Rule Tests.
 * 
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa, Andreja Sambolec,
 *         Matthias Webhofer
 * @since 0.9.2
 */
public abstract class AbstractRulesTest {

	private static final String UTILITY_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/utilities"; //$NON-NLS-1$
	protected String packageString = "eu.jsparrow.sample.preRule"; //$NON-NLS-1$

	protected static IPackageFragmentRoot root = null;

	protected static String javaVersion = JavaCore.VERSION_1_8;

	protected static List<RefactoringRule> rulesList = new ArrayList<>();

	public AbstractRulesTest() {
		super();
	}

	@BeforeAll
	public static void classSetUp() throws Exception {
		if (root == null) {
			root = createRootPackageFragment();
		}
		rulesList = new ArrayList<>();
		List<RefactoringRule> allRules = RulesContainer.getAllRules(false)
			.stream()
			/*
			 * we cannot apply Local Variable Type Inference rule until we
			 * upgrade to java 10.
			 */
			.filter(r -> JavaCore.compareJavaVersions(JavaCore.VERSION_1_8, r.getRequiredJavaVersion()) >= 0)
			.collect(Collectors.toList());

		StandardLoggerRule standardLoggerRule = new StandardLoggerRule();
		Map<String, String> options = standardLoggerRule.getDefaultOptions();
		options.put("new-logging-statement", "error"); //$NON-NLS-1$ //$NON-NLS-2$
		options.put("system-out-print-exception", "error"); //$NON-NLS-1$ //$NON-NLS-2$
		standardLoggerRule.activateOptions(options);
		rulesList.add(standardLoggerRule);
		// FIXME
		rulesList.add(new ReplaceJUnit4CategoryWithJupiterTagRule());
		rulesList.addAll(allRules);
	}

	@AfterAll
	public static void classTearDown() {
		root = null;
		javaVersion = JavaCore.VERSION_1_8;
		rulesList = new ArrayList<>();
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

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(postRuleDirectory),
				RulesTestUtil.RULE_SUFFIX)) {
			for (Path postRulePath : directoryStream) {
				Path preRulePath = Paths.get(RulesTestUtil.PRERULE_DIRECTORY, postRulePath.getFileName()
					.toString());
				data.add(new Object[] { preRulePath.getFileName()
					.toString(), preRulePath, postRulePath });
			}
		}

		return data;
	}

	public static List<Path> loadUtilityClasses(String utilityDirectory) throws IOException {
		List<Path> data = new ArrayList<>();

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(utilityDirectory), "*.java")) { //$NON-NLS-1$
			for (Path utilityPath : directoryStream) {
				data.add(utilityPath);
			}
		}

		return data;
	}

	protected String processFile(String fileName, String content, List<RefactoringRule> rules) throws Exception {

		IPackageFragment packageFragment = root.createPackageFragment(packageString, true, null);

		ICompilationUnit compilationUnit = packageFragment.createCompilationUnit(fileName, content, true, null);

		List<ICompilationUnit> compilationUnits = new ArrayList<>();
		compilationUnits.add(compilationUnit);

		RefactoringPipeline refactoringPipeline = new RefactoringPipeline(rules);

		/*
		 * A default progress monitor implementation, used just for testing
		 * purposes
		 */
		IProgressMonitor monitor = new NullProgressMonitor();

		rules.stream()
			.forEach(rule -> rule.calculateEnabledForProject(packageFragment.getJavaProject()));

		refactoringPipeline.prepareRefactoring(compilationUnits, monitor);
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
		compilationUnitSource = StringUtils.replace(compilationUnitSource, getPreRulePackage(), postRulePackage);

		// TODO check if tabs and newlines make a difference
		assertEquals(expectedSource, compilationUnitSource);
	}

	protected String getPreRulePackage() {
		return RulesTestUtil.PRERULE_PACKAGE;
	}

	protected void setPrerulePackage(String prerulePackage) {
		packageString = prerulePackage;
	}

	protected List<VariableDeclarationFragment> findDeclarationsInAnonymousClass(
			List<CompilationUnit> compilationUntis) {
		AnonymousClassFieldsVisitor visitor = new AnonymousClassFieldsVisitor();
		for (CompilationUnit cu : compilationUntis) {
			cu.accept(visitor);
		}
		return visitor.getFieldsInAnonymousClasses();
	}

	public static IPackageFragmentRoot createRootPackageFragment() throws Exception {
		IPackageFragmentRoot root = RulesTestUtil.getPackageFragementRoot(javaVersion);
		String packageString = "eu.jsparrow.sample.utilities"; //$NON-NLS-1$
		IPackageFragment packageFragment = root.createPackageFragment(packageString, true, null);
		for (Path utilityPath : loadUtilityClasses(UTILITY_DIRECTORY)) {
			String utilityClassName = utilityPath.getFileName()
				.toString();
			String utilitySource = new String(Files.readAllBytes(utilityPath), StandardCharsets.UTF_8);
			packageFragment.createCompilationUnit(utilityClassName, utilitySource, true, null);
		}
		return root;
	}

	class AnonymousClassFieldsVisitor extends ASTVisitor {
		private List<VariableDeclarationFragment> fragments = new ArrayList<>();

		@Override
		public boolean visit(FieldDeclaration field) {
			if (ASTNode.ANONYMOUS_CLASS_DECLARATION == field.getParent()
				.getNodeType()) {
				fragments.addAll(ASTNodeUtil.convertToTypedList(field.fragments(), VariableDeclarationFragment.class));
			}
			return true;
		}

		public List<VariableDeclarationFragment> getFieldsInAnonymousClasses() {
			return this.fragments;
		}
	}
}