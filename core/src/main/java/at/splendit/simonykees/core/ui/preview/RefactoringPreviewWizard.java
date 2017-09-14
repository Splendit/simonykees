package at.splendit.simonykees.core.ui.preview;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.exception.ReconcileException;
import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.exception.RuleException;
import at.splendit.simonykees.core.exception.SimonykeesException;
import at.splendit.simonykees.core.refactorer.RefactoringPipeline;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.ui.LicenseUtil;
import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.Messages;

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

	private Shell shell;

	public RefactoringPreviewWizard(RefactoringPipeline refactoringPipeline) {
		super();
		this.refactoringPipeline = refactoringPipeline;
		this.shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		setNeedsProgressMonitor(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		/*
		 * First summary page is created to collect all initial source from
		 * working copies
		 */
		RefactoringSummaryWizardPage summaryPage = new RefactoringSummaryWizardPage(refactoringPipeline);
		refactoringPipeline.getRules().forEach(rule -> {
			Map<ICompilationUnit, DocumentChange> changes = refactoringPipeline.getChangesForRule(rule);
			if (!changes.isEmpty()) {
				addPage(new RefactoringPreviewWizardPage(changes, rule));
			}
		});
		addPage(summaryPage);
	}

	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		updateViewsOnNavigation(page);
		return super.getPreviousPage(page);
	}

	private void updateViewsOnNavigation(IWizardPage page) {
		if (page instanceof RefactoringPreviewWizardPage) {
			if (!((RefactoringPreviewWizardPage) page).getUnselectedChange().isEmpty()) {
				/*
				 * if there are changes in refactoring page, it means that Back
				 * button was pressed and recalculation is needed
				 */
				startRecalculationRunnable((RefactoringPreviewWizardPage) page);
			} else {
				/*
				 * if there are no changes in refactoring page, just populate
				 * the view with current updated values
				 */
				((RefactoringPreviewWizardPage) page).populateViews(false);
			}
		}
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		updateViewsOnNavigation(page);
		return super.getNextPage(page);
	}

	/**
	 * Method used to start new {@link IRunnableWithProgress} every time Back or
	 * Next is pressed and current page contains new changes in selection.
	 * 
	 * @param page
	 *            on which are changes
	 */
	private void startRecalculationRunnable(RefactoringPreviewWizardPage page) {
		IRunnableWithProgress job = recalculateRulesAndClearChanges((RefactoringPreviewWizardPage) page);

		if (null != job) {
			try {
				getContainer().run(true, true, job);
			} catch (InvocationTargetException | InterruptedException e) {
				SimonykeesMessageDialog.openMessageDialog(shell,
						Messages.RefactoringPreviewWizard_err_runnableWithProgress, MessageDialog.ERROR);
				Activator.setRunning(false);
			}
		}
	}

	/**
	 * Creates new {@link IRunnableWithProgress} which blocks UI thread, shows
	 * progress monitor for refactoring and recalculates all rules for
	 * unselected working copies.
	 * 
	 * @param page
	 *            on which changes were made
	 * @return new IRunnableWithProgress
	 */
	private IRunnableWithProgress recalculateRulesAndClearChanges(RefactoringPreviewWizardPage page) {
		return monitor -> {
			try {
				refactoringPipeline.doAdditionalRefactoring(page.getUnselectedChange(), page.getRule(), monitor);
				if (monitor.isCanceled()) {
					refactoringPipeline.clearStates();
				}
			} catch (RuleException e) {
				synchronizeWithUIShowError(e);
			} finally {
				monitor.done();
			}
			page.applyUnselectedChange();
			updateAllPages();
		};

	}

	/**
	 * Updates changesForRule map for every page in {@link Wizard}
	 */
	private void updateAllPages() {
		for (IWizardPage page : getPages()) {
			if (page instanceof RefactoringPreviewWizardPage) {
				((RefactoringPreviewWizardPage) page)
						.update(refactoringPipeline.getChangesForRule(((RefactoringPreviewWizardPage) page).getRule()));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {

		IRunnableWithProgress job = monitor -> {
			Arrays.asList(getPages()).stream().forEach(page -> {
				if ((page instanceof RefactoringPreviewWizardPage)
						&& !((RefactoringPreviewWizardPage) page).getUnselectedChange().isEmpty()) {
					recalculateRulesAndClearChanges((RefactoringPreviewWizardPage) page);
				}
			});

			if (LicenseUtil.getInstance().isValid()) {
				try {
					refactoringPipeline.commitRefactoring();
					Activator.setRunning(false);
				} catch (RefactoringException e) {
					synchronizeWithUIShowError(e);
					Activator.setRunning(false);
					return;
				} catch (ReconcileException e) {
					synchronizeWithUIShowError(e);
					Activator.setRunning(false);
				}
			} else {
				LicenseUtil.getInstance().displayLicenseErrorDialog(getShell());
				Activator.setRunning(false);
			}
			return;
		};

		try {
			getContainer().run(true, true, job);
		} catch (InvocationTargetException | InterruptedException e) {
			SimonykeesMessageDialog.openMessageDialog(shell, Messages.RefactoringPreviewWizard_err_runnableWithProgress,
					MessageDialog.ERROR);
			Activator.setRunning(false);
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
		refactoringPipeline.clearStates();
		Activator.setRunning(false);
		return super.performCancel();
	}

	@Override
	public void dispose() {
		refactoringPipeline.clearStates();
		super.dispose();
	}

	/**
	 * When one compilation unit is checked from previously unchecked state it
	 * has to be recalculated and shown immediately.
	 * 
	 * @param newSelection
	 *            checked working copy
	 * @param rule
	 *            for which working copy is checked
	 */
	public void imediatelyUpdateForSelected(ICompilationUnit newSelection,
			RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule) {
		try {
			refactoringPipeline.refactoringForCurrent(newSelection, rule);
		} catch (RuleException exception) {
			SimonykeesMessageDialog.openErrorMessageDialog(shell, exception);
			Activator.setRunning(false);
		}

		updateAllPages();
		((RefactoringPreviewWizardPage) getContainer().getCurrentPage()).populateViews(true);
	}

	/**
	 * Method used to open ErrorDialog from non UI thread
	 */
	private void synchronizeWithUIShowError(SimonykeesException exception) {
		Display.getDefault().asyncExec(() -> {
			Shell workbenchShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			SimonykeesMessageDialog.openErrorMessageDialog(workbenchShell, exception);
			Activator.setRunning(false);
		});
	}

	/**
	 * Called from {@link WizardDialog} when Next button is pressed. Triggers
	 * recalculation if needed. Disposes control from current page which wont be
	 * visible any more
	 */
	public void pressedNext() {
		if (null != getContainer()) {
			((RefactoringPreviewWizardPage) getContainer().getCurrentPage()).disposeControl();
			getNextPage(getContainer().getCurrentPage());
		}
	}

	/**
	 * Called from {@link WizardDialog} when Back button is pressed. Disposes
	 * all controls to be recalculated and created when needed
	 */
	public void pressedBack() {
		if (null != getContainer()) {
			if (getContainer().getCurrentPage() instanceof RefactoringPreviewWizardPage) {
				((RefactoringPreviewWizardPage) getContainer().getCurrentPage()).disposeControl();
			} else {
				((RefactoringSummaryWizardPage) getContainer().getCurrentPage()).disposeControl();
			}
			getPreviousPage(getContainer().getCurrentPage());
		}
	}

	@Override
	public boolean canFinish() {
		if (LicenseUtil.getInstance().isTrial()) {
			return true;
		}
		return super.canFinish();
	}
}
