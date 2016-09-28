package at.splendit.simonykees.core.ui;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import at.splendit.simonykees.core.exception.MalformedInputException;
import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.exception.RuleException;
import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.refactorer.AbstractRefactorer;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;

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
		return Messages.SelectRulesWizard_title;
	}
	
	@Override
	public boolean performFinish() {
		final List<RefactoringRule<? extends ASTVisitor>> rules = selectRulesPage.getSelectedRules();
		AbstractRefactorer refactorer = new AbstractRefactorer(javaElements, rules) {};
		
		try {
			refactorer.prepareRefactoring();
		} catch (RefactoringException e) {
			SimonykeesMessageDialog.openErrorMessageDialog(getShell(), e);
			return true;
		}
		try {
			refactorer.doRefactoring();
		} catch (RefactoringException e) {
			SimonykeesMessageDialog.openErrorMessageDialog(getShell(), e);
			return true;
		} catch (RuleException e) {
			SimonykeesMessageDialog.openErrorMessageDialog(getShell(), e);
		}
		
		if (refactorer.hasChanges()) {
			final WizardDialog dialog = new WizardDialog(getShell(), new RefactoringPreviewWizard(refactorer));
			
			Rectangle rectangle = Display.getCurrent().getPrimaryMonitor().getBounds();
			dialog.setPageSize(rectangle.width, rectangle.height); // maximizes the RefactoringPreviewWizard 
			
			dialog.open();
		} else {
			MessageDialog dialog = new MessageDialog(getShell(), Messages.aa_codename, null, Messages.SelectRulesWizard_warning_no_refactorings, MessageDialog.INFORMATION, 1, Messages.ui_ok);
			dialog.open();
		}
		
		return true;
	}

}
