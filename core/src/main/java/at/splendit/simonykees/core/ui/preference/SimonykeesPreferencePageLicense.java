package at.splendit.simonykees.core.ui.preference;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.license.LicenseChecker;
import at.splendit.simonykees.core.license.LicenseManager;
import at.splendit.simonykees.core.license.LicenseType;

/**
 * Preference page for displaying license information and updating license key.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class SimonykeesPreferencePageLicense extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private Label licenseLabel;
	private Label expiresLabel;

	private static final String DATE_FORMAT_PATTERN = "MMMM dd, yyyy"; //$NON-NLS-1$
	private static final String DEFAULT_LICENSEE_NAME = ""; //$NON-NLS-1$

	public SimonykeesPreferencePageLicense() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	public void init(IWorkbench arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void createFieldEditors() {

		Composite composite = new Composite(getFieldEditorParent(), SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(7, true));

		Composite licenseGroup = new Composite(composite, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		licenseGroup.setLayoutData(gridData);
		licenseGroup.setLayout(new GridLayout(5, true));

		licenseLabel = new Label(licenseGroup, SWT.NONE);
		licenseLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 5, 1));
		licenseLabel.setVisible(true);

		expiresLabel = new Label(licenseGroup, SWT.PUSH);
		expiresLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 5, 1));
		expiresLabel.setVisible(true);

		Composite updateGroup = new Composite(composite, SWT.NONE);
		GridData updateGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, true);
		updateGridData.verticalSpan = 20;
		updateGroup.setLayoutData(updateGridData);
		updateGroup.setLayout(new RowLayout(SWT.HORIZONTAL));

		Button updateButton = new Button(updateGroup, SWT.PUSH);
		updateButton.setText(Messages.SimonykeesPreferencePageLicense_update_license_key_button);
		updateButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {

				SimonykeesUpdateLicenseDialog dialog = new SimonykeesUpdateLicenseDialog(getShell());
				dialog.create();

				// get the new license key from the dialog
				if (dialog.open() == Window.OK) {
					String licenseKey = dialog.getLicenseKey();
					LicenseManager licenseManager = LicenseManager.getInstance();
					licenseManager.updateLicenseeNumber(licenseKey, DEFAULT_LICENSEE_NAME);
					updateDisplayedInformation();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
			}
		});

		updateDisplayedInformation();

		updateButton.setVisible(true);
		composite.pack();
	}

	private String extractDateFormat(ZonedDateTime date) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
		String strDate = date.format(formatter);

		return strDate;
	}

	private void updateDisplayedInformation() {
		LicenseChecker licenseData = LicenseManager.getInstance().getValidationData();
		LicenseType licenseType = licenseData.getType();
		ZonedDateTime expireationDate = licenseData.getExpirationDate();

		licenseLabel.setText(Messages.SimonykeesPreferencePageLicense_jsparrow_licensed_as + licenseType.toString());
		expiresLabel.setText(
				Messages.SimonykeesPreferencePageLicense_jsparrow_valid_untill + extractDateFormat(expireationDate));
	}

}
