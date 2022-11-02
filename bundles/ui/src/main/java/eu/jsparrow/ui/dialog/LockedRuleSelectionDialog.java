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
import eu.jsparrow.ui.startup.registration.RegistrationDialog;

/**
 * Dialog that shows when user has free license and is not registered for free
 * rules
 * 
 * @since 3.0.0
 *
 */
public class LockedRuleSelectionDialog extends Dialog {

	private static final String UNLOCK_SELECTED_RULES = "Unlock selected rules";

	private static final String FORMAT_LINK_TO_JSPARROW_PRICING = "%<a href=\"https://jsparrow.io/pricing/\">%</a>%";

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

	public static final String AND_UPGRADE_YOUR_LICENSE = " and upgrade your license.";

	public static final String VISIT_US = "visit us";

	private final List<Consumer<LockedRuleSelectionDialog>> addComponentLambdas;
	private Composite area;
	private Button registerForFreeButton;
	private Link linkToUnlockRules;

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

		if (registerForFreeButton != null) {
			registerForFreeButton.setFont(composite.getFont());
			registerForFreeButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					registerButtonPressed();
				}
			});
		}

		if (linkToUnlockRules != null) {
			linkToUnlockRules.setFont(composite.getFont());
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

		return composite;
	}

	public void addLabel(String lableText) {
		Label titleLabel = new Label(area, SWT.NONE);
		titleLabel.setText(lableText);
	}
	
	public void addLinkToUnlockAllRules(String textBeforeLink, String linkedText) {
		addLinkToUnlockAllRules(textBeforeLink, linkedText, ""); //$NON-NLS-1$
	}

	public void addLinkToUnlockAllRules(String textBeforeLink, String linkedText, String textAfterLink) {
		linkToUnlockRules = new Link(area, SWT.NONE);
		linkToUnlockRules
			.setText(String.format(FORMAT_LINK_TO_JSPARROW_PRICING, textBeforeLink, linkedText, textAfterLink));
	}

	public void addRegisterForFreeButton() {
		registerForFreeButton = new Button(area, SWT.PUSH);
		registerForFreeButton.setText(Messages.SimonykeesPreferencePageLicense_register_for_free_jsparrow_trial);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(UNLOCK_SELECTED_RULES);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	private void registerButtonPressed() {
		// PlatformUI.getWorkbench()
		// .getDisplay()
		// .asyncExec(() -> {
		// Shell activeShell = PlatformUI.getWorkbench()
		// .getActiveWorkbenchWindow()
		// .getShell();
		// new RegistrationDialog(activeShell).open();
		// });
		new RegistrationDialog(getShell()).open();
		this.close();
	}

}
