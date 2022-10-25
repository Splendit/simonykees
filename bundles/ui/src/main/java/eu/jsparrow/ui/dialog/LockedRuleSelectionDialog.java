package eu.jsparrow.ui.dialog;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.ui.startup.registration.RegistrationDialog;
import eu.jsparrow.ui.util.LicenseUtil;

/**
 * Dialog that shows when user has free license and is not registered for free
 * rules
 * 
 * @since 3.0.0
 *
 */
public class LockedRuleSelectionDialog extends Dialog {

	private LicenseUtil licenseUtil = LicenseUtil.get();
	private final boolean activeRegistration;

	public LockedRuleSelectionDialog(Shell parentShell, boolean activeRegistration,
			List<RefactoringRule> selectedRules) {
		super(parentShell);
		this.activeRegistration = activeRegistration;
	}

	@Override
	protected Control createDialogArea(Composite composite) {
		Composite area = (Composite) super.createDialogArea(composite);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 10;
		area.setLayout(gridLayout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		area.setLayoutData(gridData);

		if (!activeRegistration) {
			Label titleLabel = new Label(area, SWT.NONE);
			titleLabel.setText("Unlock some of the selected rules by registration for a free trial.");

			Label descriptionLabel = new Label(area, SWT.NONE);
			descriptionLabel.setText("Registration for a free trial will unlock 20 of our most liked rules!");

			Button registerForFreeButton = new Button(area, SWT.PUSH);
			registerForFreeButton.setText(Messages.SimonykeesPreferencePageLicense_register_for_free_jsparrow_trial);
			registerForFreeButton.setFont(composite.getFont());
			registerForFreeButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					registerButtonPressed();
				}
			});
		}

		Link jSparrowLink = new Link(area, SWT.NONE);
		jSparrowLink.setFont(composite.getFont());
		jSparrowLink.setText(
				"To unlock all the rules <a href=\"https://jsparrow.io/pricing/\">visit us</a> and upgrade your license.");
		jSparrowLink.addSelectionListener(new SelectionAdapter() {
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

		return composite;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Unlock selected rules");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	private void registerButtonPressed() {
		licenseUtil.setShouldContinueWithSelectRules(false);
		PlatformUI.getWorkbench()
			.getDisplay()
			.asyncExec(() -> {
				Shell activeShell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				new RegistrationDialog(activeShell).open();
			});
		this.close();
	}
}
