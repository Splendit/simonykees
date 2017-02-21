package at.splendit.simonykees.core.ui;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.exception.RuleException;
import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.refactorer.AbstractRefactorer;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * {@link Wizard} holding the {@link SelectRulesWizardPage}, which contains a
 * list of all selectable rules.
 * 
 * Clicking the OK button either calls the {@link RefactoringPreviewWizard} (if
 * there are changes within the code for the selected rules), or a
 * {@link MessageDialog} informing the user that there are no changes.
 * 
 * @author Hannes Schweighofer, Ludwig Werzowa, Martin Huter
 * @since 0.9
 */
public class SelectRulesWizard extends Wizard {

	private final SelectRulesWizardPage selectRulesPage = new SelectRulesWizardPage();
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
		final List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = selectRulesPage.getSelectedRules();
		AbstractRefactorer refactorer = new AbstractRefactorer(javaElements, rules) {
		};

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

		if (LicenseUtil.isValid()) {
			if (refactorer.hasChanges()) {
				final WizardDialog dialog = new WizardDialog(getShell(), new RefactoringPreviewWizard(refactorer));

				Rectangle rectangle = Display.getCurrent().getPrimaryMonitor().getBounds();

				// maximizes the RefactoringPreviewWizard
				dialog.setPageSize(rectangle.width, rectangle.height);

				dialog.open();
			} else {
				SimonykeesMessageDialog.openMessageDialog(getShell(),
						Messages.SelectRulesWizard_warning_no_refactorings, MessageDialog.INFORMATION);
			}
		} else {
			LicenseUtil.displayLicenseErrorDialog(getShell());
		}

		return true;
	}

}
