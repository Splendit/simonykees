package eu.jsparrow.ui.startup.registration;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import eu.jsparrow.i18n.Messages;

public class RegistrationDialog extends Dialog {

	RegistrationControl registrationTabControl;
	ActivationControl activationTabControl;

	public RegistrationDialog(Shell parent) {
		super(parent);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout titleLayout = new GridLayout(1, false);
		container.setLayout(titleLayout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		container.setLayoutData(gridData);

		// Create the containing tab folder
		final TabFolder tabFolder = new TabFolder(container, SWT.FILL);
		GridLayout tabFolderLayout = new GridLayout(1, false);
		tabFolder.setLayout(tabFolderLayout);
		GridData tabFolderData = new GridData(SWT.FILL, SWT.FILL, true, true);
		tabFolder.setLayoutData(tabFolderData);

		// Create each tab and set its text, tool tip text,
		// image, and control
		TabItem registerTab = new TabItem(tabFolder, SWT.FILL);
		registerTab.setText(Messages.RegistrationDialog_registerTabTitle);
		registerTab.setToolTipText(Messages.RegistrationDialog_registerTabTooltip);
		registerTab.setControl(getRegistrationControl(tabFolder));

		TabItem activateTab = new TabItem(tabFolder, SWT.FILL);
		activateTab.setText(Messages.RegistrationDialog_activateTabTitle);
		activateTab.setToolTipText(Messages.RegistrationDialog_activateTabTooltip);
		activateTab.setControl(getActivationControl(tabFolder));

		// Select the third tab (index is zero-based)
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

		return container;
	}

	/**
	 * Gets the control for tab one
	 * 
	 * @param tabFolder
	 *            the parent tab folder
	 * @return Control
	 */
	private Control getRegistrationControl(TabFolder tabFolder) {
		registrationTabControl = new RegistrationControl(tabFolder, SWT.NONE);
		return registrationTabControl.getControl();
	}

	/**
	 * Gets the control for tab two
	 * 
	 * @param tabFolder
	 *            the parent tab folder
	 * @return Control
	 */
	private Control getActivationControl(TabFolder tabFolder) {
		activationTabControl = new ActivationControl(tabFolder, SWT.NONE);
		return activationTabControl.getControl();
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

	@Override
	protected Point getInitialSize() {
		return new Point(450, 600);
	}
}
