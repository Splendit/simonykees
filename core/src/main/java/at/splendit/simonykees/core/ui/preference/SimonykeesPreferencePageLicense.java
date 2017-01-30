package at.splendit.simonykees.core.ui.preference;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
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
import at.splendit.simonykees.core.license.LicenseStatus;
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
	private Label licenseStatusLabel;
	private Button updateButton;

	private static final String DATE_FORMAT_PATTERN = "MMMM dd, yyyy"; //$NON-NLS-1$

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
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, true);
		gd.verticalSpan = 5;
		composite.setLayoutData(gd);
		composite.setLayout(new RowLayout(SWT.VERTICAL));

		licenseLabel = new Label(composite, SWT.NONE);
		licenseLabel.setVisible(true);

		expiresLabel = new Label(composite, SWT.NONE);
		expiresLabel.setVisible(true);
		
		licenseStatusLabel = new Label(composite, SWT.NONE);
		licenseStatusLabel.setVisible(true);

		updateButton = new Button(composite, SWT.PUSH);
		updateButton.setText(Messages.SimonykeesPreferencePageLicense_update_license_key_button);
		updateButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {

				SimonykeesUpdateLicenseDialog dialog = new SimonykeesUpdateLicenseDialog(getShell());
				dialog.create();
				dialog.open();
				updateDisplayedInformation();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// nothing
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
		LicenseManager licenseManger = LicenseManager.getInstance();
		LicenseChecker licenseData = licenseManger.getValidationData();
		LicenseType licenseType = licenseData.getType();
		ZonedDateTime expireationDate = licenseData.getExpirationDate();
		LicenseStatus status = licenseData.getLicenseStatus();

		if(licenseType != null && expireationDate != null) {
			String licenseLabelText = Messages.SimonykeesPreferencePageLicense_jsparrow_licensed_as + licenseType.getLicenseName();

			if(!LicenseType.TRY_AND_BUY.equals(licenseType)) {
				String licenseKey = licenseManger.getLicensee().getLicenseeNumber();
				licenseLabelText += 
						" " + Messages.SimonykeesPreferencePageLicense_under_key_label //$NON-NLS-1$
						+ " " + licenseKey //$NON-NLS-1$
						+ ".";  //$NON-NLS-1$
			}
			licenseLabel.setText(licenseLabelText);
			
			expiresLabel.setText(
					Messages.SimonykeesPreferencePageLicense_jsparrow_valid_until + extractDateFormat(expireationDate));
		}

		licenseStatusLabel.setText(status.getUserMessage());
		licenseLabel.getParent().pack();
		licenseLabel.getParent().layout(true);
	}
}
