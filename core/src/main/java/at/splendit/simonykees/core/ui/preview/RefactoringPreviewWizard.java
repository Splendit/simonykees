package at.splendit.simonykees.core.ui.preview;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.swt.custom.BusyIndicator;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.exception.ReconcileException;
import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.refactorer.RefactoringPipeline;
import at.splendit.simonykees.core.ui.LicenseUtil;
import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;

/**
 * This {@link Wizard} holds a {@link RefactoringPreviewWizardPage} for every
 * selected rule that generated at least one {@link DocumentChange}.
 * 
 * The OK Button commits the refactorings.
 * 
 * @author Ludwig Werzowa, Andreja Sambolec
 * @since 0.9
 */
public class RefactoringPreviewWizard extends Wizard {

	private RefactoringPipeline refactoringPipeline;

	public RefactoringPreviewWizard(RefactoringPipeline refactoringPipeline) {
		super();
		this.refactoringPipeline = refactoringPipeline;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		// refactoringPipeline.getRules().stream().filter(rule ->
		// !rule.getDocumentChanges().isEmpty())
		// .forEach(rule -> addPage(new RefactoringPreviewWizardPage(rule)));
		refactoringPipeline.getPreviewNodes().forEach(node -> addPage(new RefactoringPreviewWizardPage(node)));
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (!((RefactoringPreviewWizardPage) page).getChangedSelections().isEmpty()) {
			recalculateRulesAndClearChanges((RefactoringPreviewWizardPage) page);
		}
		return super.getNextPage(page);
	}

	private void recalculateRulesAndClearChanges(RefactoringPreviewWizardPage page) {
		// TODO recalculate again
		// refactoringPipeline.doAdditionalRefactoring();

		page.getChangedSelections().keySet().stream().forEach(compilationUnit -> page.getPreviewNode().getSelections()
				.put(compilationUnit, page.getChangedSelections().get(compilationUnit)));
		page.getChangedSelections().clear();
	}

	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (!((RefactoringPreviewWizardPage) page).getChangedSelections().isEmpty()) {
			recalculateRulesAndClearChanges((RefactoringPreviewWizardPage) page);
		}
		return super.getPreviousPage(page);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {

			@Override
			public void run() {
				if (LicenseUtil.getInstance().isValid()) {
					try {
						refactoringPipeline.commitRefactoring();
						Activator.setRunning(false);
					} catch (RefactoringException e) {
						SimonykeesMessageDialog.openErrorMessageDialog(getShell(), e);
						Activator.setRunning(false);
						return;
					} catch (ReconcileException e) {
						SimonykeesMessageDialog.openErrorMessageDialog(getShell(), e);
						Activator.setRunning(false);
					}
				} else {
					LicenseUtil.getInstance().displayLicenseErrorDialog(getShell());
					Activator.setRunning(false);
				}
				return;
			}
		});

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performCancel()
	 */
	@Override
	public boolean performCancel() {
		refactoringPipeline.clearStates();
		Activator.setRunning(false);
		return super.performCancel();
	}

	@Override
	public void dispose() {
		refactoringPipeline.clearStates();
		super.dispose();
	}
}
