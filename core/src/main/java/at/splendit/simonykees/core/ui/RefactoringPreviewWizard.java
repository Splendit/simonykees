package at.splendit.simonykees.core.ui;

import org.eclipse.jface.wizard.Wizard;

import at.splendit.simonykees.core.exception.ReconcileException;
import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.refactorer.AbstractRefactorer;
import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;

/**
 * TODO SIM-103 class description
 * 
 * @author Martin Huter, Ludwig Werzowa
 * @since 0.9
 *
 */
public class RefactoringPreviewWizard extends Wizard {

	private AbstractRefactorer abstractRefactorer;

	public RefactoringPreviewWizard(AbstractRefactorer abstractRefactorer) {
		super();
		this.abstractRefactorer = abstractRefactorer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		abstractRefactorer.getRules().stream().filter(rule -> !rule.getDocumentChanges().isEmpty())
				.forEach(rule -> addPage(new RefactoringPreviewWizardPage(rule)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		try {
			abstractRefactorer.commitRefactoring();
		} catch (RefactoringException e) {
			SimonykeesMessageDialog.openErrorMessageDialog(getShell(), e);
			return true;
		} catch (ReconcileException e) {
			SimonykeesMessageDialog.openErrorMessageDialog(getShell(), e);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performCancel()
	 */
	@Override
	public boolean performCancel() {
		return super.performCancel();
	}

}
