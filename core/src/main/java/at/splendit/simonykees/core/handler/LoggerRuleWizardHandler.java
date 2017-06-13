package at.splendit.simonykees.core.handler;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.rule.impl.standardLogger.StandardLoggerRule;
import at.splendit.simonykees.core.ui.LicenseUtil;
import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;
import at.splendit.simonykees.core.ui.wizard.semiautomatic.LoggerRuleWizard;
import at.splendit.simonykees.core.visitor.semiAutomatic.StandardLoggerASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * Handler for semi-automatic logging rule
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
public class LoggerRuleWizardHandler extends AbstractSimonykeesHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (Activator.isRunning()) {
			SimonykeesMessageDialog.openMessageDialog(Display.getDefault().getActiveShell(),
					Messages.SelectRulesWizardHandler_allready_running, MessageDialog.INFORMATION);
		} else {
			Activator.setRunning(true);

			if (LicenseUtil.getInstance().isValid()) {
				List<IJavaElement> selectedJavaElements = getSelectedJavaElements(event);
				if (!selectedJavaElements.isEmpty()) {
					IJavaProject selectedJavaProjekt = selectedJavaElements.get(0).getJavaProject();
					StandardLoggerRule loggerRule = new StandardLoggerRule(StandardLoggerASTVisitor.class);

					if (null != selectedJavaProjekt) {
						loggerRule.calculateEnabledForProject(selectedJavaProjekt);
						if (loggerRule.isEnabled()) {

							if (SimonykeesMessageDialog.openConfirmDialog(HandlerUtil.getActiveShell(event),
									NLS.bind(Messages.LoggerRuleWizardHandler_info_supportedFrameworkFound,
											loggerRule.getAvailableLoggerType()))) {
								final WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event),
										new LoggerRuleWizard(selectedJavaElements, loggerRule)) {
									/*
									 * Removed unnecessary empty space on the
									 * bottom of the wizard intended for
									 * ProgressMonitor that is not
									 * used(non-Javadoc)
									 * 
									 * @see
									 * org.eclipse.jface.wizard.WizardDialog#
									 * createDialogArea(org.eclipse.swt.widgets.
									 * Composite)
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
							} else {
								Activator.setRunning(false);
							}
						} else {
							SimonykeesMessageDialog.openMessageDialog(HandlerUtil.getActiveShell(event),
									Messages.LoggerRuleWizardHandler_noLogger, MessageDialog.WARNING);
							Activator.setRunning(false);
						}

					}
				}
			} else {
				/*
				 * do not display the Wizard if the license is invalid
				 */
				final Shell shell = HandlerUtil.getActiveShell(event);
				LicenseUtil.getInstance().displayLicenseErrorDialog(shell);
				Activator.setRunning(false);
			}
		}
		return null;
	}

}
