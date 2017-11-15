package eu.jsparrow.ui.preview;

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

import eu.jsparrow.core.exception.ReconcileException;
import eu.jsparrow.core.exception.RefactoringException;
import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.exception.SimonykeesException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.util.LicenseUtil;

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

	private RefactoringSummaryWizardPage summaryPage;

	private RefactoringPreviewWizardModel model;

	public RefactoringPreviewWizard(RefactoringPipeline refactoringPipeline) {
		super();
		this.refactoringPipeline = refactoringPipeline;
		this.shell = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow()
			.getShell();
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
		model = new RefactoringPreviewWizardModel();
		refactoringPipeline.getRules()
			.forEach(rule -> {
				Map<ICompilationUnit, DocumentChange> changes = refactoringPipeline.getChangesForRule(rule);
				if (!changes.isEmpty()) {
					RefactoringPreviewWizardPage previewPage = new RefactoringPreviewWizardPage(changes, rule, model);
					addPage(previewPage);
				}
			});
		summaryPage = new RefactoringSummaryWizardPage(refactoringPipeline, model);
		if (!(refactoringPipeline.getRules()
			.size() == 1
				&& refactoringPipeline.getRules()
					.get(0) instanceof StandardLoggerRule)) {
			addPage(summaryPage);
		}
	}

	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		updateViewsOnNavigation(page);
		return super.getPreviousPage(page);
	}

	public void updateViewsOnNavigation(IWizardPage page) {
		if (page instanceof RefactoringPreviewWizardPage) {
			if (!((RefactoringPreviewWizardPage) page).getUnselectedChange()
				.isEmpty()) {
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
			/*
			 * Update all changes and unselected classes that were unselected in
			 * the last page shown before finish was pressed
			 */
			Arrays.asList(getPages())
				.stream()
				.filter(page -> (page instanceof RefactoringPreviewWizardPage)
						&& !((RefactoringPreviewWizardPage) page).getUnselectedChange()
							.isEmpty())
				.forEach(page -> {
					try {
						refactoringPipeline.doAdditionalRefactoring(
								((RefactoringPreviewWizardPage) page).getUnselectedChange(),
								((RefactoringPreviewWizardPage) page).getRule(), monitor);
						if (monitor.isCanceled()) {
							refactoringPipeline.clearStates();
						}
					} catch (RuleException e) {
						synchronizeWithUIShowError(e);
					}
					((RefactoringPreviewWizardPage) page).applyUnselectedChange();
				});

			if (LicenseUtil.getInstance()
				.isValid()) {
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
				LicenseUtil.getInstance()
					.displayLicenseErrorDialog(getShell());
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
		Display.getDefault()
			.asyncExec(() -> {
				Shell workbenchShell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
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
				((RefactoringSummaryWizardPage) getContainer().getCurrentPage()).disposeCompareInputControl();
			}
			getPreviousPage(getContainer().getCurrentPage());
		}
	}

	@Override
	public boolean canFinish() {
		if (!LicenseUtil.getInstance()
			.isFullLicense()) {
			return false;
		}
		return super.canFinish();
	}

	public RefactoringSummaryWizardPage getSummaryPage() {
		return summaryPage;
	}

	public RefactoringPreviewWizardModel getModel() {
		if (model == null) {
			model = new RefactoringPreviewWizardModel();
		}
		return model;
	}
}
