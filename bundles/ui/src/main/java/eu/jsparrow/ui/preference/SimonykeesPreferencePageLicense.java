package eu.jsparrow.ui.preference;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.eclipse.core.runtime.*;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.osgi.framework.Bundle;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.LicenseUtilService;

/**
 * Preference page for displaying license information and updating license key.
 * 
 * @author Ardit Ymeri, Andreja Sambolec, Matthias Webhofer
 * @since 1.0
 *
 */
public class SimonykeesPreferencePageLicense extends PreferencePage implements IWorkbenchPreferencePage {

	private static final int LICENSE_LABEL_MAX_WIDTH = 370;

	private static final String LOGO_PATH_ACTIVE = "icons/jSparrow_FIN_2_scaled.png"; //$NON-NLS-1$

	private static final String LOGO_PATH_INACTIVE = "icons/jSparrow_FIN_3_scaled.png"; //$NON-NLS-1$

	private static final String DATE_FORMAT_PATTERN = "MMMM dd, yyyy"; //$NON-NLS-1$

	private Label licenseLabel;

	private Label licenseStatusLabel;

	private Image jSparrowImageActive;

	private Image jSparrowImageInactive;

	private Label logoLabel;

	private LicenseUtilService licenseUtil = LicenseUtil.get();

	public SimonykeesPreferencePageLicense() {
		super();

		ContextInjectionFactory.inject(this, Activator.getEclipseContext());

		Activator.setRunning(true);
		setPreferenceStore(Activator.getDefault()
			.getPreferenceStore());
		noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(Composite parent) {
		Display display = getShell().getDisplay();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, true);
		gd.verticalSpan = 5;
		composite.setLayoutData(gd);
		composite.setLayout(new RowLayout(SWT.VERTICAL));

		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		IPath iPathActive = new Path(LOGO_PATH_ACTIVE);
		URL urlActive = FileLocator.find(bundle, iPathActive, new HashMap<>());
		ImageDescriptor imageDescActive = ImageDescriptor.createFromURL(urlActive);
		jSparrowImageActive = imageDescActive.createImage();

		IPath iPathInactive = new Path(LOGO_PATH_INACTIVE);
		URL urlInactive = FileLocator.find(bundle, iPathInactive, new HashMap<>());
		ImageDescriptor imageDescInactive = ImageDescriptor.createFromURL(urlInactive);
		jSparrowImageInactive = imageDescInactive.createImage();

		logoLabel = new Label(composite, SWT.NONE);
		logoLabel.setImage(jSparrowImageActive);

		licenseLabel = new Label(composite, SWT.LEFT | SWT.WRAP);
		licenseLabel.setVisible(true);
		RowData licenseRowData = new RowData();
		licenseRowData.width = LICENSE_LABEL_MAX_WIDTH;
		licenseLabel.setLayoutData(licenseRowData);
		licenseLabel.setFont(parent.getFont());

		Link jSparrowLink = new Link(composite, SWT.NONE);
		jSparrowLink.setFont(parent.getFont());
		jSparrowLink.setText(Messages.SimonykeesPreferencePageLicense_to_obtain_new_license_visit_jsparrow);

		licenseStatusLabel = new Label(composite, SWT.NONE);
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(parent.getFont())
			.setStyle(SWT.BOLD);
		licenseStatusLabel.setFont(boldDescriptor.createFont(composite.getDisplay()));
		licenseStatusLabel.setForeground(display.getSystemColor(SWT.COLOR_RED));
		licenseStatusLabel.setVisible(true);

		Button updateButton = new Button(composite, SWT.PUSH);
		updateButton.setText(Messages.SimonykeesPreferencePageLicense_update_license_key_button);
		updateButton.setFont(parent.getFont());
		updateButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {

				SimonykeesUpdateLicenseDialog dialog = new SimonykeesUpdateLicenseDialog(getShell());
				dialog.create();
				dialog.open();
				updateDisplayedInformation();
			}

		});

		jSparrowLink.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					PlatformUI.getWorkbench()
						.getBrowserSupport()
						.getExternalBrowser()
						.openURL(new URL(arg0.text));
				} catch (PartInitException | MalformedURLException e) {
					// nothing...
				}
			}

		});

		updateDisplayedInformation();

		updateButton.setVisible(true);

		composite.addDisposeListener((DisposeEvent e) -> {
			jSparrowImageActive.dispose();
			jSparrowImageInactive.dispose();
		});

		composite.pack();
		return composite;
	}

	private void updateDisplayedInformation() {
		LicenseValidationResult result = licenseUtil.getValidationResult();

		String licenseModelInfo = getLicenseModelString(result);
		licenseLabel.setText(licenseModelInfo);

		setLicenseStatusMessage(result);

		licenseLabel.getParent()
			.pack();
		licenseLabel.getParent()
			.layout(true);
	}

	private void setLicenseStatusMessage(LicenseValidationResult result) {
		if (result.isValid()) {
			licenseStatusLabel.setText(""); //$NON-NLS-1$
			logoLabel.setImage(jSparrowImageActive);
		} else {
			String invalidReason = result.getDetail();
			licenseStatusLabel.setText(invalidReason);
			logoLabel.setImage(jSparrowImageInactive);
		}
	}

	private String getLicenseModelString(LicenseValidationResult result) {		
		if(result.getLicenseType() != LicenseType.DEMO && !result.isValid()) {
			return ""; //$NON-NLS-1$
		}

		StringBuilder licenseModelString = new StringBuilder();
		licenseModelString.append(Messages.SimonykeesPreferencePageLicense_jsparrow_licensed_as);
		if (result.getLicenseType() == LicenseType.DEMO) {
			licenseModelString.append(Messages.SimonykeesPreferencePageLicense_freeLicense);
		} else {
			licenseModelString.append(Messages.SimonykeesPreferencePageLicense_fulLicense);
			licenseModelString.append(Messages.SimonykeesPreferencePageLicense_under_key_label);
			licenseModelString.append(result.getKey());
			licenseModelString.append(". "); //$NON-NLS-1$
		} 
		
		licenseModelString.append(Messages.SimonykeesPreferencePageLicense_jsparrow_valid_until);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
		licenseModelString.append(result.getExpirationDate()
			.format(formatter));
		licenseModelString.append("."); //$NON-NLS-1$

		return licenseModelString.toString();
	}

	@Override
	public boolean performOk() {
		Activator.setRunning(false);
		return super.performOk();
	}

	@Override
	public boolean performCancel() {
		Activator.setRunning(false);
		return super.performCancel();
	}

	@Override
	public void init(IWorkbench workbench) {
		// Required by super class

	}

}
