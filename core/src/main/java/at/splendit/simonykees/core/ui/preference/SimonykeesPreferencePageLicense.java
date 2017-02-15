package at.splendit.simonykees.core.ui.preference;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

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
	private Label licenseStatusLabel;
	private Button updateButton;
	
	private static final double JSPARROW_LOGO_SCALE = 0.333333;
	private static final int LICENSE_LABEL_MAX_WIDTH = 370;

	private static final String DATE_FORMAT_PATTERN = "MMMM dd, yyyy"; //$NON-NLS-1$
	private static final String LOGO_PATH = "icons/jSparrow_FIN_2.png"; //$NON-NLS-1$

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

		Display display = getShell().getDisplay();
		Composite composite = new Composite(getFieldEditorParent(), SWT.NONE);
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, true);
		gd.verticalSpan = 5;
		composite.setLayoutData(gd);
		composite.setLayout(new RowLayout(SWT.VERTICAL));
		
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		IPath iPath = new Path(LOGO_PATH);
		URL url = FileLocator.find(bundle, iPath, new HashMap<>());
		ImageDescriptor imageDesc = ImageDescriptor.createFromURL(url);
		Image jSparrowImage = imageDesc.createImage();
		ImageData imageData = jSparrowImage.getImageData();
		int scaledWidth = (int) (JSPARROW_LOGO_SCALE * imageData.width);
		int scaledHeight = (int) (JSPARROW_LOGO_SCALE * imageData.height);
		Image scaledJSparrowImage = new Image(composite.getDisplay(), imageData.scaledTo(scaledWidth, scaledHeight));
		Label logoLabel = new Label(composite, SWT.NONE);
		logoLabel.setImage(scaledJSparrowImage);

		licenseLabel = new Label(composite, SWT.LEFT | SWT.WRAP);
		licenseLabel.setVisible(true);
		RowData licenseRowData = new RowData();
		licenseRowData.width = LICENSE_LABEL_MAX_WIDTH;
		licenseLabel.setLayoutData(licenseRowData);
		
		Link jSparrowLink = new Link(composite, SWT.NONE);
		jSparrowLink.setText(Messages.SimonykeesPreferencePageLicense_to_obtain_new_license_visit_jsparrow);
		
		licenseStatusLabel = new Label(composite, SWT.NONE);
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(licenseStatusLabel.getFont()).setStyle(SWT.BOLD);
		Font boldFont = boldDescriptor.createFont(composite.getDisplay());
		Color red = new Color (display, 255, 0, 0);
		licenseStatusLabel.setFont(boldFont);
		licenseStatusLabel.setForeground(red);
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
		
		jSparrowLink.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(arg0.text));
				} catch (PartInitException | MalformedURLException e) {
					// nothing... 
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// no need to override this one
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
			} else {
				licenseLabelText += "."; //$NON-NLS-1$
			}
			licenseLabelText += " " //$NON-NLS-1$
			+ Messages.SimonykeesPreferencePageLicense_jsparrow_valid_until 
			+ extractDateFormat(expireationDate)
			+ "."; //$NON-NLS-1$
			licenseLabel.setText(licenseLabelText);
		}

		if(!licenseData.isValid()) {			
			licenseStatusLabel.setText(status.getUserMessage());
		} else {
			licenseStatusLabel.setText("");; //$NON-NLS-1$
		}
		licenseLabel.getParent().pack();
		licenseLabel.getParent().layout(true);
	}
}
