package eu.jsparrow.ui.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.rules.common.util.RefactoringUtil;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.CompilationErrorsMessageDialog;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.wizard.AbstractRuleWizard;
import eu.jsparrow.ui.wizard.RuleWizardDialog;
import eu.jsparrow.ui.wizard.impl.SelectRulesWizard;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

/**
 * A parent class for handlers that collect the selected compilation units and
 * start the rule refactoring wizards.
 * 
 * @since 4.8.0
 *
 */
public abstract class AbstractRuleWizardHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(AbstractRuleWizardHandler.class);

	protected void openAlreadyRunningDialog() {
		Display display = Display.getDefault();
		Shell activeShell = display.getActiveShell();
		SimonykeesMessageDialog.openMessageDialog(activeShell, Messages.SelectRulesWizardHandler_allready_running,
				MessageDialog.INFORMATION);
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
	protected boolean getCompilationUnits(List<ICompilationUnit> resultCompilationUnitsList,
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

	/**
	 * Method used to open SelectRulesWizard from non UI thread
	 */
	protected void synchronizeWithUIShowRuleWizard(List<ICompilationUnit> selectedJavaElements,
			Function<List<ICompilationUnit>, AbstractRuleWizard> wizardGenerator) {
		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				final RuleWizardDialog dialog = new RuleWizardDialog(shell, wizardGenerator.apply(selectedJavaElements));
				dialog.open();
			});
	}

	/**
	 * Method used to open CompilationErrorsMessageDialog from non UI thread to
	 * list all Java files that will be skipped because they contain compilation
	 * errors.
	 */
	protected void synchronizeWithUIShowCompilationErrorMessage(List<ICompilationUnit> containingErrorList,
			List<ICompilationUnit> errorFreeICus,
			Function<List<ICompilationUnit>, AbstractRuleWizard> wizardGenerator) {
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
						synchronizeWithUIShowRuleWizard(errorFreeICus, wizardGenerator);
					} else {
						WizardMessageDialog.synchronizeWithUIShowWarningNoComlipationUnitWithoutErrorsDialog();
					}
				} else {

					Activator.setRunning(false);
				}
			});
	}

	protected void synchronizeWithUIShowSelectionErrorMessage(String title, String message) {
		Display.getDefault()
			.syncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				MessageDialog.openWarning(shell, title, message);

				Activator.setRunning(false);
			});
	}

	protected IStatus startRuleWizard(Map<IJavaProject, List<IJavaElement>> selectedJavaElements,
			IProgressMonitor monitor, Function<List<ICompilationUnit>, AbstractRuleWizard> wizardGenerator) {
		List<IJavaElement> selectedElements = selectedJavaElements.entrySet()
			.iterator()
			.next()
			.getValue();

		List<ICompilationUnit> iCompilationUnits = new ArrayList<>();

		boolean success = getCompilationUnits(iCompilationUnits, selectedElements, monitor);
		if (!success) {
			return Status.CANCEL_STATUS;
		}

		List<ICompilationUnit> errorIcus = iCompilationUnits.stream()
			.filter(RefactoringUtil::checkForSyntaxErrors)
			.collect(Collectors.toList());

		List<ICompilationUnit> errorFreeIcus = iCompilationUnits.stream()
			.filter(icu -> !errorIcus.contains(icu))
			.collect(Collectors.toList());

		if (!errorIcus.isEmpty()) {
			synchronizeWithUIShowCompilationErrorMessage(errorIcus, errorFreeIcus, wizardGenerator);
		} else if (!errorFreeIcus.isEmpty()) {
			synchronizeWithUIShowRuleWizard(errorFreeIcus, wizardGenerator);
		} else {
			logger.warn(ExceptionMessages.RefactoringPipeline_warn_no_compilation_units_found);
			WizardMessageDialog.synchronizeWithUIShowInfo(new RefactoringException(
					ExceptionMessages.RefactoringPipeline_warn_no_compilation_units_found,
					ExceptionMessages.RefactoringPipeline_user_warn_no_compilation_units_found));
		}

		return Status.OK_STATUS;
	}
}
