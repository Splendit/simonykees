package at.splendit.simonykees.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;

import at.splendit.simonykees.core.refactorer.AbstractRefactorer;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

public class AbstractRulesTest {

	public AbstractRulesTest() {
		super();
	}

	protected String processFile(String fileName, String content,
			List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules) throws Exception {
	
		IPackageFragment packageFragment = RulesTestUtil.getPackageFragement();
		ICompilationUnit compilationUnit = packageFragment.createCompilationUnit(fileName, content, true, null);
	
		List<IJavaElement> javaElements = new ArrayList<>();
		javaElements.add(compilationUnit);
	
		AbstractRefactorer refactorer = new AbstractRefactorer(javaElements, rules) {
		};
	
		refactorer.prepareRefactoring();
		refactorer.doRefactoring();
		refactorer.commitRefactoring();
	
		return compilationUnit.getSource();
	}

}