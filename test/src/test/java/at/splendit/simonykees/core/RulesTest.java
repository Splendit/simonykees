package at.splendit.simonykees.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.junit.Test;

import at.splendit.simonykees.core.refactorer.AbstractRefactorer;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.util.RulesTestUtil;

public class RulesTest {
	
	private static final String SAMPLE_DIRECTORY = "../sample/src/test/java/at/splendit/simonykees/sample/";
	private static final String PRERULE_DIRECTORY = SAMPLE_DIRECTORY + "preRule";
	private static final String POSTRULE_DIRECTORY = SAMPLE_DIRECTORY + "postRule";

	@Test
	public void allRulesTest() throws Exception {
		List<RefactoringRule<? extends ASTVisitor>> rules = RulesContainer.getAllRules();
		rules.forEach(rule -> System.out.println(rule.getName()));
		
		Files.newDirectoryStream(Paths.get(PRERULE_DIRECTORY), "*Rule.java").forEach(System.out::println);
		
		for (Path path : Files.newDirectoryStream(Paths.get(PRERULE_DIRECTORY), "*Rule.java")) {
			String content = new String(Files.readAllBytes(path));
			
			IPackageFragment packageFragment = RulesTestUtil.getPackageFragement();
			ICompilationUnit compilationUnit = packageFragment.createCompilationUnit(path.getFileName().toString(), content, true, null);
			
			List<IJavaElement> javaElements = new ArrayList<>();
			javaElements.add(compilationUnit);
			
			AbstractRefactorer refactorer = new AbstractRefactorer(javaElements, rules) {};
			
			refactorer.prepareRefactoring();
			refactorer.doRefactoring();
			refactorer.commitRefactoring();
			
			System.out.println(compilationUnit.getSource());
		}
		
	}

}
