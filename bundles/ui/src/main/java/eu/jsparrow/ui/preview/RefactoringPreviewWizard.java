package eu.jsparrow.ui.preview;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import eu.jsparrow.core.exception.ReconcileException;
import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.StandaloneStatisticsMetadata;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.rules.common.exception.SimonykeesException;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.preview.statistics.RuleStatisticsSection;
import eu.jsparrow.ui.preview.statistics.StatisticsSection;
import eu.jsparrow.ui.preview.statistics.StatisticsSectionFactory;
import eu.jsparrow.ui.preview.statistics.StatisticsSectionUpdater;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.PayPerUseCreditCalculator;
import eu.jsparrow.ui.util.ResourceHelper;
import eu.jsparrow.ui.wizard.impl.SelectRulesWizard;
import eu.jsparrow.ui.wizard.impl.SelectRulesWizardData;

/**
 * This {@link Wizard} holds a {@link RefactoringPreviewWizardPage} for every
 * selected rule that generated at least one {@link DocumentChange}.
 * 
 * The OK Button commits the refactorings.
 * 
 * @author Ludwig Werzowa, Andreja Sambolec
 * @since 0.9
 */
public class RefactoringPreviewWizard extends AbstractPreviewWizard {

	private static final String WINDOW_ICON = "icons/jsparrow-icon-16-003.png"; //$NON-NLS-1$

	private RefactoringPipeline refactoringPipeline;

	private Shell shell;

	private RefactoringPreviewWizardModel model;
	protected RefactoringSummaryWizardPage summaryPage;
	protected StatisticsSection statisticsSection;
	protected StatisticsSection summaryPageStatisticsSection;
	protected StatisticsSectionUpdater updater;
	private Image windowIcon;

	private LicenseUtil licenseUtil = LicenseUtil.get();
	private StandaloneStatisticsMetadata statisticsMetadata;
	private PayPerUseCreditCalculator payPerUseCalculator = new PayPerUseCreditCalculator();
	private SelectRulesWizardData selectRulesWizardData;
	private boolean reuseRefactoringPipeline;

	public RefactoringPreviewWizard(RefactoringPipeline refactoringPipeline,
			StandaloneStatisticsMetadata standaloneStatisticsMetadata, SelectRulesWizardData selectRulesWizardData) {
		this(refactoringPipeline);
		this.statisticsMetadata = standaloneStatisticsMetadata;
		this.selectRulesWizardData = selectRulesWizardData;
	}

	public RefactoringPreviewWizard(RefactoringPipeline refactoringPipeline) {
		super();
		this.statisticsSection = StatisticsSectionFactory.createStatisticsSection(refactoringPipeline);
		this.summaryPageStatisticsSection = StatisticsSectionFactory
			.createStatisticsSectionForSummaryPage(refactoringPipeline);
		this.updater = new StatisticsSectionUpdater(statisticsSection, summaryPageStatisticsSection);
		this.refactoringPipeline = refactoringPipeline;
		this.shell = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow()
			.getShell();
		setNeedsProgressMonitor(true);
		windowIcon = ResourceHelper.createImage(WINDOW_ICON);
		org.eclipse.jface.window.Window.setDefaultImage(windowIcon);
	}

