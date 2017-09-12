package at.splendit.simonykees.core;

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

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.impl.PublicFieldsRenamingRule;
import at.splendit.simonykees.core.util.RefactoringUtil;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.core.visitor.renaming.FieldDeclarationASTVisitor;
import at.splendit.simonykees.core.visitor.renaming.FieldMetadata;
import at.splendit.simonykees.core.visitor.renaming.PublicFieldsRenamingASTVisitor;

/**
 * Testing the renaming of the public fields which are directly referenced
 * outside the class. 
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
@RunWith(Parameterized.class)
@SuppressWarnings("nls")
public class PublicFieldsRenamingRuleTest extends AbstractRulesTest {
	private static final String RENAMING = "renaming";
	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.publicFieldRenaming";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/publicFieldRenaming";
	private static final String PRERULE_RENAMING_PACKAGE_NAME = "at.splendit.simonykees.sample.preRule." + RENAMING;

	private Path path;
	private static List<FieldMetadata> metaData;
	private static List<FieldMetadata> todosMetaData;

	public PublicFieldsRenamingRuleTest(Path preRule) {
		this.path = preRule;
		rulesList.add(new PublicFieldsRenamingRule(PublicFieldsRenamingASTVisitor.class, metaData, todosMetaData));
	}

	public static List<Object[]> loadCompilationUnits() throws JavaModelException, IOException {

		IPackageFragment packageFragment = root.createPackageFragment(PRERULE_RENAMING_PACKAGE_NAME, true, null);
		/*
		 * Load iCompilationUnits on the prerule.renaming package
		 */
		List<ICompilationUnit> iCompilationUnits = new ArrayList<>();
		for (Path renamingPath : loadUtilityClasses(RulesTestUtil.PRERULE_DIRECTORY + "/" + RENAMING)) {
			String renamingClassName = renamingPath.getFileName().toString();
			String renamingSource = new String(Files.readAllBytes(renamingPath), StandardCharsets.UTF_8);
			ICompilationUnit iCompilationUnit = packageFragment.createCompilationUnit(renamingClassName, renamingSource,
					true, null);
			iCompilationUnits.add(iCompilationUnit);
		}

		/*
		 * Parse each iCompilationUnit and visit them with FieldDeclarationASTVisitor
		 */
		FieldDeclarationASTVisitor referencesVisitor = new FieldDeclarationASTVisitor(
				new IJavaElement[] { packageFragment });
		referencesVisitor.setAddTodo(true);
		for (ICompilationUnit iCompilationUnit : iCompilationUnits) {
			CompilationUnit compilationUnit = RefactoringUtil.parse(iCompilationUnit);
			compilationUnit.accept(referencesVisitor);
		}

		/*
		 * Store the references metadata and the paths of all compilation units
		 * having at least one declaration/reference to be renamed. 
		 */
		metaData = referencesVisitor.getFieldMetadata();
		todosMetaData = referencesVisitor.getUnmodifiableFieldMetadata();
		Set<IPath> iPaths = referencesVisitor.getTargetCompilationUnitPaths();
		
		return iPaths.stream().map(iPath -> new Path[] { Paths.get(iPath.toFile().getPath()) }).collect(Collectors.toList());
	}

	@Parameters(name = "{index}: test file[{0}]")
	public static Collection<Object[]> data() throws Exception {
		setUp();
		return loadCompilationUnits();
	}

	@Test
	public void testTransformation() throws Exception {
		String fileName = path.getFileName().toString();
		Path postRule = Paths.get(POSTRULE_DIRECTORY, RENAMING, fileName);
		Path preRule = Paths.get(RulesTestUtil.PRERULE_DIRECTORY, RENAMING, fileName);
		super.testTransformation(postRule, preRule, fileName, POSTRULE_PACKAGE);
	}

	@Override
	protected String processFile(String fileName, String content,
			List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules) throws Exception {

		return processFile(fileName, content, PRERULE_RENAMING_PACKAGE_NAME, rules);
	}
}
