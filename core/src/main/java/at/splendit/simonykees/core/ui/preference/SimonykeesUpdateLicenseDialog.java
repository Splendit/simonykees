package at.splendit.simonykees.core.ui.preference;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import at.splendit.simonykees.core.i18n.Messages;

/**
 * Dialog for updating license key.
 *  
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class SimonykeesUpdateLicenseDialog extends TitleAreaDialog {

	private Text licenseKeyText;
	private String licenseKey = ""; //$NON-NLS-1$

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
        GridLayout layout = new GridLayout(2, false);
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

		return container;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, Messages.SimonykeesUpdateLicenseDialog_update_llicense_dialog_button, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
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
