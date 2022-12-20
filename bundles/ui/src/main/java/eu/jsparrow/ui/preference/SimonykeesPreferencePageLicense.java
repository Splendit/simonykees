package eu.jsparrow.ui.preference;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.JSparrowPricingLink;
import eu.jsparrow.ui.startup.registration.RegistrationDialog;
import eu.jsparrow.ui.util.LicenseUtil;

/**
 * Preference page for displaying license information and updating license key.
 * 
 * @author Ardit Ymeri, Andreja Sambolec, Matthias Webhofer
 * @since 1.0
 *
 */
public class SimonykeesPreferencePageLicense extends PreferencePage implements IWorkbenchPreferencePage {
	private static final String FORMAT_ICONS_PATH = "icons/%s"; //$NON-NLS-1$

	private static final String JSPARROW_LOGO_PIRATE_HAT = "jsparrow-logo-alternative-blue-small.png"; //$NON-NLS-1$

	private static final int LICENSE_LABEL_MAX_WIDTH = 370;

	static final String LOGO_ACTIVE_LICENSE_PATH = String.format(FORMAT_ICONS_PATH, JSPARROW_LOGO_PIRATE_HAT);

	static final String LOGO_INACTIVE_LICENSE_PATH = String.format(FORMAT_ICONS_PATH, JSPARROW_LOGO_PIRATE_HAT);

	private static final String DATE_FORMAT_PATTERN = "MMMM dd, yyyy"; //$NON-NLS-1$

	private Label licenseLabel;

	private Label expirationLabel;

	private Image jSparrowImageActive;

	private Image jSparrowImageInactive;

	private Label logoLabel;

	private Button registerForFreeButton;

	Link jSparrowLink;

	private LicenseUtil licenseUtil = LicenseUtil.get();

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
		IPath iPathActive = new Path(LOGO_ACTIVE_LICENSE_PATH);
		URL urlActive = FileLocator.find(bundle, iPathActive, new HashMap<>());
		ImageDescriptor imageDescActive = ImageDescriptor.createFromURL(urlActive);
		jSparrowImageActive = imageDescActive.createImage();

		IPath iPathInactive = new Path(LOGO_INACTIVE_LICENSE_PATH);
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

		registerForFreeButton = new Button(composite, SWT.PUSH);
		registerForFreeButton.setText(Messages.SimonykeesPreferencePageLicense_register_for_free_jsparrow_trial);
		registerForFreeButton.setFont(parent.getFont());
		registerForFreeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				RegistrationDialog dialog = new RegistrationDialog(getShell());
				dialog.create();
				dialog.open();
				updateDisplayedInformation();
			}
		});

		jSparrowLink = new Link(composite, SWT.NONE);
		jSparrowLink.setFont(parent.getFont());
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

		expirationLabel = new Label(composite, SWT.NONE);
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(parent.getFont())
			.setStyle(SWT.BOLD);
		expirationLabel.setFont(boldDescriptor.createFont(composite.getDisplay()));
		expirationLabel.setForeground(display.getSystemColor(SWT.COLOR_RED));
		expirationLabel.setVisible(true);

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

		updateDisplayedInformation();
		updateButton.setVisible(true);
		composite.addDisposeListener((DisposeEvent e) -> {
			jSparrowImageActive.dispose();
			jSparrowImageInactive.dispose();
			expirationLabel.getFont()
				.dispose();
		});

		composite.pack();
		return composite;
	}

	private void updateDisplayedInformation() {
		LicenseValidationResult result = licenseUtil.getValidationResult();
		String licenseModelInfo = computeLicenseLabel(result);

		licenseLabel.setText(licenseModelInfo);
		boolean freeWithStarter = !licenseUtil.isProLicense() && licenseUtil.isActiveRegistration();
		if (!result.isValid() && !freeWithStarter) {
			expirationLabel.setText(result.getDetail());
			logoLabel.setImage(jSparrowImageInactive);
		} else {
			expirationLabel.setText(""); //$NON-NLS-1$
			logoLabel.setImage(jSparrowImageActive);
		}

		registerForFreeButton.setVisible(isButtonToRegisterForFreeVisible(result));
		jSparrowLink.setText(computeJSparrowLinkText(result).getText());

		licenseLabel.getParent()
			.pack();
		licenseLabel.getParent()
			.layout(true);
	}

	private JSparrowPricingLink computeJSparrowLinkText(LicenseValidationResult result) {
		boolean isFullLicense = licenseUtil.isProLicense();
		boolean isValid = result.isValid();
		if (isFullLicense && isValid) {
			return JSparrowPricingLink.OBTAIN_NEW_LICENSE;
		}
		return JSparrowPricingLink.TO_GET_FULL_ACCESS_UPGRADE_LICENSE;
	}

	private boolean isButtonToRegisterForFreeVisible(LicenseValidationResult result) {
		boolean isFullLicense = licenseUtil.isProLicense();
		boolean activeRegistration = licenseUtil.isActiveRegistration();
		boolean isValid = result.isValid();
		boolean fullValid = isFullLicense && isValid;
		return !fullValid && !activeRegistration;
	}

	private String computeLicenseLabel(LicenseValidationResult result) {
		boolean isFullLicense = licenseUtil.isProLicense();
		boolean activeRegistration = licenseUtil.isActiveRegistration();
		boolean isValid = result.isValid();
		boolean fullValid = isFullLicense && isValid;

		if (!fullValid) {
			return activeRegistration ? Messages.SimonykeesPreferencePageLicense_jsparrow_free
					: Messages.SimonykeesPreferencePageLicense_currently_not_registered;
		}

		LicenseType licenseType = result.getLicenseType();
		if (licenseType == LicenseType.PAY_PER_USE) {
			Integer availableCredit = result.getCredit()
				.orElse(0);
			return String.format(Messages.SimonykeesPreferencePageLicense_jsparrow_pay_per_use_available_credit,
					result.getKey(), availableCredit);
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
		ZonedDateTime expireDate = result.getExpirationDate();
		String formattedExpireDate = expireDate.format(formatter);
		return String.format(Messages.SimonykeesPreferencePageLicense_jsparrow_pro_valid_until, result.getKey(),
				formattedExpireDate);
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
