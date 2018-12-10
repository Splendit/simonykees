package eu.jsparrow.ui.startup.registration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.startup.registration.entity.RegistrationEntity;

public class RegistrationControl {

	private Composite parentComposite;

	private RegistrationFormField firstName;
	private RegistrationFormField lastName;
	private RegistrationFormField email;
	private RegistrationFormField company;

	private RegistrationFormCheckBox emailAgreeCheckBox;
	private RegistrationFormCheckBox dsgvoAgreeCheckBox;
	private RegistrationFormCheckBox licenseAgreeLCheckBox;
	private RegistrationFormCheckBox newsletterAgreeCheckBox;

	public RegistrationControl(Composite parent, int style) {
		parentComposite = new Composite(parent, style);
		GridLayout titleLayout = new GridLayout(1, false);
		parentComposite.setLayout(titleLayout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		parentComposite.setLayoutData(gridData);

		gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		gridData.verticalIndent = 5;
		Label introLabel = new Label(parentComposite, SWT.NONE);
		introLabel.setLayoutData(gridData);
		introLabel.setText(Messages.RegistrationControl_introText);

		createUserDataForm(parentComposite);

		createConditionsAgreementsForm(parentComposite);
	}

	private void createUserDataForm(Composite composite) {
		Group formGroup = new Group(composite, SWT.NONE);
		GridData groupGridData = new GridData(GridData.FILL_BOTH);
		groupGridData.verticalIndent = 15;
		formGroup.setLayoutData(groupGridData);
		GridLayout formGroupLayout = new GridLayout(2, false);
		formGroup.setLayout(formGroupLayout);

		firstName = new RegistrationFormField(formGroup, Messages.RegistrationControl_firstNameLabel);
		lastName = new RegistrationFormField(formGroup, Messages.RegistrationControl_lastNameLabel);
		email = new RegistrationFormField(formGroup, Messages.RegistrationControl_emailLabel) {
			@Override
			protected void validate(Text text) {
				setValid(validateEmail(text.getText()));
			}
		};
		company = new RegistrationFormField(formGroup, Messages.RegistrationControl_companyLabel) {
			@Override
			protected void validate(Text text) {
				setValid(true);
			}

			@Override
			protected void updateDecoVisibility() {
				// do nothing, this field is not mandatory
			}
		};
	}

	private void createConditionsAgreementsForm(Composite composite) {
		Group conditionsGroup = new Group(composite, SWT.BORDER_DASH);
		conditionsGroup.setText(Messages.RegistrationControl_termsGroupTitle);
		GridData groupGridData = new GridData(GridData.FILL_BOTH);
		groupGridData.verticalIndent = 10;
		conditionsGroup.setLayoutData(groupGridData);
		conditionsGroup.setLayout(new GridLayout(1, false));

		emailAgreeCheckBox = new RegistrationFormCheckBox(conditionsGroup, Messages.RegistrationControl_emailAgreeText);
		dsgvoAgreeCheckBox = new RegistrationFormCheckBox(conditionsGroup, Messages.RegistrationControl_gpdrAgreeText);
		licenseAgreeLCheckBox = new RegistrationFormCheckBox(conditionsGroup,
				Messages.RegistrationControl_licenseAgreeText);
		newsletterAgreeCheckBox = new RegistrationFormCheckBox(conditionsGroup,
				Messages.RegistrationControl_newsletterAgreeText) {
			@Override
			protected void addCheckBoxChangeListener() {
				// do nothing, this is not mandatory field
			}
		};

		GridData newsletterCheckBoxTextGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		newsletterCheckBoxTextGridData.widthHint = 415;
		newsletterCheckBoxTextGridData.heightHint = 70;
		newsletterAgreeCheckBox.setLayoutData(newsletterCheckBoxTextGridData);
		newsletterAgreeCheckBox.setSelection(true);

		createButtonsBar(composite);
	}

	private void createButtonsBar(Composite parentComposite) {
		GridData statusLabelGridData = new GridData(SWT.RIGHT, SWT.BOTTOM, false, true);
		Composite statusLabelComposite = new Composite(parentComposite, SWT.NONE);
		GridLayout statusLabelRowLayout = new GridLayout(1, false);
		statusLabelComposite.setLayout(statusLabelRowLayout);
		statusLabelComposite.setLayoutData(statusLabelGridData);

		Label statusLabel = new Label(statusLabelComposite, SWT.NONE);
		statusLabel.setText(Messages.RegistrationControl_statusText);
		statusLabel.setVisible(false);

		GridData buttonGridData = new GridData(SWT.RIGHT, SWT.BOTTOM, false, false);
		Composite buttonRowComposite = new Composite(parentComposite, SWT.NONE);
		GridLayout buttonRowLayout = new GridLayout(2, false);
		buttonRowComposite.setLayout(buttonRowLayout);
		buttonRowComposite.setLayoutData(buttonGridData);

		GridData buttonData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		buttonData.widthHint = 90;

		Button cancelButton = new Button(buttonRowComposite, SWT.PUSH);
		cancelButton.setText(Messages.RegistrationControl_cancelButton);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				((Button) event.getSource()).getShell()
					.close();
			}
		});
		cancelButton.setLayoutData(buttonData);

		Button registerButton = new Button(buttonRowComposite, SWT.PUSH);
		registerButton.setText(Messages.RegistrationControl_registerButton);
		registerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkMandatoryFieldsAndUpdateDecorations();
				if (!areMandatorySatisfied()) {
					return;
				}
				statusLabel.setVisible(true);
				RegistrationEntity registrationData = new RegistrationEntity(firstName.getText()
					.getText(),
						lastName.getText()
							.getText(),
						email.getText()
							.getText(),
						company.getText()
							.getText(),
						newsletterAgreeCheckBox.getSelection());
				if (sendData()) {
					showLicenseGenerationSucceededDialog();
				} else {
					showLicenseGenerationFailedDialog();
				}
				statusLabel.setVisible(false);
			}
		});
		registerButton.setLayoutData(buttonData);
	}

	private boolean sendData() {
		// TODO send data and wait for response, return whether it was
		// successful
		return true;
	}

	private void checkMandatoryFieldsAndUpdateDecorations() {
		firstName.updateDecoVisibility();
		lastName.updateDecoVisibility();
		email.updateDecoVisibility();

		emailAgreeCheckBox.updateDecoVisibility(true);
		dsgvoAgreeCheckBox.updateDecoVisibility(true);
		licenseAgreeLCheckBox.updateDecoVisibility(true);
	}

	private boolean areMandatorySatisfied() {
		return validateFields() && validateCheckboxes();
	}

	private boolean validateFields() {
		return firstName.isValid() && lastName.isValid() && email.isValid();
	}

	private boolean validateEmail(String email) {
		String regex = "^\\S*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$"; //$NON-NLS-1$
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(email);

		return matcher.matches();
	}

	private boolean validateCheckboxes() {
		return emailAgreeCheckBox.getSelection() && dsgvoAgreeCheckBox.getSelection()
				&& licenseAgreeLCheckBox.getSelection();
	}

	private void showLicenseGenerationSucceededDialog() {
		if (SimonykeesMessageDialog.openMessageDialog(Display.getCurrent()
			.getActiveShell(),
				Messages.RegistrationControl_registrationSuccessfulText + System.lineSeparator()
						+ Messages.RegistrationControl_checkEmailForLicenseText,
				MessageDialog.INFORMATION)) {
			TabFolder tabParent = (TabFolder) getControl().getParent();
			tabParent.setSelection(1);
		}

	}

	private void showLicenseGenerationFailedDialog() {
		if (SimonykeesMessageDialog.openMessageDialog(Display.getCurrent()
			.getActiveShell(),
				Messages.RegistrationControl_serverUnreachableText + System.lineSeparator()
						+ Messages.RegistrationControl_checkInternetText,
				MessageDialog.ERROR)) {

			resetToDefaultSelection();
		}

	}

	/**
	 * Get parent composite as control for the registration tab
	 * 
	 * @return parent composite as control
	 */
	public Control getControl() {
		return parentComposite;
	}

	public void resetToDefaultSelection() {
		emailAgreeCheckBox.setSelection(false);
		dsgvoAgreeCheckBox.setSelection(false);
		licenseAgreeLCheckBox.setSelection(false);
		newsletterAgreeCheckBox.setSelection(true);

		emailAgreeCheckBox.resetDecoVisibility();
		dsgvoAgreeCheckBox.resetDecoVisibility();
		licenseAgreeLCheckBox.resetDecoVisibility();
	}
}
