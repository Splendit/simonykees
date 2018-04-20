package eu.jsparrow.core.renaming;

import static eu.jsparrow.core.renaming.RenamingTestHelper.applyRenamingRule;
import static eu.jsparrow.core.renaming.RenamingTestHelper.findFields;
import static eu.jsparrow.core.renaming.RenamingTestHelper.loadExpected;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.junit.Test;

import eu.jsparrow.core.AbstractRulesTest;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.renaming.FieldDeclarationASTVisitor;

@SuppressWarnings("nls")
public class PublicFieldsRenamingRuleTest {

	private static final String PRERULE_RENAMING_PACKAGE = "eu.jsparrow.sample.preRule.renaming";
	private static final String PRERULE_DIRECTORY = RulesTestUtil.PRERULE_DIRECTORY + "/renaming";

	@Test
	public void testTransformation_onlyPublicFields() throws Exception {

		String postRuleDirectory = RulesTestUtil.BASE_DIRECTORY
				+ "/postRule/renaming/publicFieldRenamingSkippedModifiers";
		String postRulePackage = "eu.jsparrow.sample.postRule.renaming.publicFieldRenamingSkippedModifiers";

		FieldDeclarationASTVisitor referencesVisitor = findFields(PRERULE_RENAMING_PACKAGE,
				PRERULE_DIRECTORY, true, false, false, false, false);
		IPackageFragmentRoot root = AbstractRulesTest.createRootPackageFragment();

		List<ICompilationUnit> compilationUnits = applyRenamingRule(referencesVisitor, root,
				PRERULE_RENAMING_PACKAGE);

		Map<String, String> expected = loadExpected(postRuleDirectory);

		RenamingTestHelper.assertMatch(expected, compilationUnits, PRERULE_RENAMING_PACKAGE, postRulePackage);
	}

	@Test
	public void testTransformation_allFields() throws Exception {

		String postRuleDirectory = RulesTestUtil.BASE_DIRECTORY + "/postRule/renaming/publicFieldRenaming";
		String postRulePackage = "eu.jsparrow.sample.postRule.renaming.publicFieldRenaming";

		FieldDeclarationASTVisitor referencesVisitor = findFields(PRERULE_RENAMING_PACKAGE,
				PRERULE_DIRECTORY, true, true, true, false, true);
		IPackageFragmentRoot root = AbstractRulesTest.createRootPackageFragment();

		List<ICompilationUnit> compilationUnits = applyRenamingRule(referencesVisitor, root,
				PRERULE_RENAMING_PACKAGE);

		Map<String, String> expected = loadExpected(postRuleDirectory);

		RenamingTestHelper.assertMatch(expected, compilationUnits, PRERULE_RENAMING_PACKAGE, postRulePackage);
	}

}
