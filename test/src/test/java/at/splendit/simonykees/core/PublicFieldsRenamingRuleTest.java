package at.splendit.simonykees.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import at.splendit.simonykees.core.rule.impl.PublicFieldsRenamingRule;
import at.splendit.simonykees.core.util.RefactoringUtil;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.renaming.FieldDeclarationASTVisitor;
import at.splendit.simonykees.core.visitor.renaming.FieldMetadata;
import at.splendit.simonykees.core.visitor.renaming.PublicFieldsRenamingASTVisitor;

/**
 * 
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
@RunWith(Parameterized.class)
@SuppressWarnings("nls")
public class PublicFieldsRenamingRuleTest extends AbstractRulesTest {
	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.publicFieldRenaming";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/publicFieldRenaming";

	private String fileName;
	private Path preRule;
	private Path postRule;
	
	public PublicFieldsRenamingRuleTest(String fileName, Path preRule, Path postRule) {
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		
		
		String packageString = "at.splendit.simonykees.sample.preRule"; //$NON-NLS-1$
		IPackageFragment packageFragment;
		ICompilationUnit iCompilationUnit;
		try {
			packageFragment = root.createPackageFragment(packageString, true, null);
			iCompilationUnit = packageFragment.createCompilationUnit(fileName, new String(Files.readAllBytes(preRule), StandardCharsets.UTF_8), true, null);
			CompilationUnit compilationUnit = RefactoringUtil.parse(iCompilationUnit);
			FieldDeclarationASTVisitor referencesVisitor = new FieldDeclarationASTVisitor();
			compilationUnit.accept(referencesVisitor);
			
			List<FieldMetadata> metaData = referencesVisitor.getFieldMetadata();
			
			//FIXME: this will not work if there is more than one file name
			rulesList.add(new  PublicFieldsRenamingRule(PublicFieldsRenamingASTVisitor.class, metaData));
			
		} catch (JavaModelException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Parameters(name = "{index}: test file[{0}]")
	public static Collection<Object[]> data() throws Exception {
		return AbstractRulesTest.load(POSTRULE_DIRECTORY);
	}

	@Test
	public void testTransformation() throws Exception {
		super.testTransformation(postRule, preRule, fileName, POSTRULE_PACKAGE);
	}
}
