/**
 * 
 */
package at.splendit.simonykees.core.ui;

import org.eclipse.jface.wizard.Wizard;

import at.splendit.simonykees.core.refactorer.AbstractRefactorer;

public class RefactoringPreviewWizard extends Wizard {
	
	private AbstractRefactorer abstractRefactorer;

	public RefactoringPreviewWizard(AbstractRefactorer abstractRefactorer) {
		super();
		this.abstractRefactorer = abstractRefactorer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		abstractRefactorer.getRules().stream().filter(rule -> !rule.getDocumentChanges().isEmpty()).forEach(rule -> addPage(new RefactoringPreviewWizardPage(rule)));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		abstractRefactorer.commitRefactoring();
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performCancel()
	 */
	@Override
	public boolean performCancel() {
		// TODO Auto-generated method stub
		return super.performCancel();
	}

}
