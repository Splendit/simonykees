package eu.jsparrow.ui.preference;

import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.cleanslate.validation.ValidationStatus;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.LicenseUtil.LicenseUpdateResult;

/**
 * Dialog for updating license key.
 * 
 * @author Ardit Ymeri, Andreja Sambolec, Matthias Webhofer
 * @since 1.0
 *
 */
public class SimonykeesUpdateLicenseDialog extends TitleAreaDialog {

	private static final String DEFAULT_LICENSEE_NAME = ""; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(SimonykeesUpdateLicenseDialog.class);
	private static final String LOGO_ACTIVE_LICENSE_PATH = "icons/jSparrow_active_icon_100.png"; //$NON-NLS-1$
	private static final String LOGO_INACTIVE_LICENSE_PATH = "icons/jSparrow_inactive_icon_100.png"; //$NON-NLS-1$
	private static final String TICKMARK_GREEN_ICON_PATH = "icons/if_Tick_Mark_20px.png"; //$NON-NLS-1$
	private static final String CLOSE_RED_ICON_PATH = "icons/if_Close_Icon_20px.png"; //$NON-NLS-1$
	private Text licenseKeyText;
	private String licenseKey = ""; //$NON-NLS-1$
	private CLabel updatedLabel;
	private Label updatedIconLabel;
	private Image scaledJSparrowImageActive;
	private Image scaledJSparrowImageInactive;
	private Image scaledTickmarkGreenIconImage;
	private Image scaledCloseRedIconImage;

	private LicenseUtil licenseUtil = LicenseUtil.get();

