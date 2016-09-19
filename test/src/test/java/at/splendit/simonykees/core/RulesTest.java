package at.splendit.simonykees.core;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.junit.Test;

import at.splendit.simonykees.core.refactorer.AbstractRefactorer;
import at.splendit.simonykees.core.rule.BracketsToControlRule;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.BracketsToControlASTVisitor;

public class RulesTest {
	
	public static final String POSTRULE_PACKAGE = "package at.splendit.simonykees.sample.postRule;";
	public static final String PRERULE_PACKAGE = "package at.splendit.simonykees.sample.preRule;";
	public static final String SAMPLE_DIRECTORY = "../sample/src/test/java/at/splendit/simonykees/sample/";
	public static final String PRERULE_DIRECTORY = SAMPLE_DIRECTORY + "preRule";
	public static final String POSTRULE_DIRECTORY = SAMPLE_DIRECTORY + "postRule";

	@Test
	public void allRulesTest() throws Exception {
		List<RefactoringRule<? extends ASTVisitor>> rules = RulesContainer.getAllRules();
		rules.forEach(rule -> System.out.println(rule.getName()));
		
		Files.newDirectoryStream(Paths.get(PRERULE_DIRECTORY), "*Rule.java").forEach(System.out::println);
		Files.newDirectoryStream(Paths.get(POSTRULE_DIRECTORY), "*Rule.java").forEach(System.out::println);
		
		for (Path preRulePath : Files.newDirectoryStream(Paths.get(PRERULE_DIRECTORY), "*Rule.java")) {
			
			Path postRulePath = Paths.get(POSTRULE_DIRECTORY, preRulePath.getFileName().toString());
			String expectedSource = new String(Files.readAllBytes(postRulePath));
			
			String content = new String(Files.readAllBytes(preRulePath));
			
			IPackageFragment packageFragment = RulesTestUtil.getPackageFragement();
			ICompilationUnit compilationUnit = packageFragment.createCompilationUnit(preRulePath.getFileName().toString(), content, true, null);
			
			List<IJavaElement> javaElements = new ArrayList<>();
			javaElements.add(compilationUnit);
			
			AbstractRefactorer refactorer = new AbstractRefactorer(javaElements, rules) {};
			
			refactorer.prepareRefactoring();
			refactorer.doRefactoring();
			refactorer.commitRefactoring();
			
			String compilationUnitSource = StringUtils.replace(compilationUnit.getSource(), PRERULE_PACKAGE, POSTRULE_PACKAGE);
			
			assertEquals(expectedSource, compilationUnitSource);
		}
		
	}
	
	@Test
	public void bracketsToControlRuleTest() throws Exception {
		Path preRulePath = Paths.get(PRERULE_DIRECTORY, "BracketsToControlRule.java");
		String preRuleSource = new String(Files.readAllBytes(preRulePath));
		
		Path postRulePath = Paths.get(POSTRULE_DIRECTORY, "BracketsToControlRule.java");
		String postRuleSource = new String(Files.readAllBytes(postRulePath));
		
		IPackageFragment packageFragment = RulesTestUtil.getPackageFragement();
		ICompilationUnit compilationUnit = packageFragment.createCompilationUnit(preRulePath.getFileName().toString(), preRuleSource, true, null);
		
		List<IJavaElement> javaElements = new ArrayList<>();
		javaElements.add(compilationUnit);
		
		List<RefactoringRule<? extends ASTVisitor>> rules = new ArrayList<>();
		rules.add(new BracketsToControlRule(BracketsToControlASTVisitor.class));
		
		AbstractRefactorer refactorer = new AbstractRefactorer(javaElements, rules) {};
		
		refactorer.prepareRefactoring();
		refactorer.doRefactoring();
		refactorer.commitRefactoring();
		
		String compilationUnitSource = StringUtils.replace(compilationUnit.getSource(), PRERULE_PACKAGE, POSTRULE_PACKAGE);
		
		assertEquals(postRuleSource, compilationUnitSource);
	}
	
}
