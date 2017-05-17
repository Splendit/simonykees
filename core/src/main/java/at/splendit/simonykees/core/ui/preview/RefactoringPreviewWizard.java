package at.splendit.simonykees.core.ui.preview;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.swt.custom.BusyIndicator;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.exception.ReconcileException;
import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.exception.RuleException;
import at.splendit.simonykees.core.refactorer.RefactoringPipeline;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.ui.LicenseUtil;
import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

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
		refactoringPipeline.getRules().forEach(rule -> {
			Map<ICompilationUnit, DocumentChange> changes = refactoringPipeline.getChangesForRule(rule);
			if (!changes.isEmpty()) {
				addPage(new RefactoringPreviewWizardPage(changes, rule));
			}
		});
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (!((RefactoringPreviewWizardPage) page).getUnselectedChange().isEmpty()) {
			recalculateRulesAndClearChanges((RefactoringPreviewWizardPage) page);
		}
		return super.getNextPage(page);
	}

	private void recalculateRulesAndClearChanges(RefactoringPreviewWizardPage page) {
		try {
			refactoringPipeline.doAdditionalRefactoring(page.getUnselectedChange(), page.getRule());
		} catch (RuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		page.applyUnselectedChange();

		updateAllPages();
	}

	private void updateAllPages() {
		// TODO Auto-generated method stub
		for (IWizardPage page : getPages()) {
			((RefactoringPreviewWizardPage) page)
					.update(refactoringPipeline.getChangesForRule(((RefactoringPreviewWizardPage) page).getRule()));
		}
	}

	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (!((RefactoringPreviewWizardPage) page).getUnselectedChange().isEmpty()) {
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

		Arrays.asList(getPages()).stream().forEach(page -> {
			if (!((RefactoringPreviewWizardPage) page).getUnselectedChange().isEmpty()) {
				recalculateRulesAndClearChanges((RefactoringPreviewWizardPage) page);
			}
		});

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

	public void imediatelyUpdateForSelected(ICompilationUnit newSelection,
			RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule) {
		try {
			refactoringPipeline.refactoringForCurrent(newSelection, rule);
		} catch (RuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		updateAllPages();
	}
}
