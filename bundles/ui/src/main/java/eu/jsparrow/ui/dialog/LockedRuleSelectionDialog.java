package eu.jsparrow.ui.dialog;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

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

/**
 * Dialog that shows when user has free license and is not registered for free
 * rules
 * 
 * @since 3.0.0
 *
 */
public class LockedRuleSelectionDialog extends Dialog {

	public static final int BUTTON_ID_REGISTER_FOR_A_FREE_TRIAL = 11001;
	public static final int BUTTON_ID_ENTER_PREMIUM_LICENSE_KEY = 11002;

	static final String FORMAT_LINK_TO_JSPARROW_PRICING = "%s<a href=\"https://jsparrow.io/pricing/\">%s</a>%s";

	public static final String UPGRADE_YOUR_LICENSE = "Upgrade your license";
	public static final String _TO_BE_ABLE_TO_APPLY_ALL_OUR_RULES = " to be able to apply all our rules!";
	public static final String REGISTER_FOR_A_FREE_J_SPARROW_TRIAL = "Register for a free jSparrow trial";
	public static final String _TO_BE_ABLE_TO_APPLY_20_OF_OUR_MOST_LIKED_RULES = " to be able to apply 20 of our most liked rules!";

	public static final String UNLOCK_SELECTED_RULES = "Unlock selected rules";

	public static final String FULLSTOP = ".";

	public static final String TO_UNLOCK_ALL_OUR_RULES = "To unlock all our rules, ";
	public static final String TO_UNLOCK_PREMIUM_RULES = "To unlock premium rules ";
	public static final String TO_UNLOCK_THEM = "To unlock them, ";

	public static final String ALL_RULES_IN_YOUR_SELECTION_ARE_FREE = "All rules in your selection are free.";
	public static final String YOUR_SELECTION_IS_INCLUDING_FREE_RULES = "Your selection is including free rules.";
	public static final String YOUR_SELECTION_IS_INCLUDING_ONLY_PREMIUM_RULES = "Your selection is including only premium rules.";
	public static final String YOUR_SELECTION_IS_INCLUDING_PREMIUM_RULES = "Your selection is including premium rules.";

	public static final String REGISTER_FOR_A_FREE_TRIAL_VERSION = "register for a free trial version.";
	public static final String REGISTER_FOR_A_PREMIUM_LICENSE = "register for a premium license.";
	public static final String REGISTRATION_FOR_A_FREE_TRIAL_WILL_UNLOCK_20_OF_OUR_MOST_LIKED_RULES = "Registration for a free trial will unlock 20 of our most liked rules!";

	private static final String REGISTER_FOR_A_FREE_TRIAL = "Register for a free trial";
	private static final String ENTER_YOUR_LICENSE_KEY = "Enter your license key";

	public static final String AND_UPGRADE_YOUR_LICENSE = " and upgrade your license.";

	public static final String VISIT_US = "visit us";

	private final List<Consumer<LockedRuleSelectionDialog>> addComponentLambdas;

	private Composite area;
	private boolean cancelAsLastButton;
	private boolean skipAsLastButton;
	private String textForShell;

	public LockedRuleSelectionDialog(Shell parentShell, List<Consumer<LockedRuleSelectionDialog>> addComponentLambdas) {
		super(parentShell);
		this.addComponentLambdas = addComponentLambdas;
	}

	@Override
	protected Control createDialogArea(Composite composite) {
		area = (Composite) super.createDialogArea(composite);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 10;
		area.setLayout(gridLayout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		area.setLayoutData(gridData);

		addComponentLambdas.forEach(lambda -> lambda.accept(this));

		Control[] children = area.getChildren();
		for (Control child : children) {
			child.setFont(composite.getFont());
		}
		return composite;
	}

	public void useCancelAsLastButton() {
		cancelAsLastButton = true;
		skipAsLastButton = false;
	}

	public void useSkipAsLastButton() {
		skipAsLastButton = true;
		cancelAsLastButton = false;
	}

	public void setTextForShell(String text) {
		textForShell = text;
	}

	public void addLabel(String lableText) {
		Label label = new Label(area, SWT.NONE);
		label.setText(lableText);
	}

	public void addLinkToUnlockAllRules(String textBeforeLink, String linkedText) {
		addLinkToUnlockAllRules(textBeforeLink, linkedText, ""); //$NON-NLS-1$
	}

	public void addLinkToUnlockAllRules(String textBeforeLink, String linkedText, String textAfterLink) {
		Link linkToUnlockRules = new Link(area, SWT.NONE);
		linkToUnlockRules
			.setText(String.format(FORMAT_LINK_TO_JSPARROW_PRICING, textBeforeLink, linkedText, textAfterLink));
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

	public void addRegisterForFreeButton() {
		Button registerForFreeButton = new Button(area, SWT.PUSH);
		registerForFreeButton.setText(REGISTER_FOR_A_FREE_TRIAL);
		registerForFreeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				registerForFreeButtonPressed();
			}
		});
	}

	public void addRegisterForPremiumButton() {
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
		if (textForShell != null) {
			shell.setText(textForShell);
		}

	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (skipAsLastButton) {
			createButton(parent, IDialogConstants.OK_ID, Messages.SuggestRegistrationDialog_skipButtonText, false);
		} else if (cancelAsLastButton) {
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		}
	}

	private void registerForFreeButtonPressed() {
		this.setReturnCode(BUTTON_ID_REGISTER_FOR_A_FREE_TRIAL);
		this.close();
	}

	private void registerForPremiumButtonPressed() {
		this.setReturnCode(BUTTON_ID_ENTER_PREMIUM_LICENSE_KEY);
		this.close();
	}
}
