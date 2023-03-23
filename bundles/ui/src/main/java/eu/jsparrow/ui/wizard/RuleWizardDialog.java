package eu.jsparrow.ui.wizard;

import java.util.Arrays;
import java.util.List;

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
import eu.jsparrow.ui.dialog.JSparrowPricingLink;
import eu.jsparrow.ui.dialog.ObtainLicenseButtonData;
import eu.jsparrow.ui.preference.SimonykeesUpdateLicenseDialog;
import eu.jsparrow.ui.preview.AbstractPreviewWizard;
import eu.jsparrow.ui.util.LicenseUtil;

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
	 * Removed unnecessary empty space on the bottom of the wizard intended for
	 * ProgressMonitor that is not used(non-Javadoc)
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

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, ObtainLicenseButtonData.BUTTON_ID_UNLOCK_PREMIUM_RULES,
				ObtainLicenseButtonData.BUTTON_TEXT_UNLOCK_PREMIUM_RULES, false);
		super.createButtonsForButtonBar(parent);

		Button finish = getButton(IDialogConstants.FINISH_ID);
		finish.setText(Messages.SelectRulesWizardHandler_finishButtonText);
		setButtonLayoutData(finish);
		updateButtonsForButtonBar();
	}

	protected void updateButtonsForButtonBar() {
		boolean showEnterPremiumLicenseKey = false;
		LicenseUtil licenseUtil = LicenseUtil.get();
		if (licenseUtil.isFreeLicense()) {
			showEnterPremiumLicenseKey = true;
		}
		getButton(ObtainLicenseButtonData.BUTTON_ID_UNLOCK_PREMIUM_RULES)
			.setVisible(showEnterPremiumLicenseKey);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == ObtainLicenseButtonData.BUTTON_ID_UNLOCK_PREMIUM_RULES) {
			showSimonykeesUpdateLicenseDialog(JSparrowPricingLink.UNLOCK_ALL_PREMIUM_RULES);
		} else {
			super.buttonPressed(buttonId);
		}
	}

	public void showSimonykeesUpdateLicenseDialog(JSparrowPricingLink linkToPricingPage) {
		List<Runnable> afterLicenseUpdateListeners = Arrays.asList(this::updateButtonsForButtonBar);
		SimonykeesUpdateLicenseDialog dialog = new SimonykeesUpdateLicenseDialog(getShell(), linkToPricingPage,
				afterLicenseUpdateListeners);
		dialog.create();
		dialog.open();
	}
}
