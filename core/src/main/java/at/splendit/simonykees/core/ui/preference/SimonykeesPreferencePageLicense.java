package at.splendit.simonykees.core.ui.preference;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

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
import org.eclipse.swt.events.DisposeListener;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.i18n.ExceptionMessages;
import at.splendit.simonykees.i18n.Messages;
import at.splendit.simonykees.license.api.LicenseValidationService;

/**
 * Preference page for displaying license information and updating license key.
 * 
 * @author Ardit Ymeri, Andreja Sambolec, Matthias Webhofer
 * @since 1.0
 *
 */
public class SimonykeesPreferencePageLicense extends PreferencePage implements IWorkbenchPreferencePage {

	private static final Logger logger = LoggerFactory.getLogger(SimonykeesPreferencePageLicense.class);

	private Label licenseLabel;
	private Label licenseStatusLabel;
	private Button updateButton;

	private Image jSparrowImageActive;
	private Image jSparrowImageInactive;
	private Label logoLabel;

	private static final int LICENSE_LABEL_MAX_WIDTH = 370;

	private static final String LOGO_PATH_ACTIVE = "icons/jSparrow_FIN_2_scaled.png"; //$NON-NLS-1$
	private static final String LOGO_PATH_INACTIVE = "icons/jSparrow_FIN_3_scaled.png"; //$NON-NLS-1$

	@Inject
	private LicenseValidationService licenseValidationService;
	private boolean isLicenseValidationServiceAvailable = false;

	public SimonykeesPreferencePageLicense() {
		super();
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		ContextInjectionFactory.inject(this, Activator.getEclipseContext());
	}

	@PostConstruct
	private void postConstruct() {
		if (licenseValidationService != null)
			isLicenseValidationServiceAvailable = true;
	}

	@PreDestroy
	private void preDestroy() {
		isLicenseValidationServiceAvailable = false;
	}

	@Override
	public void init(IWorkbench arg0) {
		// TODO Auto-generated method stub
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
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(parent.getFont()).setStyle(SWT.BOLD);
		licenseStatusLabel.setFont(boldDescriptor.createFont(composite.getDisplay()));
		licenseStatusLabel.setForeground(display.getSystemColor(SWT.COLOR_RED));
		licenseStatusLabel.setVisible(true);

		updateButton = new Button(composite, SWT.PUSH);
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
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(arg0.text));
				} catch (PartInitException | MalformedURLException e) {
					// nothing...
				}
			}

		});

		updateDisplayedInformation();

		updateButton.setVisible(true);

		composite.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				jSparrowImageActive.dispose();
				jSparrowImageInactive.dispose();
			}
		});

		composite.pack();
		return composite;
	}

	private void updateDisplayedInformation() {
		if (isLicenseValidationServiceAvailable) {
			licenseLabel.setText(licenseValidationService.getDisplayableLicenseInformation());

			if (!licenseValidationService.isValid()) {
				licenseStatusLabel.setText(licenseValidationService.getLicenseStautsUserMessage());
				logoLabel.setImage(jSparrowImageInactive);
			} else {
				licenseStatusLabel.setText(""); //$NON-NLS-1$
				logoLabel.setImage(jSparrowImageActive);
			}
		} else {
			// TODO: proper error handling
			logger.error(ExceptionMessages.SimonykeesPreferencePageLicense_license_service_unavailable);
		}

		licenseLabel.getParent().pack();
		licenseLabel.getParent().layout(true);
	}

}
