package eu.jsparrow.core.renaming;

import static eu.jsparrow.core.renaming.RenamingTestHelper.applyRenamingRule;
import static eu.jsparrow.core.renaming.RenamingTestHelper.assertMatch;
import static eu.jsparrow.core.renaming.RenamingTestHelper.calculateActual;
import static eu.jsparrow.core.renaming.RenamingTestHelper.findFieldsToBeRenamed;
import static eu.jsparrow.core.renaming.RenamingTestHelper.loadExpected;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.core.visitor.renaming.FieldDeclarationASTVisitor;

@SuppressWarnings("nls")
public class FieldsRenamingRuleTest {

	private static final String PRERULE_RENAMING_PACKAGE = "eu.jsparrow.sample.preRule.renaming";
	private static final String PRERULE_DIRECTORY = RulesTestUtil.PRERULE_DIRECTORY + "/renaming";

	@Test
	public void testTransformation_onlyPublicFields() throws Exception {

		String postRuleDirectory = RulesTestUtil.BASE_DIRECTORY
				+ "/postRule/renaming/publicFieldRenamingSkippedModifiers";
		String postRulePackage = "eu.jsparrow.sample.postRule.renaming.publicFieldRenamingSkippedModifiers";
		List<String> expected = new ArrayList<>(loadExpected(postRuleDirectory).values());
		FieldDeclarationASTVisitor referencesVisitor = findFieldsToBeRenamed(PRERULE_RENAMING_PACKAGE,
				PRERULE_DIRECTORY, true, false, false, false, false);

		List<ICompilationUnit> compilationUnits = applyRenamingRule(referencesVisitor);

		List<String> actual = calculateActual(compilationUnits, PRERULE_RENAMING_PACKAGE, postRulePackage);
		assertMatch(expected, actual);
	}

	@Test
	public void testTransformation_allFields() throws Exception {

		String postRuleDirectory = RulesTestUtil.BASE_DIRECTORY + "/postRule/renaming/publicFieldRenaming";
		String postRulePackage = "eu.jsparrow.sample.postRule.renaming.publicFieldRenaming";
		List<String> expectedList = new ArrayList<>(loadExpected(postRuleDirectory).values());
		FieldDeclarationASTVisitor referencesVisitor = findFieldsToBeRenamed(PRERULE_RENAMING_PACKAGE,
				PRERULE_DIRECTORY, true, true, true, false, true);

		List<ICompilationUnit> compilationUnits = applyRenamingRule(referencesVisitor);

		List<String> actual = calculateActual(compilationUnits, PRERULE_RENAMING_PACKAGE, postRulePackage);
		assertMatch(expectedList, actual);
	}

}
