package eu.jsparrow.ui.startup.registration;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.startup.registration.entity.ActivationEntity;

public class ActivationControl {

	private Composite parentComposite;

	private Text licenseText;
	private String licenseKeyString = "";

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
		Label introLabel = new Label(parentComposite, SWT.NONE);
		introLabel.setLayoutData(labelGridData);
		introLabel.setText("Enter the license key received by email:");

		createLicenseInputArea(parentComposite);
	}

	private void createLicenseInputArea(Composite composite) {
		licenseText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		GridData licenseTextGridData = new GridData(GridData.FILL_HORIZONTAL);
		licenseTextGridData.heightHint = 80;
		licenseTextGridData.horizontalIndent = 5;
		licenseTextGridData.verticalIndent = 5;
		licenseText.setLayoutData(licenseTextGridData);
		licenseText.addModifyListener((ModifyEvent e) -> {
			licenseKeyString = ((Text) e.getSource()).getText();
			invalidLicenseLabel.setVisible(!validateLicenseKey());
			updateEnabledActivateButton();
		});
		Listener scrollBarListener = (Event event) -> {
			Text t = (Text) event.widget;
			Rectangle r1 = t.getClientArea();
			Rectangle r2 = t.computeTrim(r1.x, r1.y, r1.width, r1.height);
			Point p = t.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			t.getVerticalBar()
				.setVisible(r2.height <= p.y);
			if (event.type == SWT.Modify) {
				t.getParent()
					.layout(true);
				t.showSelection();
			}
		};
		licenseText.addListener(SWT.Resize, scrollBarListener);
		licenseText.addListener(SWT.Modify, scrollBarListener);

		invalidLicenseLabel = new Label(composite, SWT.NONE);
		GridData licenseLabelGridData = new GridData(GridData.FILL_HORIZONTAL);
		licenseLabelGridData.heightHint = 80;
		licenseLabelGridData.horizontalIndent = 5;
		licenseLabelGridData.verticalIndent = 5;
		invalidLicenseLabel.setLayoutData(licenseLabelGridData);
		invalidLicenseLabel.setText("Please provide a valid license key");
		invalidLicenseLabel.setVisible(false);

		createActivateButton(composite);

		updateEnabledActivateButton();
	}

	private void createActivateButton(Composite composite) {
		GridData buttonGridData = new GridData(SWT.RIGHT, SWT.BOTTOM, false, true);
		Composite buttonRowComposite = new Composite(composite, SWT.NONE);
		GridLayout buttonRowLayout = new GridLayout(2, false);
		buttonRowComposite.setLayout(buttonRowLayout);
		buttonRowComposite.setLayoutData(buttonGridData);

		statusLabel = new Label(buttonRowComposite, SWT.NONE);
		statusLabel.setText("Contacting Server . . . ");
		statusLabel.setVisible(false);

		activateButton = new Button(buttonRowComposite, SWT.PUSH);
		activateButton.setText("Activate");
		activateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				statusLabel.setVisible(true);
				ActivationEntity activationData = new ActivationEntity(licenseKeyString);
				// TODO send license key and wait for response
				statusLabel.setVisible(false);
				// TODO show appropriate dialog regarding received response

				// if license is valid
				if (true) {
					showLicenseValidDialog();
				} else {
					// else
					showInvalidLicenseDialog();
				}
			}
		});
	}

	public boolean validateLicenseKey() {
		// TODO validate key, general validation (length/form)

		return (null != licenseKeyString && licenseKeyString.length() >= 1);
	}

	private void updateEnabledActivateButton() {
		activateButton.setEnabled(validateLicenseKey());
	}

	private void showLicenseValidDialog() {
		if (SimonykeesMessageDialog.openMessageDialog(Display.getCurrent()
			.getActiveShell(), "Your activation was successful! Enjoy using free rules and improving your code base.",
				MessageDialog.INFORMATION)) {
			getControl().getShell()
				.close();
		}
	}

	private void showInvalidLicenseDialog() {
		if (SimonykeesMessageDialog.openMessageDialog(Display.getCurrent()
			.getActiveShell(),
				"Activation failed. Please verify you entered a valid license key." + System.lineSeparator()
						+ "The license key is valid for only one activation. If you have already used the license key, please register again to get a new license key.",
				MessageDialog.ERROR)) {
			licenseText.setText("");
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
}
