package at.splendit.simonykees.core.ui;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.refactorer.AbstractRefactorer;
import at.splendit.simonykees.core.rule.RefactoringRule;

public class SelectRulesWizard extends Wizard {

	private final SelectRulesPage selectRulesPage = new SelectRulesPage();
	private final List<IJavaElement> javaElements;
	
	public SelectRulesWizard(List<IJavaElement> javaElements) {
		this.javaElements = javaElements;
	}
	
	@Override
	public void addPages() {
		addPage(selectRulesPage);
	}

	@Override
	public String getWindowTitle() {
		return "Select Rules";
	}
	
	@Override
	public boolean performFinish() {
		final List<RefactoringRule<? extends ASTVisitor>> rules = selectRulesPage.getSelectedRules();
		AbstractRefactorer refactorer = new AbstractRefactorer(javaElements, rules) {};
		
		refactorer.prepareRefactoring();
		refactorer.doRefactoring();
		
		Activator.log(formatRuleAndFileAndChange(refactorer.getRules()));
		
		refactorer.commitRefactoring();
		
		return true;
	}
	
	private String formatRuleAndFileAndChange(List<RefactoringRule<? extends ASTVisitor>> rules) {
		String output = "";
		
		for (RefactoringRule<? extends ASTVisitor> rule : rules) {
			output += "\nRule [" + rule.getName() + "]\n";
			Map<ICompilationUnit, DocumentChange> changes = rule.getDocumentChanges();
			for (ICompilationUnit cu : changes.keySet()) {
				output += "\tFile\t[" + cu.getPath().toString() + "]\n";
				output += "\tChange\t[\n" + changes.get(cu).getEdit() + "\n\t]\n"; 
			}
		}
		
		return output;
	}

}
