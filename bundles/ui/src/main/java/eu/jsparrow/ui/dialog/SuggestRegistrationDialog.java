package eu.jsparrow.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.startup.registration.RegistrationDialog;
import eu.jsparrow.ui.util.LicenseUtil;

/**
 * Dialog that shows when user has free license and is not registered for free
 * rules
 * 
 * @since 3.0.0
 *
 */
public class SuggestRegistrationDialog extends Dialog {

	private LicenseUtil licenseUtil = LicenseUtil.get();

	public SuggestRegistrationDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite composite) {
		Composite area = (Composite) super.createDialogArea(composite);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 10;
		area.setLayout(gridLayout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		area.setLayoutData(gridData);

		Label titleLabel = new Label(area, SWT.NONE);
		titleLabel.setText(Messages.SuggestRegistrationDialog_noFreemiumLiceseWarning);

		Label descriptionLabel = new Label(area, SWT.NONE);
		descriptionLabel.setText(Messages.SuggestRegistrationDialog_descriptionOfFreemiumLicense);

		Label offerLabel = new Label(area, SWT.NONE);
		offerLabel.setText(Messages.SuggestRegistrationDialog_suggestToRegister);

		GridData checkBoxTextGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		checkBoxTextGridData.widthHint = 300;
		Button dontShowAgainCheckBox = new Button(area, SWT.CHECK | SWT.WRAP);
		dontShowAgainCheckBox.setText(Messages.SuggestRegistrationDialog_dontShowAgainCheckbox);
		dontShowAgainCheckBox.setLayoutData(checkBoxTextGridData);
		dontShowAgainCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button source = (Button) e.getSource();
				licenseUtil.openSuggestRegistrationDialogAgain(!source.getSelection());
			}
		});

		return composite;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.SuggestRegistrationDialog_getFreeRulesTitle);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, 7, Messages.SuggestRegistrationDialog_registerButtonText, true);
		createButton(parent, IDialogConstants.OK_ID, Messages.SuggestRegistrationDialog_skipButtonText, false);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == 7) {
			registerButtonPressed();
		} else {
			super.buttonPressed(buttonId);
		}
	}

	private void registerButtonPressed() {
		licenseUtil.setShouldContinueWithSelectRules(false);
		PlatformUI.getWorkbench()
			.getDisplay()
			.asyncExec(() -> {
				Shell activeShell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				new RegistrationDialog(activeShell).open();
			});
		this.close();
	}
}
