package eu.jsparrow.ui.wizard;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

import eu.jsparrow.ui.dialog.JSparrowPricingLink;
import eu.jsparrow.ui.dialog.ObtainLicenseButtonData;
import eu.jsparrow.ui.preference.SimonykeesUpdateLicenseDialog;
import eu.jsparrow.ui.util.LicenseUtil;

/**
 * @since 4.17.0
 */
public abstract class AbstractRefactoringWizardDialog extends WizardDialog {

	protected AbstractRefactoringWizardDialog(Shell parentShell, AbstractRefactoringWizard newWizard) {
		super(parentShell, newWizard);
	}

	protected void updateButtonsForButtonBar() {
		Button unlockPremiumRulesButton = getButton(ObtainLicenseButtonData.BUTTON_ID_UNLOCK_PREMIUM_RULES);
		if (unlockPremiumRulesButton != null) {
			boolean showEnterPremiumLicenseKey = LicenseUtil.get()
				.isFreeLicense();
			unlockPremiumRulesButton.setVisible(showEnterPremiumLicenseKey);
		}
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
