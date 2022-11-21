package eu.jsparrow.ui.dialog;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.preference.SimonykeesUpdateLicenseDialog;
import eu.jsparrow.ui.startup.registration.RegistrationDialog;

/**
 * Dialog that shows when user has free license and is not registered for free
 * rules
 * 
 * @since 3.0.0
 *
 */
public class SuggestRegistrationDialog extends Dialog {

	private Composite area;

	public SuggestRegistrationDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite composite) {
		area = (Composite) super.createDialogArea(composite);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 10;
		area.setLayout(gridLayout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		area.setLayoutData(gridData);

		Label descriptionLabel = new Label(area, SWT.NONE);
		descriptionLabel.setText(Messages.SuggestRegistrationDialog_descriptionOfFreemiumLicense);
		addRegisterForFreeButton();
		addLinkToUnlockAllRules("", "Upgrade your license", " to be able to apply all our rules!");
		addRegisterForPremiumButton();
		return composite;
	}

	public void addRegisterForFreeButton() {
		Button registerForFreeButton = new Button(area, SWT.PUSH);
		registerForFreeButton.setText("Register for a free trial");
		registerForFreeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				registerForFreeButtonPressed();
			}
		});
	}

	public void addLinkToUnlockAllRules(String textBeforeLink, String linkedText, String textAfterLink) {
		Link linkToUnlockRules = new Link(area, SWT.NONE);
		linkToUnlockRules
			.setText(String.format(LockedRuleSelectionDialog.FORMAT_LINK_TO_JSPARROW_PRICING, textBeforeLink,
					linkedText, textAfterLink));
		linkToUnlockRules.addSelectionListener(new SelectionAdapter() {
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
	}

	public void addRegisterForPremiumButton() {
		Button registerForPremiumButton = new Button(area, SWT.PUSH);
		registerForPremiumButton.setText("Enter your license key");
		registerForPremiumButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				registerForPremiumButtonPressed();
			}
		});
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.SuggestRegistrationDialog_getFreeRulesTitle);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, Messages.SuggestRegistrationDialog_skipButtonText, false);
	}

	private void registerForFreeButtonPressed() {
		new RegistrationDialog(getShell()).open();
		this.close();
	}

	private void registerForPremiumButtonPressed() {
		SimonykeesUpdateLicenseDialog dialog = new SimonykeesUpdateLicenseDialog(getShell());
		dialog.create();
		dialog.open();
		this.close();
	}
}
