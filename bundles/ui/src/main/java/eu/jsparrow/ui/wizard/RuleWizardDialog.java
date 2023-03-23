package eu.jsparrow.ui.wizard;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import eu.jsparrow.ui.preview.AbstractPreviewWizard;

/**
 * Intended to be used for preview wizards which are subclasses of
 * {@link AbstractPreviewWizard}.
 * 
 * @since 4.17.0
 */
public class RuleWizardDialog extends WizardDialog {

	public RuleWizardDialog(Shell parentShell, AbstractRuleWizard newWizard) {
		super(parentShell, newWizard);
	}
	
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
}
