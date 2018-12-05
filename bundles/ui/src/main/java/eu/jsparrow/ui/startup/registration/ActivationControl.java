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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.startup.registration.entity.ActivationEntity;

public class ActivationControl {

	private Composite parentComposite;

	private Text licenseText;
	private String licenseKeyString = ""; //$NON-NLS-1$

	private Label invalidLicenseLabel;
	private Label statusLabel;
	private Button activateButton;

	public ActivationControl(Composite parent, int style) {
		parentComposite = new Composite(parent, style);
		GridLayout titleLayout = new GridLayout(1, false);
		parentComposite.setLayout(titleLayout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		parentComposite.setLayoutData(gridData);

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
			licenseKeyString = ((Text) e.getSource()).getText();
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
				if (!validateLicenseKey()) {
					invalidLicenseLabel.setVisible(true);
					return;
				}
				statusLabel.setVisible(true);
				ActivationEntity activationData = new ActivationEntity(licenseKeyString);
				// TODO send license key and wait for response
				statusLabel.setVisible(false);

				// TODO check if response is valid
				// if license is valid
				if (validateLicenseKey()) {
					showLicenseValidDialog();
				} else {
					showInvalidLicenseDialog();
				}
			}
		});
		activateButton.setLayoutData(buttonData);

	}

	public boolean validateLicenseKey() {
		String regex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"; //$NON-NLS-1$
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(licenseText.getText());

		return matcher.matches();

	}

	private void updateEnabledActivateButton() {
		activateButton.setEnabled(null != licenseKeyString && licenseKeyString.length() >= 1);
	}

	private void showLicenseValidDialog() {
		if (SimonykeesMessageDialog.openMessageDialog(Display.getCurrent()
			.getActiveShell(), Messages.ActivationControl_successfulActivationText, MessageDialog.INFORMATION)) {
			getControl().getShell()
				.close();
		}
	}

	private void showInvalidLicenseDialog() {
		if (SimonykeesMessageDialog.openMessageDialog(Display.getCurrent()
			.getActiveShell(),
				Messages.ActivationControl_activationFailedText + System.lineSeparator()
						+ Messages.ActivationControl_licenseValidityExplanationText,
				MessageDialog.ERROR)) {
			licenseText.setText(""); //$NON-NLS-1$
			resetToDefaultSelection();
		}
	}

	/**
	 * Get parent composite as control for the activation tab
	 * 
	 * @return parent composite as control
	 */
	public Control getControl() {
		return parentComposite;
	}

	public void resetToDefaultSelection() {
		invalidLicenseLabel.setVisible(false);
	}
}
