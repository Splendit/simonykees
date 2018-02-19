package eu.jsparrow.core;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.renaming.FieldDeclarationASTVisitor;
import eu.jsparrow.core.visitor.renaming.FieldMetaData;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

@RunWith(Parameterized.class)
@SuppressWarnings("nls")
public class PublicFieldsRenamingSkippedModifiersTest extends AbstractRulesTest {

	private static final String RENAMING = "renaming";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/" + RENAMING
			+ "/publicFieldRenamingSkippedModifiers";
	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule." + RENAMING
			+ ".publicFieldRenamingSkippedModifiers";
	protected static final String PRERULE_RENAMING_PACKAGE_NAME = "eu.jsparrow.sample.preRule." + RENAMING;

	private Path path;
	private static FieldDeclarationASTVisitor referencesVisitor;

	public PublicFieldsRenamingSkippedModifiersTest(Path preRule) {
		this.path = preRule;
		List<FieldMetaData> metaData = referencesVisitor.getFieldMetaData();
		rulesList.add(new PublicFieldsRenamingRule(metaData, Collections.emptyList()));
	}

	public static List<Object[]> loadCompilationUnits() throws JavaModelException, IOException {

		IPackageFragment packageFragment = root.createPackageFragment(PRERULE_RENAMING_PACKAGE_NAME, true, null);
		String packagePath = RulesTestUtil.PRERULE_DIRECTORY + "/" + RENAMING;
		List<CompilationUnit> compilationUnits = PublicFieldsRenamingRuleTest.loadCompilationUnits(packageFragment,
				packagePath);

		referencesVisitor = new FieldDeclarationASTVisitor(new IJavaElement[] { packageFragment });
		referencesVisitor.setRenamePackageProtectedField(false);
		referencesVisitor.setRenameProtectedField(false);
		referencesVisitor.setRenamePrivateField(false);
		for (CompilationUnit compilationUnit : compilationUnits) {
			compilationUnit.accept(referencesVisitor);
		}

		return PublicFieldsRenamingRuleTest.collectPaths(referencesVisitor.getTargetIJavaElements());
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
