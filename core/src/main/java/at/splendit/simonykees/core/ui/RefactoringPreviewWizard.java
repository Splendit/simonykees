package at.splendit.simonykees.core.ui;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import at.splendit.simonykees.core.exception.ReconcileException;
import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.refactorer.AbstractRefactorer;
import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;

/**
 * This {@link Wizard} holds a {@link RefactoringPreviewWizardPage} for every
 * selected rule that generated at least one {@link DocumentChange}.
 * 
 * The OK Button commits the refactorings.
 * 
 * @author Ludwig Werzowa
 * @since 0.9
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
