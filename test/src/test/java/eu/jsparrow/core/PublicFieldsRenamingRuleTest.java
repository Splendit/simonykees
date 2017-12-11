package eu.jsparrow.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.impl.PublicFieldsRenamingRule;
import eu.jsparrow.core.util.RefactoringUtil;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.core.visitor.renaming.FieldDeclarationASTVisitor;
import eu.jsparrow.core.visitor.renaming.FieldMetaData;

/**
 * Testing the renaming of the public fields which are directly referenced
 * outside the class.
 * 
 * @author Ardit Ymeri
 * @since 2.3.0
 *
 */
@RunWith(Parameterized.class)
@SuppressWarnings("nls")
public class PublicFieldsRenamingRuleTest extends AbstractRulesTest {
	protected static final String RENAMING = "renaming";
	protected static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule." + RENAMING
			+ ".publicFieldRenaming";
	protected static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/" + RENAMING
			+ "/publicFieldRenaming";
	protected static final String PRERULE_RENAMING_PACKAGE_NAME = "eu.jsparrow.sample.preRule." + RENAMING;

	private Path path;
	private static List<FieldMetaData> metaData;
	private static List<FieldMetaData> todosMetaData;

	public PublicFieldsRenamingRuleTest(Path preRule) {
		this.path = preRule;
		rulesList.add(new PublicFieldsRenamingRule(metaData, todosMetaData));
	}

	public static List<Object[]> loadCompilationUnits() throws JavaModelException, IOException {

		IPackageFragment packageFragment = root.createPackageFragment(PRERULE_RENAMING_PACKAGE_NAME, true, null);
		List<CompilationUnit> compilationUnits = loadCompilationUnits(packageFragment,
				RulesTestUtil.PRERULE_DIRECTORY + "/" + RENAMING);

		/*
		 * Parse each iCompilationUnit and visit them with
		 * FieldDeclarationASTVisitor
		 */
		FieldDeclarationASTVisitor referencesVisitor = new FieldDeclarationASTVisitor(
				new IJavaElement[] { packageFragment });
		referencesVisitor.setAddTodo(true);
		for (CompilationUnit compilationUnit : compilationUnits) {
			compilationUnit.accept(referencesVisitor);
		}

		/*
		 * Store the references metaData and the paths of all compilation units
		 * having at least one declaration/reference to be renamed.
		 */
		metaData = referencesVisitor.getFieldMetaData();
		todosMetaData = referencesVisitor.getUnmodifiableFieldMetaData();

		return collectPaths(referencesVisitor.getTargetIJavaElements());
	}

	public static List<CompilationUnit> loadCompilationUnits(IPackageFragment packageFragment, String packagePath)
			throws IOException, JavaModelException {
		/*
		 * Load iCompilationUnits on the prerule.renaming package
		 */
		List<CompilationUnit> compilationUnits = new ArrayList<>();
		for (Path renamingPath : loadUtilityClasses(packagePath)) {
			String renamingClassName = renamingPath.getFileName()
				.toString();
			String renamingSource = new String(Files.readAllBytes(renamingPath), StandardCharsets.UTF_8);
			ICompilationUnit iCompilationUnit = packageFragment.createCompilationUnit(renamingClassName, renamingSource,
					true, null);
			compilationUnits.add(RefactoringUtil.parse(iCompilationUnit));
		}
		return compilationUnits;
	}

	public static List<Object[]> collectPaths(Set<ICompilationUnit> targetICUs) {
		return targetICUs.stream()
			.map(ICompilationUnit::getPath)
			.map(iPath -> new Path[] { Paths.get(iPath.toFile()
				.getPath()) })
			.collect(Collectors.toList());
	}

	@Parameters(name = "{index}: test file[{0}]")
	public static Collection<Object[]> data() throws Exception {
		classSetUp();
		return loadCompilationUnits();
	}

	@Test
	public void testTransformation() throws Exception {
		String fileName = path.getFileName()
			.toString();
		Path postRule = Paths.get(POSTRULE_DIRECTORY, fileName);
		Path preRule = Paths.get(RulesTestUtil.PRERULE_DIRECTORY, RENAMING, fileName);
		super.testTransformation(postRule, preRule, fileName, POSTRULE_PACKAGE);
	}

	@Override
	protected String processFile(String fileName, String content,
			List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules) throws Exception {
		setPrerulePackage(PRERULE_RENAMING_PACKAGE_NAME);
		return super.processFile(fileName, content, rules);
	}

	@Override
	protected String getPreRulePackage() {
		return super.getPreRulePackage() + "." + RENAMING;
	}
}
