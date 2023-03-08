package eu.jsparrow.ui.preference;

import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.framework.Bundle;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.JSparrowPricingLink;
import eu.jsparrow.ui.dialog.ObtainLicenseButtonData;
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

	private Button updateButton;

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
		Image jSparrowImageActive = imageDescActive.createImage();

		Label logoLabel = new Label(composite, SWT.NONE);
		logoLabel.setImage(jSparrowImageActive);

		expirationLabel = new Label(composite, SWT.NONE);
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(parent.getFont())
			.setStyle(SWT.BOLD);
		expirationLabel.setFont(boldDescriptor.createFont(composite.getDisplay()));
		expirationLabel.setForeground(display.getSystemColor(SWT.COLOR_RED));
		expirationLabel.setVisible(true);

		licenseLabel = new Label(composite, SWT.LEFT | SWT.WRAP);
		licenseLabel.setVisible(true);
		RowData licenseRowData = new RowData();
		licenseRowData.width = LICENSE_LABEL_MAX_WIDTH;
		licenseLabel.setLayoutData(licenseRowData);
		licenseLabel.setFont(parent.getFont());

		updateButton = new Button(composite, SWT.PUSH);
		updateButton.setFont(parent.getFont());
		updateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				SimonykeesUpdateLicenseDialog dialog = new SimonykeesUpdateLicenseDialog(getShell(),
						computeJSparrowLink(), new ArrayList<>());
				dialog.create();
				dialog.open();
				updateDisplayedInformation();
			}
		});
		updateButton.setVisible(true);
		updateDisplayedInformation();

		composite.addDisposeListener((DisposeEvent e) -> {
			jSparrowImageActive.dispose();
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
		boolean freeWithStarter = !licenseUtil.isProLicense();
		if (!result.isValid() && !freeWithStarter) {
			expirationLabel.setText(result.getDetail());
		} else {
			expirationLabel.setText(""); //$NON-NLS-1$
		}

		licenseLabel.getParent()
			.pack();
		licenseLabel.getParent()
			.layout(true);

		if (licenseUtil.isFreeLicense()) {
			updateButton.setText(ObtainLicenseButtonData.BUTTON_TEXT_UNLOCK_PREMIUM_RULES);
		} else {
			updateButton.setText(ObtainLicenseButtonData.BUTTON_TEXT_OBTAIN_NEW_LICENSE);
		}
	}

	private JSparrowPricingLink computeJSparrowLink() {
		if (licenseUtil.isFreeLicense()) {
			return JSparrowPricingLink.UNLOCK_ALL_PREMIUM_RULES;
		}
		return JSparrowPricingLink.OBTAIN_NEW_LICENSE;
	}

	private String computeLicenseLabel(LicenseValidationResult result) {
		boolean isFullLicense = licenseUtil.isProLicense();
		boolean isValid = result.isValid();
		boolean fullValid = isFullLicense && isValid;

		if (!fullValid) {
			return Messages.SimonykeesPreferencePageLicense_jsparrow_free;
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
