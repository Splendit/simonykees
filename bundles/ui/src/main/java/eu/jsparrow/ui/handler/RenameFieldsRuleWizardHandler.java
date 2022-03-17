package eu.jsparrow.ui.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.rules.common.util.RefactoringUtil;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.LicenseUtilService;
import eu.jsparrow.ui.util.WizardHandlerUtil;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;
import eu.jsparrow.ui.wizard.semiautomatic.ConfigureRenameFieldsRuleWizard;

/**
 * Handler for semi-automatic rename public fields rule
 * 
 * @author Andreja Sambolec, Ardit Ymeri
 * @since 2.3.0
 *
 */
public class RenameFieldsRuleWizardHandler extends AbstractRuleWizardHandler {

	private static final Logger logger = LoggerFactory.getLogger(RenameFieldsRuleWizardHandler.class);

	private LicenseUtilService licenseUtil = LicenseUtil.get();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (Activator.isRunning()) {
			openAlreadyRunningDialog();
			return null;
		}

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

		if (selectedJavaElements.isEmpty()) {
			WizardMessageDialog.synchronizedWithUIShowWarningNoCompilationUnitDialog();
			logger.error(Messages.WizardMessageDialog_selectionDidNotContainAnyJavaFiles);
			Activator.setRunning(false);
			return null;
		}

		if (selectedJavaElements.size() != 1) {
			String title = Messages.RenameFieldsRuleWizardHandler_multipleProjectsSelected;
			String message = Messages.RenameFieldsRuleWizardHandler_renamingRuleOnOneProjectOnly;
			synchronizeWithUIShowSelectionErrorMessage(title, message);
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
					synchronizeWithUIShowCompilationErrorMessage(errorIcus, errorFreeIcus, ConfigureRenameFieldsRuleWizard::new);
				} else if (!errorFreeIcus.isEmpty()) {
					synchronizeWithUIShowRuleWizard(errorFreeIcus, ConfigureRenameFieldsRuleWizard::new);
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
	}
}