	@Override
	public String getWindowTitle() {
		return Messages.SummaryWizardPage_RunSummary;
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
					RuleStatisticsSection ruleStats = StatisticsSectionFactory.createRuleStatisticsSection(rule);
					RefactoringPreviewWizardPage previewPage = new RefactoringPreviewWizardPage(changes, rule, model,
							canFinish(), ruleStats, updater);
					previewPage.setTotalStatisticsSection(statisticsSection);
					addPage(previewPage);
				}
			});
		if (needsSummaryPage()) {
			this.summaryPage = new RefactoringSummaryWizardPage(refactoringPipeline, model, canFinish(),
					statisticsMetadata, summaryPageStatisticsSection);
			addPage(summaryPage);
		}
	}

	@Override
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
				statisticsSection.updateForSelected();
			}
		}
	}

	/**
	 * Method used to start new {@link IRunnableWithProgress} every time Back or
	 * Next is pressed and current page contains new changes in selection.
	 * 
	 * @param page
	 *            on which are changes
	 */
	private void startRecalculationRunnable(RefactoringPreviewWizardPage page) {
		IRunnableWithProgress job = recalculateRulesAndClearChanges(page);

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
				this.statisticsSection.updateForSelected();
				this.summaryPageStatisticsSection.updateForSelected();
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

	@Override
	public boolean canFinish() {
		if (licenseUtil.isFreeLicense()) {
			return licenseUtil.isActiveRegistration() && containsOnlyFreeRules();
		}

		LicenseValidationResult result = licenseUtil.getValidationResult();
		if (result.getLicenseType() != LicenseType.PAY_PER_USE) {
			return super.canFinish();
		}
		boolean enoughCredit = payPerUseCalculator.validateCredit(refactoringPipeline.getRules());
		return enoughCredit && super.canFinish();
	}

	private boolean containsOnlyFreeRules() {
		return refactoringPipeline.getRules()
			.stream()
			.allMatch(RefactoringRule::isFree);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {

		try {
			getContainer().run(true, true, this::tryDoAdditionalRefactoring);

		} catch (InvocationTargetException | InterruptedException e) {
			SimonykeesMessageDialog.openMessageDialog(shell,
					Messages.RefactoringPreviewWizard_err_runnableWithProgress,
					MessageDialog.ERROR);
			Activator.setRunning(false);
			return true;
		}

		if (!refactoringPipeline.hasAnyValidChange()) {
			SimonykeesMessageDialog.openMessageDialog(shell,
					"Cannot commit because all changes have been deselected.", //$NON-NLS-1$
					MessageDialog.ERROR);
			return false;
		}

		updateContainerOnCommit();

		IRunnableWithProgress job = monitor -> {

			try {
				refactoringPipeline.commitRefactoring(monitor);
				int sum = payPerUseCalculator.findTotalRequiredCredit(refactoringPipeline.getRules());
				licenseUtil.reserveQuantity(sum);
				Activator.setRunning(false);
			} catch (RefactoringException e) {
				synchronizeWithUIShowError(e);
				Activator.setRunning(false);
				return;
			} catch (ReconcileException e) {
				synchronizeWithUIShowError(e);
				Activator.setRunning(false);
			}

			return;
		};

		try {
			getContainer().run(true, true, job);
			showSuccessfulCommitMessage();

		} catch (InvocationTargetException | InterruptedException e) {
			SimonykeesMessageDialog.openMessageDialog(shell, Messages.RefactoringPreviewWizard_err_runnableWithProgress,
					MessageDialog.ERROR);
			Activator.setRunning(false);
		}

		return true;
	}

	private void tryDoAdditionalRefactoring(IProgressMonitor monitor) {
		Arrays.asList(getPages())
			.stream()
			.filter(page -> (page instanceof RefactoringPreviewWizardPage)
					&& !((RefactoringPreviewWizardPage) page).getUnselectedChange()
						.isEmpty())
			.forEach(page -> {
				tryDoAdditionalRefactoring(monitor, page);
				((RefactoringPreviewWizardPage) page).applyUnselectedChange();
			});
	}


	private void tryDoAdditionalRefactoring(IProgressMonitor monitor, IWizardPage page) {
		try {
			refactoringPipeline.doAdditionalRefactoring(((RefactoringPreviewWizardPage) page).getUnselectedChange(),
					((RefactoringPreviewWizardPage) page).getRule(), monitor);
			if (monitor.isCanceled()) {
				refactoringPipeline.clearStates();
			}
		} catch (RuleException e) {
			synchronizeWithUIShowError(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performCancel()
	 */
	@Override
	public boolean performCancel() {
		if (selectRulesWizardData != null) {
			reuseRefactoringPipeline = true;

			Display.getCurrent()
				.asyncExec(() -> {
					Job job = createJobToShowSelectRulesWizard(refactoringPipeline, selectRulesWizardData,
							"Cancelling file changes and opening Select Rules Wizard."); //$NON-NLS-1$

					job.setUser(true);
					job.schedule();
				});

		} else {
			Activator.setRunning(false);
		}
		return true;
	}

	public static Job createJobToShowSelectRulesWizard(RefactoringPipeline refactoringPipeline,
			SelectRulesWizardData selectRulesWizardData, String jobName) {

		return new Job(jobName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IStatus status = refactoringPipeline.cancelFileChanges(monitor);
				if (!status.isOK()) {
					refactoringPipeline.clearStates();
					Activator.setRunning(false);
					return Status.CANCEL_STATUS;
				}
				SelectRulesWizard.synchronizeWithUIShowSelectRulesWizard(refactoringPipeline,
						selectRulesWizardData);
				return Status.OK_STATUS;
			}
		};
	}

	@Override
	public void dispose() {
		if (!reuseRefactoringPipeline) {
			refactoringPipeline.clearStates();
		}
		windowIcon.dispose();
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
	public void imediatelyUpdateForSelected(ICompilationUnit newSelection, RefactoringRule rule) {
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
	@Override
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
	@Override
	public void pressedBack() {
		if (null != getContainer()) {
			if (getContainer().getCurrentPage() instanceof RefactoringPreviewWizardPage) {
				((RefactoringPreviewWizardPage) getContainer().getCurrentPage()).disposeControl();
			}
			getPreviousPage(getContainer().getCurrentPage());
		}
	}

	public RefactoringPreviewWizardModel getModel() {
		if (model == null) {
			model = new RefactoringPreviewWizardModel();
		}
		return model;
	}

	public RefactoringSummaryWizardPage getSummaryPage() {
		return this.summaryPage;
	}

	protected boolean needsSummaryPage() {
		return !(refactoringPipeline.getRules()
			.size() == 1
				&& refactoringPipeline.getRules()
					.get(0) instanceof StandardLoggerRule);
	}
	
	@Override
	public void showSummaryPage() {
		if(getSummaryPage() == null) {
			return;
		}
		/*
		 * If summary button is pressed on any page that is not
		 * Summary page, views have to be check for change and
		 * updated, and preview control has to be disposed on
		 * current page. If it is already on Summary page, just
		 * refresh.
		 */
		if (getContainer().getCurrentPage() instanceof RefactoringPreviewWizardPage) {
			updateViewsOnNavigation(getContainer().getCurrentPage());
			((RefactoringPreviewWizardPage) getContainer().getCurrentPage()).disposeControl();
		}
		getContainer().showPage(getSummaryPage());
	}
}
