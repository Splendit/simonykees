package eu.jsparrow.ui.handler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.LicenseUtilService;
import eu.jsparrow.ui.util.WizardHandlerUtil;
import eu.jsparrow.ui.wizard.AbstractRuleWizard;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;
import eu.jsparrow.ui.wizard.semiautomatic.RemoveUnusedCodeWizard;

public class RemoveUnusedCodeWizardHandler extends AbstractRuleWizardHandler {

	private static final Logger logger = LoggerFactory.getLogger(RemoveUnusedCodeWizardHandler.class);

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

		Job job = new Job("Find dead code") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Function<List<ICompilationUnit>, AbstractRuleWizard> wizardGenerator = RemoveUnusedCodeWizard::new;
				return startRuleWizard(selectedJavaElements, monitor, wizardGenerator);
			}
		};

		job.setUser(true);
		job.schedule();
		return true;
	}
}
