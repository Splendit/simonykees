package at.splendit.simonykees.core.ui.preference;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.i18n.Messages;

public class SimonykeesPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private RadioGroupFieldEditor useProfileOptionRadioGroup;
	// private String currentProfileId;
	private int currentProfileSelection = 0;

	private Table profilesTable;
	private List<Button> buttons = new ArrayList<>();

	private Button newProfileButton;
	private Button editProfileButton;
	private Button removeProfileButton;

	public SimonykeesPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		SimonykeesPreferenceManager.loadCurrentProfiles();
	}

	@Override
	protected void createFieldEditors() {
		Composite composite = new Composite(getFieldEditorParent(), SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout gridLayout = new GridLayout(2, false);
		composite.setLayout(gridLayout);

		String[][] useProfileOption = new String[][] { { Messages.SimonykeesPreferencePage_useProfileOptionNoProfile, SimonykeesPreferenceConstants.PROFILE_USE_OPTION_NO_PROFILE },
				{ Messages.SimonykeesPreferencePage_useProfileOptionSelectedProfile, SimonykeesPreferenceConstants.PROFILE_USE_OPTION_SELECTED_PROFILE } };

		useProfileOptionRadioGroup = new RadioGroupFieldEditor(SimonykeesPreferenceConstants.PROFILE_USE_OPTION,
				Messages.SimonykeesPreferencePage_useProfileOptionRadioGroupTitle, 1, useProfileOption, composite);
		addField(useProfileOptionRadioGroup);

		createProfilesTableView(composite);

		initializeView();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getSource() instanceof RadioGroupFieldEditor && ((RadioGroupFieldEditor) event.getSource())
				.getPreferenceName().equals(SimonykeesPreferenceConstants.PROFILE_USE_OPTION)) {
			if (event.getNewValue().toString().equals(SimonykeesPreferenceConstants.PROFILE_USE_OPTION_NO_PROFILE)) {
				profilesTable.deselectAll();
				profilesTable.setEnabled(false);
				newProfileButton.setEnabled(false);
				editProfileButton.setEnabled(false);
				removeProfileButton.setEnabled(false);
			} else {
				profilesTable.setEnabled(true);
				newProfileButton.setEnabled(true);
				editProfileButton.setEnabled(false);
				removeProfileButton.setEnabled(false);
			}
		}
	}

	private void createProfilesTableView(Composite composite) {
		Composite viewerComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		viewerComposite.setLayout(layout);

		GridData data = new GridData(GridData.FILL_BOTH);
		viewerComposite.setLayoutData(data);

		profilesTable = new Table(viewerComposite, SWT.BORDER | SWT.MULTI);
		data = new GridData(GridData.FILL_BOTH);
		profilesTable.setLayoutData(data);
		TableColumn column = new TableColumn(profilesTable, SWT.NONE);
		column.setWidth(100);
		populateTable();

		profilesTable.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				buttons.get(currentProfileSelection).setSelection(false);
				currentProfileSelection = ((Table) e.getSource()).getSelectionIndex();
				buttons.get(currentProfileSelection).setSelection(true);
				handleSelectionChanged(currentProfileSelection);
			}
		});

		createButtonsPart(viewerComposite);
	}

	private void populateTable() {
		SimonykeesPreferenceManager.loadCurrentProfiles();
		for (int i = 0; i < SimonykeesPreferenceManager.getAllProfileIds().size(); i++) {
			new TableItem(profilesTable, SWT.NONE);
		}
		TableItem[] items = profilesTable.getItems();
		for (int i = 0; i < items.length; i++) {
			TableEditor editor = new TableEditor(profilesTable);
			TableItem item = items[i];
			Button button = new Button(profilesTable, SWT.RADIO);
			button.setText(SimonykeesPreferenceManager.getAllProfileIds().get(i));
			button.pack();
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					super.widgetSelected(e);
					profilesTable.setSelection(item);
					currentProfileSelection = buttons.indexOf(button);
					handleSelectionChanged(currentProfileSelection);
				}
			});
			buttons.add(button);
			editor.minimumWidth = button.getSize().x;
			editor.horizontalAlignment = SWT.LEFT;
			editor.setEditor(button, item, 0);
		}
	}

	private void handleSelectionChanged(int selectionIndex) {
		editProfileButton.setEnabled(true);
		if (SimonykeesPreferenceManager
				.getProfileFromName(SimonykeesPreferenceManager.getAllProfileIds().get(selectionIndex))
				.isBuiltInProfile()) {
			removeProfileButton.setEnabled(false);
		} else {
			removeProfileButton.setEnabled(true);
		}
	}

	private void createButtonsPart(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.CENTER));

		newProfileButton = new Button(composite, SWT.PUSH);
		newProfileButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		newProfileButton.setText(Messages.SimonykeesPreferencePage_newProfileButtonLabel);

		editProfileButton = new Button(composite, SWT.PUSH);
		editProfileButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		editProfileButton.setText(Messages.SimonykeesPreferencePage_editProfileButtonLabel);

		removeProfileButton = new Button(composite, SWT.PUSH);
		removeProfileButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		removeProfileButton.setText(Messages.SimonykeesPreferencePage_removeProfileButtonLabel);

		newProfileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleButtonClickedListener(""); //$NON-NLS-1$
				updateView();
			}
		});

		editProfileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleButtonClickedListener(
						SimonykeesPreferenceManager.getAllProfileIds().get(currentProfileSelection));
				updateView();
			}
		});

		removeProfileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SimonykeesPreferenceManager
						.removeProfile(SimonykeesPreferenceManager.getAllProfileIds().get(currentProfileSelection));
				currentProfileSelection = 0;
				updateView();
			}
		});
	}

	public void handleButtonClickedListener(String profileId) {
		final WizardDialog dialog = new WizardDialog(getShell(), new ConfigureProfileWizard(profileId)) {
			// Removed unnecessary empty space on the bottom of
			// the wizard intended for ProgressMonitor that is
			// not used
			@Override
			protected Control createDialogArea(Composite parent) {
				Control ctrl = super.createDialogArea(parent);
				getProgressMonitor();
				return ctrl;
			}

			@Override
			protected IProgressMonitor getProgressMonitor() {
				ProgressMonitorPart monitor = (ProgressMonitorPart) super.getProgressMonitor();
				GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.heightHint = 0;
				monitor.setLayoutData(gridData);
				monitor.setVisible(false);
				return monitor;
			}
		};
		/*
		 * the dialog is made as smaller than necessary horizontally (we want
		 * line breaks for rule descriptions)
		 */
		dialog.setPageSize(750, 500);

		dialog.open();

	}

	private void initializeView() {
		// profilesTableVIewer.setInput(SimonykeesPreferenceManager.getAllProfileNamesAndIdsArray());
		Activator.getDefault().getPreferenceStore().setDefault(SimonykeesPreferenceConstants.PROFILE_USE_OPTION, SimonykeesPreferenceConstants.PROFILE_USE_OPTION_NO_PROFILE);
		currentProfileSelection = SimonykeesPreferenceManager.getProfiles()
				.indexOf(SimonykeesPreferenceManager.getProfileFromName(SimonykeesPreferenceManager.getCurrentProfileId()));// loadCurrentProfileId()));
		buttons.get(currentProfileSelection).setSelection(true);
		if (Activator.getDefault().getPreferenceStore().getString(SimonykeesPreferenceConstants.PROFILE_USE_OPTION)
				.equals(SimonykeesPreferenceConstants.PROFILE_USE_OPTION_NO_PROFILE)) {
			profilesTable.setEnabled(false);
		} else {
			profilesTable.setEnabled(true);
		}

		newProfileButton.setEnabled(true);
		editProfileButton.setEnabled(false);
		removeProfileButton.setEnabled(false);
	}
	
	private void updateView() {		
		profilesTable.removeAll();
		profilesTable.getItemCount();
		for(Button button : buttons) {
			button.dispose();
		}
		buttons.clear();
		populateTable();
		buttons.get(currentProfileSelection).setSelection(true);
	}
	

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	public boolean performOk() {
		SimonykeesPreferenceManager
				.setCurrentProfileId(SimonykeesPreferenceManager.getAllProfileIds().get(currentProfileSelection));
		SimonykeesPreferenceManager.loadCurrentProfiles();
		return super.performOk();
	}
}
