package eu.jsparrow.ui.wizard.impl;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.util.LicenseUtil;

public class SelectRulesWizardDialog extends WizardDialog {

	private static final int BUTTON_ID_REGISTER_FOR_A_FREE_TRIAL = 11001;
	private static final int BUTTON_ID_ENTER_PREMIUM_LICENSE_KEY = 11002;
	
	private final Runnable lambdaShowRegistrationDialog;
	private final Runnable lambdaShowSimonykeesUpdateLicenseDialog;

	public SelectRulesWizardDialog(Shell parentShell, SelectRulesWizard selectRulesWizard) {
		super(parentShell, selectRulesWizard);
		selectRulesWizard.addLicenseUpdateListener(this::updateButtonsForButtonBar);
		lambdaShowRegistrationDialog = selectRulesWizard::showRegistrationDialog;
		lambdaShowSimonykeesUpdateLicenseDialog = selectRulesWizard::showSimonykeesUpdateLicenseDialog;
	}

	/*
	 * Removed unnecessary empty space on the bottom of the
	 * wizard intended for ProgressMonitor that is not used
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

	/**
	 * Creates new shell defined for this wizard. The dialog is
	 * made as big enough to show rule description vertically
	 * and horizontally to avoid two scrollers. Minimum size is
	 * set to avoid loosing components from view.
	 * 
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setSize(1000, 1000);
		newShell.setMinimumSize(680, 600);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, BUTTON_ID_REGISTER_FOR_A_FREE_TRIAL, "Register for a free trial", false);
		createButton(parent, BUTTON_ID_ENTER_PREMIUM_LICENSE_KEY, "Enter premium license key", false);
		super.createButtonsForButtonBar(parent);

		Button finish = getButton(IDialogConstants.FINISH_ID);
		finish.setText(Messages.SelectRulesWizardHandler_finishButtonText);
		setButtonLayoutData(finish);
		updateButtonsForButtonBar();
	}

	private void updateButtonsForButtonBar() {
		boolean showRegisterForAFreeTrial = false;
		boolean showEnterPremiumLicenseKey = false;
		LicenseUtil licenseUtil = LicenseUtil.get();
		if (licenseUtil.isFreeLicense()) {
			if (!licenseUtil.isActiveRegistration()) {
				showRegisterForAFreeTrial = true;
			}
			showEnterPremiumLicenseKey = true;
		}
		getButton(BUTTON_ID_REGISTER_FOR_A_FREE_TRIAL).setVisible(showRegisterForAFreeTrial);
		getButton(BUTTON_ID_ENTER_PREMIUM_LICENSE_KEY).setVisible(showEnterPremiumLicenseKey);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == BUTTON_ID_REGISTER_FOR_A_FREE_TRIAL) {
			lambdaShowRegistrationDialog.run();
		} else if (buttonId == BUTTON_ID_ENTER_PREMIUM_LICENSE_KEY) {
			lambdaShowSimonykeesUpdateLicenseDialog.run();
		} else {
			super.buttonPressed(buttonId);
		}
	}
}