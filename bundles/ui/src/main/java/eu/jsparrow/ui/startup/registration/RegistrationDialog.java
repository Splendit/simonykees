package eu.jsparrow.ui.startup.registration;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import eu.jsparrow.i18n.Messages;

/**
 * A dialog containing a {@link TabFolder} with {@link TabItem}s for customer
 * registration and activation.
 * 
 * @since 3.0.0
 *
 */
public class RegistrationDialog extends Dialog {

	RegistrationControl registrationTabControl;
	ActivationControl activationTabControl;
	private Runnable lambdaAfterActivation;

	public RegistrationDialog(Shell parent, Runnable lambdaAfterActivation) {
		super(parent);
		this.lambdaAfterActivation = lambdaAfterActivation;
	}

	public RegistrationDialog(Shell parent) {
		super(parent);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		// Create the containing tab folder
		final TabFolder tabFolder = new TabFolder(parent, SWT.NONE);

		// Create each tab and set its text, tool tip text,
		// image, and control
		TabItem registerTab = new TabItem(tabFolder, SWT.NONE);
		registerTab.setText(Messages.RegistrationDialog_registerTabTitle);
		registerTab.setToolTipText(Messages.RegistrationDialog_registerTabTooltip);
		registrationTabControl = new RegistrationControl(tabFolder, SWT.NONE);
		registerTab.setControl(registrationTabControl);

		TabItem activateTab = new TabItem(tabFolder, SWT.NONE);
		activateTab.setText(Messages.RegistrationDialog_activateTabTitle);
		activateTab.setToolTipText(Messages.RegistrationDialog_activateTabTooltip);
		activationTabControl = new ActivationControl(tabFolder, SWT.NONE, lambdaAfterActivation);
		activateTab.setControl(activationTabControl);

		// Select the first tab (index is zero-based)
		tabFolder.setSelection(0);

		// Add an event listener to write the selected tab to stdout
		tabFolder.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				if (tabFolder.getSelection()[0].equals(registerTab)) {
					registrationTabControl.resetToDefaultSelection();
				}
				if (tabFolder.getSelection()[0].equals(activateTab)) {
					activationTabControl.resetToDefaultSelection();
				}
			}
		});

		return tabFolder;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		// we don't want those buttons
		return null;
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.RegistrationDialog_registrationTitle);
	}

}
