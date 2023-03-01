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

/**
 * Dialog that shows when user has free license and is not registered for free
 * rules
 * 
 * @since 3.0.0
 *
 */
public class SuggestRegistrationDialog extends Dialog {

	public static final int BUTTON_ID_ENTER_PREMIUM_LICENSE_KEY = 11002;

	public static final String UNLOCK_SELECTED_RULES = "Unlock selected rules";

	public static final String YOUR_SELECTION_IS_INCLUDING_PREMIUM_RULES = "Your selection is including premium rules.";

	private static final String ENTER_YOUR_LICENSE_KEY = "Enter your license key";

	private Composite area;
	private final String lastButtonLabel;

	public SuggestRegistrationDialog(Shell parentShell, String lastButtonLabel) {
		super(parentShell);
		this.lastButtonLabel = lastButtonLabel;
	}

	@Override
	protected Control createDialogArea(Composite composite) {
		area = (Composite) super.createDialogArea(composite);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 10;
		area.setLayout(gridLayout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		area.setLayoutData(gridData);

		addLabel(YOUR_SELECTION_IS_INCLUDING_PREMIUM_RULES);
		addLinkToJSparrowPricingPage(JSparrowPricingLink.TO_UNLOCK_PREMIUM_RULES_UPGRADE_LICENSE);
		addRegisterForPremiumButton();

		Control[] children = area.getChildren();
		for (Control child : children) {
			child.setFont(composite.getFont());
		}
		return composite;
	}

	private void addLabel(String lableText) {
		Label label = new Label(area, SWT.NONE);
		label.setText(lableText);
	}

	private void addLinkToJSparrowPricingPage(JSparrowPricingLink jSparrowPricingLink) {
		Link linkToUnlockRules = new Link(area, SWT.NONE);
		linkToUnlockRules
			.setText(jSparrowPricingLink.getText());
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

	private void addRegisterForPremiumButton() {
		Button registerForPremiumButton = new Button(area, SWT.PUSH);
		registerForPremiumButton.setText(ENTER_YOUR_LICENSE_KEY);
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
		shell.setText(UNLOCK_SELECTED_RULES);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (IDialogConstants.SKIP_LABEL.equals(lastButtonLabel)) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.SKIP_LABEL, false);
		} else if (IDialogConstants.CANCEL_LABEL.equals(lastButtonLabel)) {
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		}
	}

	private void registerForPremiumButtonPressed() {
		this.setReturnCode(BUTTON_ID_ENTER_PREMIUM_LICENSE_KEY);
		this.close();
	}
}
