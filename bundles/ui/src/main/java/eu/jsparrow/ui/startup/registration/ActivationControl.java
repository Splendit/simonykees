package eu.jsparrow.ui.startup.registration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.startup.registration.entity.ActivationEntity;
import eu.jsparrow.ui.util.LicenseUtil;

/**
 * Controller for activating the customer registration.
 * 
 * @since 3.0.0
 *
 */
public class ActivationControl extends Composite {

	private Composite parentComposite;

	private Text licenseText;
	private String licenseKeyString = ""; //$NON-NLS-1$

	private Label invalidLicenseLabel;
	private Label statusLabel;
	private Button activateButton;

	private Runnable lambdaAfterActivation;

	public ActivationControl(Composite parent, int style, Runnable lambdaAfterActivation) {
		super(parent, style);
		this.lambdaAfterActivation = lambdaAfterActivation;

		GridLayout overallLayout = new GridLayout();
		overallLayout.marginHeight = 0;
		overallLayout.marginWidth = 0;
		this.setLayout(overallLayout);
		this.setLayoutData(new GridData(GridData.FILL_BOTH));

		parentComposite = new Composite(this, style);
		parentComposite.setLayout(new GridLayout());
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridData labelGridData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		labelGridData.verticalIndent = 5;
		Label enterLicenseLabel = new Label(parentComposite, SWT.NONE);
		enterLicenseLabel.setLayoutData(labelGridData);
		enterLicenseLabel.setText(Messages.ActivationControl_enterLicenseLabel);

		createLicenseInputArea(parentComposite);
	}

	private void createLicenseInputArea(Composite composite) {
		licenseText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		GridData licenseTextGridData = new GridData(GridData.FILL_HORIZONTAL);
		licenseTextGridData.heightHint = 150;
		licenseTextGridData.verticalIndent = 5;
		licenseText.setLayoutData(licenseTextGridData);
		licenseText.addModifyListener((ModifyEvent e) -> {
			licenseKeyString = ((Text) e.getSource()).getText()
				.trim();
			invalidLicenseLabel.setVisible(false);
			updateEnabledActivateButton();
		});

		invalidLicenseLabel = new Label(composite, SWT.NONE);
		GridData licenseLabelGridData = new GridData(GridData.FILL_HORIZONTAL);
		licenseLabelGridData.heightHint = 80;
		licenseLabelGridData.horizontalIndent = 5;
		licenseLabelGridData.verticalIndent = 5;
		invalidLicenseLabel.setLayoutData(licenseLabelGridData);
		invalidLicenseLabel.setText(Messages.ActivationControl_invalidLicenseLabel);
		invalidLicenseLabel.setVisible(false);

		createButtonsBar(composite);

		updateEnabledActivateButton();
	}

	private void createButtonsBar(Composite composite) {
		GridData statusLabelGridData = new GridData(SWT.RIGHT, SWT.BOTTOM, false, true);
		Composite statusLabelComposite = new Composite(composite, SWT.NONE);
		GridLayout statusLabelRowLayout = new GridLayout(1, false);
		statusLabelComposite.setLayout(statusLabelRowLayout);
		statusLabelComposite.setLayoutData(statusLabelGridData);

		statusLabel = new Label(statusLabelComposite, SWT.NONE);
		statusLabel.setText(Messages.ActivationControl_statusLabel);
		statusLabel.setVisible(false);

		GridData buttonGridData = new GridData(SWT.RIGHT, SWT.BOTTOM, false, false);
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		GridLayout buttonRowLayout = new GridLayout(2, false);
		buttonComposite.setLayout(buttonRowLayout);
		buttonComposite.setLayoutData(buttonGridData);

		GridData buttonData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		buttonData.widthHint = 90;

		Button cancelButton = new Button(buttonComposite, SWT.PUSH);
		cancelButton.setText(Messages.ActivationControl_cancelButton);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				((Button) event.getSource()).getShell()
					.close();
			}
		});
		cancelButton.setLayoutData(buttonData);

		activateButton = new Button(buttonComposite, SWT.PUSH);
		activateButton.setText(Messages.ActivationControl_activateButton);
		activateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!validateLicenseKeyForm()) {
					invalidLicenseLabel.setVisible(true);
					return;
				}

				Display display = (Display.getCurrent() != null) ? Display.getCurrent() : Display.getDefault();

				activateButton.setEnabled(false);
				statusLabel.setVisible(true);

				ActivationEntity activationData = new ActivationEntity(licenseKeyString);

				new Thread(() -> {

					boolean valid = validateActivationKey(activationData);

					display.asyncExec(() -> {
						statusLabel.setVisible(false);

						// if license is valid
						if (valid) {
							showLicenseValidDialog(display);
							parentComposite.getShell()
								.close();
							LicenseUtil licenseUtil = LicenseUtil.get();
							licenseUtil.updateValidationResult();
							if (lambdaAfterActivation != null) {
								lambdaAfterActivation.run();
							}
							return;
						} else {
							showInvalidLicenseDialog(display);
						}

						activateButton.setEnabled(true);
					});

				}).start();

			}
		});
		activateButton.setLayoutData(buttonData);
	}

	private boolean validateActivationKey(ActivationEntity activationEntity) {
		LicenseUtil licenseUtil = LicenseUtil.get();
		return licenseUtil.activateRegistration(activationEntity);
	}

	public boolean validateLicenseKeyForm() {
		String regex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"; //$NON-NLS-1$
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(licenseKeyString);

		return matcher.matches();

	}

	private void updateEnabledActivateButton() {
		activateButton.setEnabled(null != licenseKeyString && licenseKeyString.length() >= 1);
	}

	private void showLicenseValidDialog(Display display) {
		SimonykeesMessageDialog.openMessageDialog(display.getActiveShell(),
				Messages.ActivationControl_successfulActivationText, MessageDialog.INFORMATION,
				Messages.ActivationControl_acitvationSuccessfulTitle);
	}

	private void showInvalidLicenseDialog(Display display) {
		if (SimonykeesMessageDialog.openMessageDialog(display.getActiveShell(),
				Messages.ActivationControl_activationFailedText + System.lineSeparator()
						+ Messages.ActivationControl_licenseValidityExplanationText,
				MessageDialog.ERROR, Messages.ActivationControl_activationFailedTitle)) {
			licenseText.setText(""); //$NON-NLS-1$
			resetToDefaultSelection();
		}
	}

	public void resetToDefaultSelection() {
		invalidLicenseLabel.setVisible(false);
	}
}
