package at.splendit.simonykees.core.ui;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jface.wizard.Wizard;

import at.splendit.simonykees.core.Activator;
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
		rules.forEach(rule -> Activator.log(rule.getName()));
//		new AbstractRefactorer(javaElements, rules) {}.doRefactoring();
		return true;
	}

}
