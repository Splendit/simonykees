package at.splendit.simonykees.core.ui.preference;

import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.i18n.Messages;
import at.splendit.simonykees.license.LicenseManager;
import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;

/**
 * Dialog for updating license key.
 * 
 * @author Ardit Ymeri, Andreja Sambolec
 * @since 1.0
 *
 */
public class SimonykeesUpdateLicenseDialog extends TitleAreaDialog {

	private static final String DEFAULT_LICENSEE_NAME = ""; //$NON-NLS-1$

	private Text licenseKeyText;
	private String licenseKey = ""; //$NON-NLS-1$
	private Button updateButton;
	private Label updatedLabel;
	private Label updatedIconLabel;

	private Image scaledJSparrowImageActive;
	private Image scaledJSparrowImageInactive;

	private static final String LOGO_ACTIVE_LICENSE_PATH = "icons/jSparrow_active_icon_100.png"; //$NON-NLS-1$
	private static final String LOGO_INACTIVE_LICENSE_PATH = "icons/jSparrow_inactive_icon_100.png"; //$NON-NLS-1$

	protected SimonykeesUpdateLicenseDialog(Shell parentShell) {
		super(parentShell);
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
		area.addHelpListener(new HelpListener() {
			@Override
			public void helpRequested(HelpEvent e) {
				SimonykeesMessageDialog.openDefaultHelpMessageDialog(getShell());
			}
		});

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
		licenseKeyText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				updatedIconLabel.setVisible(false);
				updatedLabel.setVisible(false);
				Text textWidget = (Text) event.getSource();
				licenseKey = textWidget.getText();
			}
		});

		groupGridData = new GridData(SWT.CENTER, SWT.FILL, false, false);
		groupGridData.horizontalIndent = 20;
		updateButton = new Button(newKeyGroup, SWT.PUSH);
		updateButton.setLayoutData(groupGridData);
		updateButton.setText(Messages.SimonykeesUpdateLicenseDialog_update_llicense_dialog_button);
		updateButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
					public void run() {
						String licenseKey = getLicenseKey();
						LicenseManager licenseManager = LicenseManager.getInstance();
						boolean updated = licenseManager.updateLicenseeNumber(licenseKey.trim(), DEFAULT_LICENSEE_NAME);
						updateWarningInformation(updated);
					}
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
		updatedIconLabel.setVisible(false);

		updatedLabel = new Label(container, SWT.NONE);
		gridData = new GridData(SWT.LEFT, SWT.CENTER, true, true);
		gridData.verticalIndent = 5;
		gridData.horizontalIndent = 10;
		updatedLabel.setLayoutData(gridData);
		updatedLabel.setText(Messages.SimonykeesUpdateLicenseDialog_license_updated_successfully);
		updatedLabel.setVisible(false);

		/*
		 * Automatic release does not work for Image, so we do it manually when container is disposed 
		 */
		container.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				scaledJSparrowImageActive.dispose();
				scaledJSparrowImageInactive.dispose();
			}
		});
		
		return container;
	}

	private void updateWarningInformation(boolean updated) {
		Device device = Display.getCurrent();

		if (!updated) {
			updatedLabel.setText(Messages.SimonykeesUpdateLicenseDialog_invalid_license_key);
			updatedLabel.setForeground(device.getSystemColor(SWT.COLOR_DARK_RED));
			updatedLabel.setVisible(true);

			updatedIconLabel.setImage(scaledJSparrowImageInactive);
			updatedIconLabel.setVisible(true);

		} else {
			updatedLabel.setText(Messages.SimonykeesUpdateLicenseDialog_license_updated_successfully);
			updatedLabel.setForeground(device.getSystemColor(SWT.COLOR_GREEN));
			updatedLabel.setVisible(true);

			updatedIconLabel.setImage(scaledJSparrowImageActive);
			updatedIconLabel.setVisible(true);
		}

		updatedIconLabel.getParent().layout();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
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
