package eu.jsparrow.ui.preference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLConfigException;
import eu.jsparrow.core.config.YAMLConfigUtil;
import eu.jsparrow.core.config.YAMLProfile;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.preference.profile.SimonykeesProfile;

public class SimonykeesPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final Logger logger = LoggerFactory.getLogger(SimonykeesPreferencePage.class);

	private RadioGroupFieldEditor useProfileOptionRadioGroup;
	private int currentProfileSelection = 0;

	private Table profilesTable;
	private List<Button> buttons = new ArrayList<>();

	private Button newProfileButton;
	private Button editProfileButton;
	private Button removeProfileButton;
	private Button importProfileButton;
	private Button exportProfileButton;

	private Font font;

	public SimonykeesPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		SimonykeesPreferenceManager.loadCurrentProfiles();
	}

	@Override
	protected void createFieldEditors() {
		font = getFieldEditorParent().getFont();
		Composite composite = new Composite(getFieldEditorParent(), SWT.NONE);
		composite.setFont(font);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout gridLayout = new GridLayout(2, false);
		composite.setLayout(gridLayout);

		String[][] useProfileOption = new String[][] {
				{ Messages.SimonykeesPreferencePage_useProfileOptionNoProfile,
						SimonykeesPreferenceConstants.PROFILE_USE_OPTION_NO_PROFILE },
				{ Messages.SimonykeesPreferencePage_useProfileOptionSelectedProfile,
						SimonykeesPreferenceConstants.PROFILE_USE_OPTION_SELECTED_PROFILE } };

		useProfileOptionRadioGroup = new RadioGroupFieldEditor(SimonykeesPreferenceConstants.PROFILE_USE_OPTION,
				Messages.SimonykeesPreferencePage_useProfileOptionRadioGroupTitle, 1, useProfileOption, composite);
		addField(useProfileOptionRadioGroup);

		createProfilesTableView(composite);

		addField(new BooleanFieldEditor(SimonykeesPreferenceConstants.ENABLE_INTRO,
				Messages.SimonykeesPreferencePage_enableIntroText, composite));

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
				importProfileButton.setEnabled(false);
				exportProfileButton.setEnabled(false);
			} else {
				profilesTable.setEnabled(true);
				newProfileButton.setEnabled(true);
				editProfileButton.setEnabled(false);
				removeProfileButton.setEnabled(false);
				importProfileButton.setEnabled(true);
				exportProfileButton.setEnabled(true);
			}
		}
	}

	private void createProfilesTableView(Composite composite) {
		Composite viewerComposite = new Composite(composite, SWT.NONE);
		viewerComposite.setFont(font);
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
		profilesTable.setFont(font);
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
		for (int i = 0; i < SimonykeesPreferenceManager.getAllProfileIds().size(); i++) {
			new TableItem(profilesTable, SWT.NONE);
		}
		TableItem[] items = profilesTable.getItems();
		for (int i = 0; i < items.length; i++) {
			TableEditor editor = new TableEditor(profilesTable);
			TableItem item = items[i];
			Button button = new Button(profilesTable, SWT.RADIO);
			button.setText(SimonykeesPreferenceManager.getAllProfileIds().get(i));
			button.setFont(font);
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
		composite.setFont(font);

		newProfileButton = new Button(composite, SWT.PUSH);
		newProfileButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		newProfileButton.setText(Messages.SimonykeesPreferencePage_newProfileButtonLabel);
		newProfileButton.setFont(font);

		editProfileButton = new Button(composite, SWT.PUSH);
		editProfileButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		editProfileButton.setText(Messages.SimonykeesPreferencePage_editProfileButtonLabel);
		editProfileButton.setFont(font);

		removeProfileButton = new Button(composite, SWT.PUSH);
		removeProfileButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		removeProfileButton.setText(Messages.SimonykeesPreferencePage_removeProfileButtonLabel);
		removeProfileButton.setFont(font);

		importProfileButton = new Button(composite, SWT.PUSH);
		importProfileButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		importProfileButton.setText(Messages.SimonykeesPreferencePage_ImportProfilesButton);
		importProfileButton.setFont(font);

		exportProfileButton = new Button(composite, SWT.PUSH);
		exportProfileButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		exportProfileButton.setText(Messages.SimonykeesPreferencePage_ExportProfilesButton);
		exportProfileButton.setFont(font);

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
				removeProfileButton.setEnabled(false);
				updateView();
			}
		});

		importProfileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleProfileImport();
				updateView();
			}
		});

		exportProfileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleProfileExport();
			}
		});
	}

	public void handleButtonClickedListener(String profileId) {
		final WizardDialog dialog = new WizardDialog(getShell(), new ConfigureProfileWizard(profileId)) {
			/*
			 * Removed unnecessary empty space on the bottom of the wizard intended for
			 * ProgressMonitor that is not used
			 */
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
		 * the dialog is made as big enough to show rule description vertically and
		 * horizontally to avoid two scrollers
		 */
		dialog.setPageSize(800, 700);

		dialog.open();

	}

	/**
	 * View initialization called on first creation of the view.
	 */
	private void initializeView() {
		Activator.getDefault().getPreferenceStore().setDefault(SimonykeesPreferenceConstants.PROFILE_USE_OPTION,
				SimonykeesPreferenceConstants.PROFILE_USE_OPTION_NO_PROFILE);
		currentProfileSelection = SimonykeesPreferenceManager.getProfiles().indexOf(
				SimonykeesPreferenceManager.getProfileFromName(SimonykeesPreferenceManager.getCurrentProfileId()));// loadCurrentProfileId()))
		buttons.get(currentProfileSelection).setSelection(true);
		if (Activator.getDefault().getPreferenceStore().getString(SimonykeesPreferenceConstants.PROFILE_USE_OPTION)
				.equals(SimonykeesPreferenceConstants.PROFILE_USE_OPTION_NO_PROFILE)) {
			profilesTable.setEnabled(false);
			newProfileButton.setEnabled(false);
		} else {
			profilesTable.setEnabled(true);
			newProfileButton.setEnabled(true);
		}
		editProfileButton.setEnabled(false);
		removeProfileButton.setEnabled(false);
	}

	/**
	 * Updates view every time something changes in profiles list.
	 */
	private void updateView() {
		profilesTable.removeAll();
		buttons.forEach(Button::dispose);
		buttons.clear();
		populateTable();
		buttons.get(currentProfileSelection).setSelection(true);
	}

	@Override
	public void init(IWorkbench workbench) {
		//
	}

	@Override
	public boolean performOk() {
		SimonykeesPreferenceManager
				.setCurrentProfileId(SimonykeesPreferenceManager.getAllProfileIds().get(currentProfileSelection));
		SimonykeesPreferenceManager.loadCurrentProfiles();
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();

		SimonykeesPreferenceManager.performDefaults();
		SimonykeesPreferenceManager.loadCurrentProfiles();
		updateView();

		profilesTable.setEnabled(false);
		newProfileButton.setEnabled(false);
		editProfileButton.setEnabled(false);
		removeProfileButton.setEnabled(false);
	}

	/**
	 * If cancel is pressed, no changes from current manipulation should get stored.
	 */
	@Override
	public boolean performCancel() {
		SimonykeesPreferenceManager.resetProfilesList();
		return super.performCancel();
	}

	/**
	 * opens a file dialog to select a config file path. depending on the style
	 * parameter a save dialog or an open dialog will be displayed.
	 * 
	 * @param style
	 *            must be either {@link SWT#OPEN} or {@link SWT#SAVE}
	 * @return the selected config file path or null if the style parameter is not
	 *         one of the above or if the user cancelled the file dialog
	 */
	private String chooseConfigFile(int style) {
		if (style != SWT.SAVE && style != SWT.OPEN) {
			return null;
		}

		FileDialog fileDialog = new FileDialog(getShell(), style);
		fileDialog.setFilterExtensions(new String[] { "*.yml", "*.yaml", "*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		fileDialog.setText(Messages.SimonykeesPreferencePage_ChooseConfigFileDialogTitle);
		fileDialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());

		return fileDialog.open();
	}

	/**
	 * imports profiles from a config file
	 */
	private void handleProfileImport() {
		String path = chooseConfigFile(SWT.OPEN);

		if (path == null) {
			// user cancelled file dialog
			return;
		}

		File file = new File(path);

		if (!file.exists()) {
			logger.error(Messages.SimonykeesPreferencePage_SelectedFileDoesNotExist);
			SimonykeesMessageDialog.openMessageDialog(getShell(),
					Messages.SimonykeesPreferencePage_SelectedFileDoesNotExist, MessageDialog.ERROR); // $NON-NLS-1$
			return;
		}

		if (file.isDirectory()) {
			logger.error(Messages.SimonykeesPreferencePage_SelectedPathIsDirectory);
			SimonykeesMessageDialog.openMessageDialog(getShell(),
					Messages.SimonykeesPreferencePage_SelectedPathIsDirectory, MessageDialog.ERROR); // $NON-NLS-1$
			return;
		}

		try {
			YAMLConfig config = YAMLConfigUtil.loadConfiguration(file);
			List<String> currentProfileNames = SimonykeesPreferenceManager.getAllProfileIds();

			config.getProfiles().forEach(profile -> {
				boolean doImport = true;

				// prevent the default profile from being replaced
				if (Messages.Profile_DefaultProfile_profileName.equals(profile.getName())) {
					logger.error(Messages.SimonykeesPreferencePage_DefaultProfileNotReplacable);
					SimonykeesMessageDialog.openMessageDialog(getShell(),
							Messages.SimonykeesPreferencePage_DefaultProfileNotReplacable, MessageDialog.ERROR);
					return;
				}

				// check if the profile already exists
				if (currentProfileNames.contains(profile.getName())) {
					String message = NLS.bind(Messages.SimonykeesPreferencePage_ProfileExistsReplace,
							profile.getName());
					doImport = SimonykeesMessageDialog.openConfirmDialog(getShell(), message);
				}

				if (doImport) {
					SimonykeesPreferenceManager.removeProfile(profile.getName());
					SimonykeesPreferenceManager.addProfile(profile.getName(), profile.getRules());
					logger.info("profile added: " + profile); //$NON-NLS-1$
				} else {
					logger.info("profile NOT added: " + profile); //$NON-NLS-1$
				}
			});

			SimonykeesMessageDialog.openMessageDialog(getShell(),
					Messages.SimonykeesPreferencePage_ProfileImportSuccessful, MessageDialog.INFORMATION);
		} catch (YAMLConfigException e) {
			logger.error(e.getMessage(), e);
			SimonykeesMessageDialog.openMessageDialog(getShell(), e.getMessage(), MessageDialog.ERROR);
			return;
		}
	}

	/**
	 * exports the selected profiles to a config file
	 */
	private void handleProfileExport() {
		int[] indices = profilesTable.getSelectionIndices();
		if (indices.length > 0) {

			String path = chooseConfigFile(SWT.SAVE);

			if (path == null) {
				// user cancelled file dialog
				return;
			}

			File file = new File(path);
			if (file.exists()) {
				logger.error(Messages.SimonykeesPreferencePage_FileAlreadyExists);
				SimonykeesMessageDialog.openMessageDialog(getShell(),
						Messages.SimonykeesPreferencePage_FileAlreadyExists, MessageDialog.ERROR);
				return;
			}

			if (file.isDirectory()) {
				logger.error(Messages.SimonykeesPreferencePage_SelectedPathIsDirectory);
				SimonykeesMessageDialog.openMessageDialog(getShell(),
						Messages.SimonykeesPreferencePage_SelectedPathIsDirectory, MessageDialog.ERROR);
				return;
			}

			YAMLConfig config = new YAMLConfig();

			for (int i : indices) {
				String id = SimonykeesPreferenceManager.getAllProfileIds().get(i);
				SimonykeesProfile profile = SimonykeesPreferenceManager.getProfileFromName(id);

				YAMLProfile yamlProfile = new YAMLProfile();
				yamlProfile.setName(id);
				yamlProfile.setRules(profile.getEnabledRuleIds());

				config.getProfiles().add(yamlProfile);
			}
			try {
				YAMLConfigUtil.exportConfig(config, file);
				String message = NLS.bind(Messages.SimonykeesPreferencePage_ProfileExportSuccessfulTo, path);
				SimonykeesMessageDialog.openMessageDialog(getShell(), message, MessageDialog.INFORMATION);
			} catch (YAMLConfigException e) {
				logger.error(e.getMessage(), e);
				SimonykeesMessageDialog.openMessageDialog(getShell(), e.getMessage(), MessageDialog.ERROR);
			}
		} else {
			SimonykeesMessageDialog.openMessageDialog(getShell(), Messages.SimonykeesPreferencePage_NoProfilesSelected,
					MessageDialog.ERROR);
		}
	}
}
