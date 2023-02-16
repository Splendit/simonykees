package eu.jsparrow.ui.preview;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.core.exception.ReconcileException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.PayPerUseCreditCalculator;
import eu.jsparrow.ui.wizard.AbstractRefactoringWizard;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

/**
 * A parent class for all preview wizards.
 * 
 * @author Ardit Ymeri
 * @since 2.3.1
 *
 */
@SuppressWarnings("nls")
public abstract class AbstractPreviewWizard extends AbstractRefactoringWizard {

	private static final String NO_CHANGES_TO_COMMIT_MESSAGE = "Cannot commit because all changes have been deselected!";
	private static final String NO_CHANGES_TO_COMMIT_TITLE = "No Changes to Commit";
	private static final String COMMIT_SUCCESSFUL_MESSAGE = "Changes committed successfully!";
	private static final String COMMIT_SUCCESSFUL_TITLE = "Commit Successful";
	protected RefactoringPipeline refactoringPipeline;
	private PayPerUseCreditCalculator payPerUseCalculator = new PayPerUseCreditCalculator();
	private LicenseUtil licenseUtil = LicenseUtil.get();

	protected AbstractPreviewWizard(RefactoringPipeline refactoringPipeline) {
		ContextInjectionFactory.inject(this, Activator.getEclipseContext());
		this.refactoringPipeline = refactoringPipeline;
	}

	@Override
	public boolean canFinish() {
		if (!super.canFinish()) {
			return false;
		}
		if (licenseUtil.isFreeLicense()) {
			return canHaveFreeRule() && containsOnlyFreeRules() && licenseUtil.isActiveRegistration();
		}
		LicenseValidationResult result = licenseUtil.getValidationResult();
		if (result.getLicenseType() != LicenseType.PAY_PER_USE) {
			return true;
		}
		return payPerUseCalculator.validateCredit(refactoringPipeline.getRules());
	}

	protected boolean canHaveFreeRule() {
		return false;
	}

	protected boolean containsOnlyFreeRules() {
		return refactoringPipeline.getRules()
			.stream()
			.allMatch(RefactoringRule::isFree);
	}

	public abstract void updateViewsOnNavigation(IWizardPage page);

	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		updateViewsOnNavigation(page);
		return super.getPreviousPage(page);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		updateViewsOnNavigation(page);
		return super.getNextPage(page);
	}

	public void updateContainerOnCommit() {
		IWizardContainer container = getContainer();
		if (container instanceof PreviewWizardDialog) {
			PreviewWizardDialog previewWizardDialog = (PreviewWizardDialog) container;
			previewWizardDialog.updateOnCommit();
		}
	}

	private void showDialogOnPerformFinish(String dialogTitle, String dialogMessage) {
		new MessageDialog(getShell(), dialogTitle, null, dialogMessage,
				MessageDialog.INFORMATION, 0, IDialogConstants.OK_LABEL).open();
	}

	protected Map<ICompilationUnit, DocumentChange> getChangesForRule(RefactoringRule rule) {
		return refactoringPipeline.getChangesForRule(rule);
	}

	protected void clearPipelineState() {
		refactoringPipeline.clearStates();
	}

	protected List<RefactoringRule> getPipelineRules() {
		return refactoringPipeline.getRules();
	}

	@Override
	public boolean performFinish() {
		IWizardContainer container = getContainer();
		if (container == null) {
			return true;
		}
		try {
			container.run(true, true, this::prepareForCommit);
		} catch (InvocationTargetException | InterruptedException e) {
			SimonykeesMessageDialog.openMessageDialog(getShell(),
					Messages.RefactoringPreviewWizard_err_runnableWithProgress,
					MessageDialog.ERROR);
			Activator.setRunning(false);
			return true;
		}
		if (!hasAnyValidChange()) {
			showDialogOnPerformFinish(NO_CHANGES_TO_COMMIT_TITLE, NO_CHANGES_TO_COMMIT_MESSAGE);
			return false;
		}
		commitChanges();
		return true;
	}

	protected boolean hasAnyValidChange() {
		return refactoringPipeline.hasAnyValidChange();
	}

	protected void commitChanges() {
		updateContainerOnCommit();
		IRunnableWithProgress job = monitor -> {
			try {
				commitChanges(monitor);
				Activator.setRunning(false);
			} catch (RefactoringException | ReconcileException e) {
				WizardMessageDialog.synchronizeWithUIShowError(e);
				Activator.setRunning(false);
			}
		};

		try {
			getContainer().run(true, true, job);
			showDialogOnPerformFinish(COMMIT_SUCCESSFUL_TITLE, COMMIT_SUCCESSFUL_MESSAGE);
		} catch (InvocationTargetException | InterruptedException e) {
			SimonykeesMessageDialog.openMessageDialog(getShell(),
					Messages.RefactoringPreviewWizard_err_runnableWithProgress,
					MessageDialog.ERROR);
			Activator.setRunning(false);
		}
	}

	protected void commitChanges(IProgressMonitor monitor) throws RefactoringException, ReconcileException {
		refactoringPipeline.commitRefactoring(monitor);
		int sum = payPerUseCalculator.findTotalRequiredCredit(getPipelineRules());
		licenseUtil.reserveQuantity(sum);
	}

	protected boolean needsSummaryPage() {
		return true;
	}

	/**
	 * Called from {@link WizardDialog} when Next button is pressed. Triggers
	 * recalculation if needed. Disposes control from current page which wont be
	 * visible any more
	 */
	protected abstract void pressedNext();

	/**
	 * Called from {@link WizardDialog} when Back button is pressed. Disposes
	 * all controls to be recalculated and created when needed
	 */
	protected abstract void pressedBack();

	public abstract void showSummaryPage();

	protected abstract void prepareForCommit(IProgressMonitor monitor);

}
