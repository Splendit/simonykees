package at.splendit.simonykees.core.ui.preference;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.license.LicenseManager;

/**
 * Dialog for updating license key.
 *  
 * @author Ardit Ymeri
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
		
		Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(3, false);
        container.setLayout(layout);

		Label licenseKeyLabel = new Label(container, SWT.NONE);
		licenseKeyLabel.setText(Messages.SimonykeesUpdateLicenseDialog_update_license_dialog_label);
		
		GridData licenseKeyGridData = new GridData();
		licenseKeyGridData.grabExcessHorizontalSpace = true;
		licenseKeyGridData.horizontalAlignment = GridData.FILL;

		licenseKeyText = new Text(container, SWT.BORDER);
		licenseKeyText.setLayoutData(licenseKeyGridData);
		licenseKeyText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				Text textWidget = (Text) event.getSource();
				licenseKey = textWidget.getText();
			}
		});
		
		updateButton = new Button(container, SWT.PUSH);
		updateButton.setText(Messages.SimonykeesUpdateLicenseDialog_update_llicense_dialog_button);
		updateButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String licenseKey = getLicenseKey();
				LicenseManager licenseManager = LicenseManager.getInstance();
				boolean updated = licenseManager.updateLicenseeNumber(licenseKey.trim(), DEFAULT_LICENSEE_NAME);
				updateWarningInformation(updated);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		updatedIconLabel = new Label(container, SWT.NONE);
		updatedIconLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		updatedIconLabel.setVisible(true);
		
		updatedLabel = new Label(container, SWT.NONE);
		updatedLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		updatedLabel.setVisible(true);
		
		return container;
	}
	
	private void updateWarningInformation(boolean updated) {
		Display display = getShell().getDisplay();
		Device device = Display.getCurrent ();
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(updatedLabel.getFont()).setStyle(SWT.BOLD);
		Font boldFont = boldDescriptor.createFont(display);
		updatedLabel.setFont(boldFont);
		
		if(!updated) {
			Color red = new Color (device, 255, 0, 0);
			updatedLabel.setText(Messages.SimonykeesUpdateLicenseDialog_invalid_license_key);
			updatedLabel.setForeground(red);
			Image errorIcon = display.getSystemImage(SWT.ICON_ERROR);
			updatedIconLabel.setImage(errorIcon);
			
		} else {
			Color green = new Color (device, 1, 66, 37);
			updatedLabel.setText(Messages.SimonykeesUpdateLicenseDialog_license_updated_successfully);
			updatedLabel.setForeground(green);
			Image successIcon = display.getSystemImage(SWT.ICON_WORKING);
			updatedIconLabel.setImage(successIcon);
			
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
		this. licenseKey = licenseKeyText.getText();
		super.okPressed();
	}

	public String getLicenseKey() {
		return licenseKey;
	}
}
