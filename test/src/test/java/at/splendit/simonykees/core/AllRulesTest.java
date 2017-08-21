package at.splendit.simonykees.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.rule.impl.PublicFieldsRenamingRule;
import at.splendit.simonykees.core.rule.impl.standardLogger.StandardLoggerRule;
import at.splendit.simonykees.core.util.RefactoringUtil;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.renaming.FieldDeclarationASTVisitor;
import at.splendit.simonykees.core.visitor.renaming.FieldMetadata;
import at.splendit.simonykees.core.visitor.renaming.PublicFieldsRenamingASTVisitor;
import at.splendit.simonykees.core.visitor.semiAutomatic.StandardLoggerASTVisitor;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa, Ardit Ymeri
 * @since 0.9
 */
@SuppressWarnings("nls")
@RunWith(Parameterized.class)
public class AllRulesTest extends AbstractRulesTest {

	public static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.allRules";
	public static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/allRules";

	private String fileName;
	private Path preRule, postRule;

	public AllRulesTest(String fileName, Path preRule, Path postRule) {
		super();
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		
		StandardLoggerRule standardLoggerRule = new StandardLoggerRule(StandardLoggerASTVisitor.class);
		standardLoggerRule.activateDefaultOptions();
		rulesList.add(standardLoggerRule);
		
		rulesList.addAll(RulesContainer.getAllRules());
		
		List<PublicFieldsRenamingRule> fieldRenamingRules = preparePublicFieldrenaming();
		rulesList.addAll(fieldRenamingRules);
	}

	private List<PublicFieldsRenamingRule> preparePublicFieldrenaming() {
		String packageString = "at.splendit.simonykees.sample.preRule"; //$NON-NLS-1$
		IPackageFragment packageFragment;
		ICompilationUnit iCompilationUnit;
		List<PublicFieldsRenamingRule> rules = new ArrayList<>();
		try {
			packageFragment = root.createPackageFragment(packageString, true, null);
			iCompilationUnit = packageFragment.createCompilationUnit(fileName, new String(Files.readAllBytes(preRule), StandardCharsets.UTF_8), true, null);
			CompilationUnit compilationUnit = RefactoringUtil.parse(iCompilationUnit);
			FieldDeclarationASTVisitor referencesVisitor = new FieldDeclarationASTVisitor();
			compilationUnit.accept(referencesVisitor);
			
			List<FieldMetadata> metaData = referencesVisitor.getFieldMetadata();
			
			//FIXME: if a field is referenced in other classes, will be missed. 
			rules.add(new  PublicFieldsRenamingRule(PublicFieldsRenamingASTVisitor.class, metaData));
			
		} catch (JavaModelException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rules;
	}

	/**
	 * All files in the preRule package are matched with its corresponding
	 * allRules match. If an preRule File exists with no postRule complement
	 * there is an file not found exception raised. This assures that a postRule
	 * file for each preRule File exists in case of the allRules test
	 * 
	 * @return the object array list used for tests
	 * @throws Exception
	 *             junit test default
	 */
	@Parameters(name = "{index}: test file[{0}]")
	public static Collection<Object[]> data() throws Exception {
		List<Object[]> data = new ArrayList<>();
		for (Path preRulePath : Files.newDirectoryStream(Paths.get(RulesTestUtil.PRERULE_DIRECTORY),
				RulesTestUtil.RULE_SUFFIX)) {
			Path postRulePath = Paths.get(POSTRULE_DIRECTORY, preRulePath.getFileName().toString());
			data.add(new Object[] { preRulePath.getFileName().toString(), preRulePath, postRulePath });
		}
		return data;
	}
	
	@Test
	public void testTransformation() throws Exception {
		super.testTransformation(postRule, preRule, fileName, POSTRULE_PACKAGE);
	}
}
