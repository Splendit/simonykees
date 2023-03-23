package eu.jsparrow.ui.preview;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.dialog.ObtainLicenseButtonData;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.wizard.AbstractRefactoringWizardDialog;

/**
 * Intended to be used for preview wizards which are subclasses of
 * {@link AbstractPreviewWizard}.
 * 
 * @since 4.15.0
 */
public class PreviewWizardDialog extends AbstractRefactoringWizardDialog {

	public static final int SUMMARY_BUTTON_ID = 9;

	public PreviewWizardDialog(Shell parentShell, AbstractPreviewWizard previewWizard) {
		super(parentShell, previewWizard);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, ObtainLicenseButtonData.BUTTON_ID_UNLOCK_PREMIUM_RULES,
				ObtainLicenseButtonData.BUTTON_TEXT_UNLOCK_PREMIUM_RULES, false);
		if (needsSummaryButton()) {
			createButton(parent, SUMMARY_BUTTON_ID, Messages.SelectRulesWizard_Summary, false);
		}
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.FINISH_ID).setText("Commit"); //$NON-NLS-1$
		updateButtonsForButtonBar();
	}

	protected void updateOnCommit() {
		Button buttonSummary = getButton(SUMMARY_BUTTON_ID);
		if (buttonSummary != null) {
			buttonSummary.setVisible(false);
		}

		getButton(IDialogConstants.CANCEL_ID).setVisible(false);

		Button buttonNext = getButton(IDialogConstants.NEXT_ID);
		if (buttonNext != null) {
			buttonNext.setVisible(false);
		}
		Button buttonBack = getButton(IDialogConstants.BACK_ID);
		if (buttonBack != null) {
			buttonBack.setVisible(false);
		}
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == SUMMARY_BUTTON_ID) {
			summaryButtonPressed();
		} else {
			super.buttonPressed(buttonId);
		}
	}

	@Override
	protected void nextPressed() {
		((AbstractPreviewWizard) getWizard()).pressedNext();
		super.nextPressed();
	}

	@Override
	protected void backPressed() {
		((AbstractPreviewWizard) getWizard()).pressedBack();
		super.backPressed();
	}

	@Override
	protected void finishPressed() {
		summaryButtonPressed();
		super.finishPressed();
	}

	protected void summaryButtonPressed() {
		((AbstractPreviewWizard) getWizard()).showSummaryPage();
	}

	private boolean needsSummaryButton() {
		return ((AbstractPreviewWizard) getWizard()).needsSummaryPage();
	}

	@Override
	protected void updateButtonsForButtonBar() {
		boolean enableCommitButton = !LicenseUtil.get()
			.isFreeLicense();
		getButton(IDialogConstants.FINISH_ID).setEnabled(enableCommitButton);
		super.updateButtonsForButtonBar();
	}
}
