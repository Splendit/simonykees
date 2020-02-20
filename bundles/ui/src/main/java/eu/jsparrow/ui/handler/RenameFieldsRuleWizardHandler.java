package eu.jsparrow.ui.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.rules.common.util.RefactoringUtil;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.CompilationErrorsMessageDialog;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.LicenseUtilService;
import eu.jsparrow.ui.util.WizardHandlerUtil;
import eu.jsparrow.ui.wizard.impl.SelectRulesWizard;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;
import eu.jsparrow.ui.wizard.semiautomatic.ConfigureRenameFieldsRuleWizard;

/**
 * Handler for semi-automatic rename public fields rule
 * 
 * @author Andreja Sambolec, Ardit Ymeri
 * @since 2.3.0
 *
 */
public class RenameFieldsRuleWizardHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(RenameFieldsRuleWizardHandler.class);

	private LicenseUtilService licenseUtil = LicenseUtil.get();

	public RenameFieldsRuleWizardHandler() {

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (Activator.isRunning()) {
			SimonykeesMessageDialog.openMessageDialog(Display.getDefault()
				.getActiveShell(), Messages.SelectRulesWizardHandler_allready_running, MessageDialog.INFORMATION);
		} else {
			Activator.setRunning(true);

			final Shell shell = HandlerUtil.getActiveShell(event);
			if (!licenseUtil.checkAtStartUp(shell)) {
				Activator.setRunning(false);
				return null;
			}

			Map<IJavaProject, List<IJavaElement>> selectedJavaElements;
			try {
				selectedJavaElements = WizardHandlerUtil.getSelectedJavaElements(event);
			} catch (CoreException e) {
				logger.error(e.getMessage(), e);
				WizardMessageDialog.synchronizeWithUIShowError(new RefactoringException(
						Messages.SelectRulesWizardHandler_getting_selected_resources_failed + e.getMessage(),
						Messages.SelectRulesWizardHandler_user_getting_selected_resources_failed, e));
				return null;
			}
			if (!selectedJavaElements.isEmpty()) {

				if (selectedJavaElements.size() != 1) {
					synchronizeWithUIShowSelectionErrorMessage();
					return false;
				}

				Job job = new Job(Messages.RenameFieldsRuleWizardHandler_performFinish_jobName) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						List<IJavaElement> selectedElements = selectedJavaElements.entrySet()
							.iterator()
							.next()
							.getValue();

						List<ICompilationUnit> iCompilationUnits = new ArrayList<>();

						boolean transformed = getCompilationUnits(iCompilationUnits, selectedElements, monitor);
						if (!transformed) {
							return Status.CANCEL_STATUS;
						}

						List<ICompilationUnit> errorIcus = iCompilationUnits.stream()
							.filter(RefactoringUtil::checkForSyntaxErrors)
							.collect(Collectors.toList());

						List<ICompilationUnit> errorFreeIcus = iCompilationUnits.stream()
							.filter(icu -> !errorIcus.contains(icu))
							.collect(Collectors.toList());

						if (!errorIcus.isEmpty()) {
							synchronizeWithUIShowCompilationErrorMessage(errorIcus, errorFreeIcus);
						} else if (!errorFreeIcus.isEmpty()) {
							synchronizeWithUIShowRenameFieldsRuleWizard(errorFreeIcus);
						} else {
							logger.warn(ExceptionMessages.RefactoringPipeline_warn_no_compilation_units_found);
							WizardMessageDialog.synchronizeWithUIShowInfo(new RefactoringException(
									ExceptionMessages.RefactoringPipeline_warn_no_compilation_units_found,
									ExceptionMessages.RefactoringPipeline_user_warn_no_compilation_units_found));
						}

						return Status.OK_STATUS;
					}
				};

				job.setUser(true);
				job.schedule();

				return true;
			} else {
				// SIM-656
				WizardMessageDialog.synchronizedWithUIShowWarningNoCompilationUnitDialog();
				logger.error(Messages.SelectRulesWizardHandler_selectionNotPossible_ubuntuBug);
				Activator.setRunning(false);
			}

		}
		return null;

	}

	/**
	 * Method used to open SelectRulesWizard from non UI thread
	 */
	private void synchronizeWithUIShowRenameFieldsRuleWizard(List<ICompilationUnit> selectedJavaElements) {
		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				final WizardDialog dialog = new WizardDialog(shell,
						new ConfigureRenameFieldsRuleWizard(selectedJavaElements)) {
					/*
					 * Removed unnecessary empty space on the bottom of the
					 * wizard intended for ProgressMonitor that is not
					 * used(non-Javadoc)
					 * 
					 * @see org.eclipse.jface.wizard.WizardDialog#
					 * createDialogArea(org.eclipse.swt.widgets. Composite)
					 */
					@Override
					protected Control createDialogArea(Composite parent) {
						Control ctrl = super.createDialogArea(parent);
						getProgressMonitor();
						return ctrl;
					}

					@Override
					protected IProgressMonitor getProgressMonitor() {
						ProgressMonitorPart monitor = (ProgressMonitorPart) super.getProgressMonitor();
						GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.heightHint = 0;
						monitor.setLayoutData(gridData);
						monitor.setVisible(false);
						return monitor;
					}
				};

				dialog.open();
			});
	}

	/**
	 * Method used to open CompilationErrorsMessageDialog from non UI thread to
	 * list all Java files that will be skipped because they contain compilation
	 * errors.
	 */
	private void synchronizeWithUIShowCompilationErrorMessage(List<ICompilationUnit> containingErrorList,
			List<ICompilationUnit> errorFreeICus) {
		Display.getDefault()
			.syncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				CompilationErrorsMessageDialog dialog = new CompilationErrorsMessageDialog(shell);
				dialog.create();
				dialog.setTableViewerInput(containingErrorList);
				dialog.open();
				if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
					if (!errorFreeICus.isEmpty()) {
						synchronizeWithUIShowRenameFieldsRuleWizard(errorFreeICus);
					} else {
						WizardMessageDialog.synchronizeWithUIShowWarningNoComlipationUnitWithoutErrorsDialog();
					}
				} else {

					Activator.setRunning(false);
				}
			});
	}

	private void synchronizeWithUIShowSelectionErrorMessage() {
		Display.getDefault()
			.syncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				MessageDialog.openError(shell, Messages.RenameFieldsRuleWizardHandler_multipleProjectsSelected,
						Messages.RenameFieldsRuleWizardHandler_renamingRuleOnOneProjectOnly);

				Activator.setRunning(false);
			});
	}

	/**
	 * Collects all CompilationUnits from IjavaElements
	 * 
	 * @param resultCompilationUnitsList
	 *            resulting list containing all created CompilationUnits
	 * @param sourceJavaElementsList
	 *            source list containing all IJavaElements
	 * @param monitor
	 *            progress monitor
	 * @return false if result list is empty of exception occurred while
	 *         collecting, true otherwise
	 */
	private boolean getCompilationUnits(List<ICompilationUnit> resultCompilationUnitsList,
			List<IJavaElement> sourceJavaElementsList, IProgressMonitor monitor) {

		try {
			SelectRulesWizard.collectICompilationUnits(resultCompilationUnitsList, sourceJavaElementsList, monitor);
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), e);
			WizardMessageDialog.synchronizeWithUIShowInfo(
					new RefactoringException(ExceptionMessages.RefactoringPipeline_java_element_resolution_failed,
							ExceptionMessages.RefactoringPipeline_user_java_element_resolution_failed, e));
			return false;
		}

		return true;
	}
}
