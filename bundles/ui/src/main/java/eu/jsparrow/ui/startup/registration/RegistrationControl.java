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
		introLabel.setText("Register and get FREE rules.");

		createUserDataForm(parentComposite);

		createConditionsAgreementsForm(parentComposite);
	}

	private void createUserDataForm(Composite composite) {
		Group formGroup = new Group(composite, SWT.NONE);
		GridData groupGridData = new GridData(GridData.FILL_BOTH);
		groupGridData.verticalIndent = 15;
		formGroup.setLayoutData(groupGridData);
		GridLayout formGroupLayout = new GridLayout(2, true);
		formGroup.setLayout(formGroupLayout);

		firstName = new RegistrationFormField(formGroup, "First name");
		lastName = new RegistrationFormField(formGroup, "Last name");
		email = new RegistrationFormField(formGroup, "Email") {
			@Override
			protected void isValid(Text text) {
				setValid(validateEmail(text.getText()));
			}
		};
		company = new RegistrationFormField(formGroup, "Company") {
			@Override
			protected void isValid(Text text) {
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
		conditionsGroup.setText("Terms and conditions");
		GridData groupGridData = new GridData(GridData.FILL_BOTH);
		groupGridData.verticalIndent = 10;
		conditionsGroup.setLayoutData(groupGridData);
		conditionsGroup.setLayout(new GridLayout(1, false));

		emailAgreeCheckBox = new RegistrationFormCheckBox(conditionsGroup,
				"I agree to being contacted via email with license key for free rule activation.");
		dsgvoAgreeCheckBox = new RegistrationFormCheckBox(conditionsGroup, "I agree to DSGVO text.");
		licenseAgreeLCheckBox = new RegistrationFormCheckBox(conditionsGroup,
				"I agree with terms and conditions of the License Agreement");
		newsletterAgreeCheckBox = new RegistrationFormCheckBox(conditionsGroup,
				"I agree to receive the jSparrow newsletter about new product features, "
						+ "special offers and interesting information about Java refactoring and improving code quality") {
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

		createRegisterButton(composite);
	}

	private void createRegisterButton(Composite parentComposite) {
		GridData buttonGridData = new GridData(SWT.RIGHT, SWT.BOTTOM, false, true);
		Composite buttonRowComposite = new Composite(parentComposite, SWT.NONE);
		GridLayout buttonRowLayout = new GridLayout(2, false);
		buttonRowComposite.setLayout(buttonRowLayout);
		buttonRowComposite.setLayoutData(buttonGridData);

		Label statusLabel = new Label(buttonRowComposite, SWT.NONE);
		statusLabel.setText("Contacting Server . . . ");
		statusLabel.setVisible(false);

		Button registerButton = new Button(buttonRowComposite, SWT.PUSH);
		registerButton.setText("Register");
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
				// TODO send license key and wait for response
				statusLabel.setVisible(false);
				// TODO show appropriate dialog regarding received response

				// if license generation succeeded
				if (true) {
					showLicenseGenerationSucceededDialog();
				} else {
					showLicenseGenerationFailedDialog();
				}
			}
		});
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
		String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
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
				"Registration has completed!" + System.lineSeparator()
						+ "Please check your email to activate jSparrow Freemium.",
				MessageDialog.INFORMATION)) {
			TabFolder tabParent = (TabFolder) getControl().getParent();
			tabParent.setSelection(1);
		}

	}

	private void showLicenseGenerationFailedDialog() {
		if (SimonykeesMessageDialog.openMessageDialog(Display.getCurrent()
			.getActiveShell(),
				"Server cannot be reached." + System.lineSeparator()
						+ "Please check your internet connection and try again later.",
				MessageDialog.ERROR)) {
			emailAgreeCheckBox.setSelection(false);
			dsgvoAgreeCheckBox.setSelection(false);
			licenseAgreeLCheckBox.setSelection(false);
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
}