	protected SimonykeesUpdateLicenseDialog(Shell parentShell) {
		super(parentShell);
		ContextInjectionFactory.inject(this, Activator.getEclipseContext());
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.SimonykeesUpdateLicenseDialog_update_license_dialog_title);
		setMessage(Messages.SimonykeesUpdateLicenseDialog_update_license_dialog_message, IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		/*
		 * Setting help listener to question mark help button Open default help
		 * dialog
		 */
		area.addHelpListener((HelpEvent e) -> SimonykeesMessageDialog.openDefaultHelpMessageDialog(getShell()));

		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.WRAP, true, true));

		GridLayout licenceMainLayout = new GridLayout(2, false);
		container.setLayout(licenceMainLayout);

		// enter new key group
		Group newKeyGroup = new Group(container, SWT.NONE);
		newKeyGroup.setText(Messages.SimonykeesUpdateLicenseDialog_update_license_dialog_group_title);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 2;
		newKeyGroup.setLayoutData(gridData);

		GridLayout groupGridLayout = new GridLayout(3, false);
		newKeyGroup.setLayout(groupGridLayout);

		GridData groupGridData = new GridData(SWT.CENTER, SWT.FILL, false, false);
		groupGridData.horizontalIndent = 10;
		groupGridData.verticalIndent = 5;

		Label licenseKeyLabel = new Label(newKeyGroup, SWT.NONE);
		licenseKeyLabel.setText(Messages.SimonykeesUpdateLicenseDialog_update_license_dialog_label);
		licenseKeyLabel.setLayoutData(groupGridData);

		groupGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		groupGridData.horizontalIndent = 20;
		licenseKeyText = new Text(newKeyGroup, SWT.BORDER);
		licenseKeyText.setLayoutData(groupGridData);
		licenseKeyText.addModifyListener((ModifyEvent event) -> {
			Text textWidget = (Text) event.getSource();
			licenseKey = textWidget.getText();
		});

		groupGridData = new GridData(SWT.CENTER, SWT.FILL, false, false);
		groupGridData.horizontalIndent = 20;

		Button updateButton = new Button(newKeyGroup, SWT.PUSH);
		updateButton.setLayoutData(groupGridData);
		updateButton.setText(Messages.SimonykeesUpdateLicenseDialog_update_llicense_dialog_button);
		updateButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				BusyIndicator.showWhile(Display.getDefault(), () -> {
					
					LicenseUpdateResult result = LicenseUtil.get().update(licenseKey);
					if (!result.wasSuccessful()) {
						updatedLabel.setImage(scaledCloseRedIconImage);
						updatedIconLabel.setImage(scaledJSparrowImageInactive);

					} else {
						updatedLabel.setImage(scaledTickmarkGreenIconImage);
						updatedIconLabel.setImage(scaledJSparrowImageActive);
						
					}
					updatedLabel.setText(result.getDetailMessage());
					updatedLabel.setVisible(true);
					updatedIconLabel.setVisible(true);

					updatedIconLabel.getParent()
						.layout();
				});
			}
		});

		updatedIconLabel = new Label(container, SWT.NONE);
		gridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		gridData.verticalIndent = 5;
		updatedIconLabel.setLayoutData(gridData);

		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

		IPath iPathActive = new Path(LOGO_ACTIVE_LICENSE_PATH);
		URL urlActive = FileLocator.find(bundle, iPathActive, new HashMap<>());
		ImageDescriptor imageDescActive = ImageDescriptor.createFromURL(urlActive);
		Image jSparrowImageActive = imageDescActive.createImage();
		ImageData imageDataActive = jSparrowImageActive.getImageData();
		scaledJSparrowImageActive = new Image(container.getDisplay(), imageDataActive);

		IPath iPathInactive = new Path(LOGO_INACTIVE_LICENSE_PATH);
		URL urlInactive = FileLocator.find(bundle, iPathInactive, new HashMap<>());
		ImageDescriptor imageDescInactive = ImageDescriptor.createFromURL(urlInactive);
		Image jSparrowImageInactive = imageDescInactive.createImage();
		ImageData imageDataInactive = jSparrowImageInactive.getImageData();
		scaledJSparrowImageInactive = new Image(container.getDisplay(), imageDataInactive);

		updatedIconLabel.setImage(scaledJSparrowImageActive);
		updatedIconLabel.setVisible(true);
		
		updatedLabel = new CLabel(container, SWT.NONE);
		gridData = new GridData(SWT.LEFT, SWT.CENTER, true, true);
		gridData.verticalIndent = 5;
		gridData.horizontalIndent = 10;
		updatedLabel.setLayoutData(gridData);

		IPath iPathTickMarkGreen = new Path(TICKMARK_GREEN_ICON_PATH);
		URL urlTickMarkGreen = FileLocator.find(bundle, iPathTickMarkGreen, new HashMap<>());
		ImageDescriptor imageDescTickMarkGreen = ImageDescriptor.createFromURL(urlTickMarkGreen);
		Image tickmarkGreenIconImage = imageDescTickMarkGreen.createImage();
		ImageData imageDataTickmarkGreen = tickmarkGreenIconImage.getImageData();
		scaledTickmarkGreenIconImage = new Image(container.getDisplay(), imageDataTickmarkGreen);

		IPath iPathCloseRed = new Path(CLOSE_RED_ICON_PATH);
		URL urlCloseRed = FileLocator.find(bundle, iPathCloseRed, new HashMap<>());
		ImageDescriptor imageDescCloseRed = ImageDescriptor.createFromURL(urlCloseRed);
		Image closeRedIconImage = imageDescCloseRed.createImage();
		ImageData imageDataCloseRed = closeRedIconImage.getImageData();
		scaledCloseRedIconImage = new Image(container.getDisplay(), imageDataCloseRed);

		updatedLabel.setImage(scaledTickmarkGreenIconImage);
		updatedLabel.setText(Messages.SimonykeesUpdateLicenseDialog_license_updated_successfully);
		updatedLabel.setVisible(false);

		/*
		 * Automatic release does not work for Image, so we do it manually when
		 * container is disposed
		 */
		container.addDisposeListener((DisposeEvent e) -> {
			scaledJSparrowImageActive.dispose();
			scaledJSparrowImageInactive.dispose();
			scaledTickmarkGreenIconImage.dispose();
			scaledCloseRedIconImage.dispose();
		});

		return container;
	}

	private void updateWarningInformation(boolean updated) {
		if (!updated) {
			updatedLabel.setText(Messages.SimonykeesUpdateLicenseDialog_invalid_license_key);
			updatedLabel.setImage(scaledCloseRedIconImage);
			updatedLabel.setVisible(true);

			updatedIconLabel.setImage(scaledJSparrowImageInactive);
			updatedIconLabel.setVisible(true);
		} else {
			updatedLabel.setText(Messages.SimonykeesUpdateLicenseDialog_license_updated_successfully);
			updatedLabel.setImage(scaledTickmarkGreenIconImage);
			updatedLabel.setVisible(true);

			updatedIconLabel.setImage(scaledJSparrowImageActive);
			updatedIconLabel.setVisible(true);
		}

		updatedIconLabel.getParent()
			.layout();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(550, 350);
	}

	@Override
	protected void okPressed() {
		this.licenseKey = licenseKeyText.getText();
		super.okPressed();
	}

	public String getLicenseKey() {
		return licenseKey;
	}

}
