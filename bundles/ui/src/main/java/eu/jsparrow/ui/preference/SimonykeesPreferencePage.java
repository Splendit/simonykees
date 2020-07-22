package eu.jsparrow.ui.preference;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
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

	private Table profilesTable;

	private Button setDefaultProfileButton;
	private Button newProfileButton;
	private Button editProfileButton;
	private Button removeProfileButton;
	private Button importProfileButton;
	private Button exportProfileButton;

	private Font font;

	public SimonykeesPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault()
			.getPreferenceStore());
		SimonykeesPreferenceManager.loadCurrentProfiles();
	}

	@Override
	protected void createFieldEditors() {
		font = getFieldEditorParent().getFont();
		Composite composite = new Composite(getFieldEditorParent(), SWT.NONE);
		composite.setFont(font);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout gridLayout = new GridLayout(1, false);
		composite.setLayout(gridLayout);

		Group generalGroup = new Group(composite, SWT.NONE);
		generalGroup.setText(Messages.SimonykeesPreferencePage_generalSettingsGroupTitle);
		generalGroup.setFont(font);
		generalGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		generalGroup.setLayout(new GridLayout(1, false));

		addField(new BooleanFieldEditor(SimonykeesPreferenceConstants.RESOLVE_PACKAGES_RECURSIVELY,
				Messages.SimonykeesPreferencePage_resolvePackagesRecursivelyLabel, generalGroup));

		createProfilesTableView(composite);

		initializeButtons();

	}

	private void createProfilesTableView(Composite composite) {
		Group profileGroup = new Group(composite, SWT.NONE);
		profileGroup.setText(Messages.SimonykeesPreferencePage_profileSettingsGroupTitle);
		profileGroup.setFont(font);
		profileGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		profileGroup.setLayout(new GridLayout(1, false));

		Composite viewerComposite = new Composite(profileGroup, SWT.NONE);
		viewerComposite.setFont(font);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		viewerComposite.setLayout(layout);

		GridData data = new GridData(GridData.FILL_BOTH);
		viewerComposite.setLayoutData(data);

		profilesTable = new Table(viewerComposite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		data = new GridData(GridData.FILL_BOTH);
		profilesTable.setLayoutData(data);
		profilesTable.setFont(font);

		TableColumn checkMarkColumn = new TableColumn(profilesTable, SWT.LEFT);
		checkMarkColumn.setWidth(20);
		TableColumn column = new TableColumn(profilesTable, SWT.NONE);
		column.setWidth(100);

		populateTable();

		profilesTable.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				handleSelectionChanged();
			}
		});

		createButtonsPart(viewerComposite);
	}

	private void populateTable() {
		for (int i = 0; i < SimonykeesPreferenceManager.getAllProfileIds()
			.size(); i++) {
			String currentSelectedProfile = SimonykeesPreferenceManager.getCurrentProfileId();
			String currentProfileId = SimonykeesPreferenceManager.getAllProfileIds()
				.get(i);
			Optional<SimonykeesProfile> currentProfile = SimonykeesPreferenceManager
				.getProfileFromName(currentProfileId);

			String itemText = currentProfileId + (currentProfile.isPresent() && currentProfile.get()
				.isBuiltInProfile() ? Messages.SimonykeesPreferencePage_profilesBuiltInSuffix : ""); //$NON-NLS-1$
			TableItem item = new TableItem(profilesTable, SWT.NONE);

			item.setText(1, itemText);

			if (currentSelectedProfile.equals(currentProfileId)) {
				FontData[] fontData = font.getFontData();
				Arrays.stream(fontData)
					.forEach(fd -> fd.setStyle(fd.getStyle() | SWT.BOLD));
				Font boldFont = new Font(font.getDevice(), fontData);

				item.setText(0, Character.toString((char) 0x2713)); // Unicode
																	// 0x2713 =
																	// '✓'
				item.setFont(boldFont);
			} else {
				item.setFont(font);
			}
		}
	}

	private void handleSelectionChanged() {

		int selectionCount = profilesTable.getSelectionCount();
		if (selectionCount == 1) {
			Optional<SimonykeesProfile> optionalProfile = SimonykeesPreferenceManager
				.getProfileFromName(SimonykeesPreferenceManager.getAllProfileIds()
					.get(profilesTable.getSelectionIndex()));
			String currentProfileId = SimonykeesPreferenceManager.getCurrentProfileId();

			optionalProfile.ifPresent(profile -> {
				if (currentProfileId.equals(profile.getProfileName())) {
					setDefaultProfileButton.setEnabled(false);
				} else {
					setDefaultProfileButton.setEnabled(true);
				}

				editProfileButton.setEnabled(true);
				exportProfileButton.setEnabled(true);

				if (profile.isBuiltInProfile()) {
					editProfileButton.setEnabled(false);
					removeProfileButton.setEnabled(false);
				} else {
					editProfileButton.setEnabled(true);
					removeProfileButton.setEnabled(true);
				}
			});
		} else if (selectionCount > 1) {
			setDefaultProfileButton.setEnabled(false);
			editProfileButton.setEnabled(false);
			removeProfileButton.setEnabled(true);
			exportProfileButton.setEnabled(true);
		} else {
			initializeButtons();
		}
	}

	private void createButtonsPart(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.CENTER));
		composite.setFont(font);

		setDefaultProfileButton = new Button(composite, SWT.PUSH);
		setDefaultProfileButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		setDefaultProfileButton.setText(Messages.SimonykeesPreferencePage_UseAsDefaultProfileButtonLabel);
		setDefaultProfileButton.setFont(font);

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

		setDefaultProfileButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (profilesTable.getSelectionCount() == 1) {
					String selectedProfileId = SimonykeesPreferenceManager.getAllProfileIds()
						.get(profilesTable.getSelectionIndex());
					setDefaultProfileButton.setEnabled(false);
					SimonykeesPreferenceManager.setCurrentProfileId(selectedProfileId);
					updateView();
					setSelection(selectedProfileId);
				}
			}

		});

		newProfileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleButtonClickedListener(""); //$NON-NLS-1$
				updateView();
				initializeButtons();
			}
		});

		editProfileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (profilesTable.getSelectionCount() == 1) {
					String selectedProfileId = SimonykeesPreferenceManager.getAllProfileIds()
						.get(profilesTable.getSelectionIndex());
					handleButtonClickedListener(selectedProfileId);
					updateView();
					setSelection(selectedProfileId);
				}
			}
		});

		removeProfileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelectedProfiles();
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

	private void removeSelectedProfiles() {
		List<String> profilesToDelete = new LinkedList<>();
		for (int index : profilesTable.getSelectionIndices()) {
			Optional<SimonykeesProfile> optionalProfile = SimonykeesPreferenceManager
				.getProfileFromName(SimonykeesPreferenceManager.getAllProfileIds()
					.get(index));

			optionalProfile.filter(profile -> !profile.isBuiltInProfile())
				.ifPresent(profile -> {
					if (profile.getProfileName()
						.equals(SimonykeesPreferenceManager.getCurrentProfileId())) {
						SimonykeesPreferenceManager
							.setCurrentProfileId(SimonykeesPreferenceManager.getDefaultProfileName());
					}
					profilesToDelete.add(profile.getProfileName());
				});
		}

		profilesToDelete.forEach(SimonykeesPreferenceManager::removeProfile);

		initializeButtons();
		updateView();
	}

	public void handleButtonClickedListener(String profileId) {
		String currentDefaultProfileId = SimonykeesPreferenceManager.getCurrentProfileId();
		final WizardDialog dialog = new WizardDialog(getShell(),
				new ConfigureProfileWizard(profileId, profileId.equals(currentDefaultProfileId))) {
			/*
			 * Removed unnecessary empty space on the bottom of the wizard
			 * intended for ProgressMonitor that is not used
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
		 * the dialog is made as big enough to show rule description vertically
		 * and horizontally to avoid two scrollers
		 */
		dialog.setPageSize(800, 700);

		dialog.open();
	}

	/**
	 * View initialisation called on first creation of the view.
	 */
	private void initializeButtons() {
		profilesTable.setEnabled(true);
		setDefaultProfileButton.setEnabled(false);
		newProfileButton.setEnabled(true);
		exportProfileButton.setEnabled(false);
		importProfileButton.setEnabled(true);
		editProfileButton.setEnabled(false);
		removeProfileButton.setEnabled(false);
	}

	private void setSelection(String selectedProfile) {
		profilesTable.setSelection(SimonykeesPreferenceManager.getAllProfileIds()
			.indexOf(selectedProfile));
	}

	/**
	 * Updates view every time something changes in profiles list.
	 */
	private void updateView() {
		profilesTable.removeAll();
		populateTable();
	}

	@Override
	public void init(IWorkbench workbench) {
		//
	}

	@Override
	public boolean performOk() {
		SimonykeesPreferenceManager.loadCurrentProfiles();
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();

		SimonykeesPreferenceManager.performDefaults();
		SimonykeesPreferenceManager.loadCurrentProfiles();
		updateView();

		initializeButtons();
	}

	/**
	 * If cancel is pressed, no changes from current manipulation should get
	 * stored.
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
	 * @return the selected config file path or null if the style parameter is
	 *         not one of the above or if the user cancelled the file dialog
	 */
	private String chooseConfigFile(int style) {
		if (style != SWT.SAVE && style != SWT.OPEN) {
			return null;
		}

		FileDialog fileDialog = new FileDialog(getShell(), style);
		fileDialog.setFilterExtensions(new String[] { "*.yml", "*.yaml", "*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		fileDialog.setText(Messages.SimonykeesPreferencePage_ChooseConfigFileDialogTitle);
		fileDialog.setFilterPath(ResourcesPlugin.getWorkspace()
			.getRoot()
			.getLocation()
			.toString());

		return fileDialog.open();
	}

	private boolean isValidFile(File file) {
		if (!file.exists()) {
			logger.error(Messages.SimonykeesPreferencePage_SelectedFileDoesNotExist);
			SimonykeesMessageDialog.openMessageDialog(getShell(),
					Messages.SimonykeesPreferencePage_SelectedFileDoesNotExist, MessageDialog.ERROR); // $NON-NLS-1$
			return false;
		}

		if (file.isDirectory()) {
			logger.error(Messages.SimonykeesPreferencePage_SelectedPathIsDirectory);
			SimonykeesMessageDialog.openMessageDialog(getShell(),
					Messages.SimonykeesPreferencePage_SelectedPathIsDirectory, MessageDialog.ERROR); // $NON-NLS-1$
			return false;
		}
		return true;
	}

	private boolean isValidProfileName(String profileName) {
		String customProfileLabel = Messages.SelectRulesWizardPage_CustomProfileLabel;
		if (customProfileLabel.equals(profileName)) {
			String message = NLS.bind(Messages.SimonykeesPreferencePage_reservedProfileNameError, customProfileLabel);
			logger.error(message);
			SimonykeesMessageDialog.openMessageDialog(getShell(), message, MessageDialog.ERROR);
			return false;
		}

		// prevent the default profile from being replaced
		boolean isBuiltIn = SimonykeesPreferenceManager.getProfileFromName(profileName)
			.filter(SimonykeesProfile::isBuiltInProfile)
			.map(builtIn -> true)
			.orElse(false);
		if (isBuiltIn) {
			String message = NLS.bind(Messages.SimonykeesPreferencePage_DefaultProfileNotReplacable, profileName);
			logger.error(message);
			SimonykeesMessageDialog.openMessageDialog(getShell(), message, MessageDialog.ERROR);
			return false;
		}

		return true;
	}
	
	private ProfileImportMode requestImportModeUpdate(String profileName) {
		String message = NLS.bind(Messages.SimonykeesPreferencePage_ProfileExistsReplace,
				profileName);
		String[] buttonLabels = new String[] { Messages.SimonykeesPreferencePage_Skip,
				Messages.SimonykeesPreferencePage_Replace, Messages.SimonykeesPreferencePage_KeepBoth };
		int doImport = SimonykeesMessageDialog.openQuestionWithCancelDialog(getShell(), message,
				buttonLabels);
		ProfileImportMode mode;
		switch (doImport) {
		case 0:
			mode = ProfileImportMode.SKIP;
			break;
		case 1:
			mode = ProfileImportMode.REPLACE;
			break;
		case 2:
			mode = ProfileImportMode.RENAME;
			break;
		default:
			mode = ProfileImportMode.SKIP;
		}
		return mode;
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
		if (!isValidFile(file)) {
			return;
		}

		try {
			YAMLConfig config = YAMLConfigUtil.loadConfiguration(file);
			int importedProfileCount = 0;

			for (YAMLProfile profile : config.getProfiles()) {
				List<String> currentProfileNames = SimonykeesPreferenceManager.getAllProfileIds();
				ProfileImportMode mode = ProfileImportMode.IMPORT;

				if (!isValidProfileName(profile.getName())) {
					return;
				}

				// check if the profile already exists
				if (currentProfileNames.contains(profile.getName())) {
					mode = requestImportModeUpdate(profile.getName());
				}

				if (mode != ProfileImportMode.SKIP) {
					if (mode == ProfileImportMode.REPLACE) {
						SimonykeesPreferenceManager.removeProfile(profile.getName());
					} else if (mode == ProfileImportMode.RENAME) {
						String newProfileName = addSuffixToProfileName(profile.getName());
						profile.setName(newProfileName);
					}
					validateExistingRules(profile);
					SimonykeesPreferenceManager.addProfile(profile.getName(), profile.getRules());
					importedProfileCount++;
					logger.info("profile added: {}", profile); //$NON-NLS-1$

				}
			}

			String finishMessage = (importedProfileCount == 0) ? Messages.SimonykeesPreferencePage_NoProfilesImported
					: NLS.bind(Messages.SimonykeesPreferencePage_ProfileImportSuccessful, importedProfileCount);

			SimonykeesMessageDialog.openMessageDialog(getShell(), finishMessage, MessageDialog.INFORMATION);

		} catch (YAMLConfigException e) {
			logger.error(e.getMessage(), e);
			SimonykeesMessageDialog.openMessageDialog(getShell(), e.getMessage(), MessageDialog.ERROR);
		}
	}

	private void validateExistingRules(YAMLProfile profile) {
		List<String> nonExistentRules = YAMLConfigUtil.getNonExistentRules(profile.getRules(), false);
		if (!nonExistentRules.isEmpty()) {
			String nonExistentRulesMessage = NLS.bind(Messages.SimonykeesPreferencePage_profileAndName,
					profile.getName()) + "\n" //$NON-NLS-1$
					+ NLS.bind(Messages.Activator_standalone_RulesDoNotExist, nonExistentRules.toString());
			SimonykeesMessageDialog.openMessageDialog(getShell(), nonExistentRulesMessage,
					MessageDialog.INFORMATION);
			profile.getRules()
				.removeAll(nonExistentRules);
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
				boolean replace = SimonykeesMessageDialog.openConfirmDialog(getShell(),
						Messages.SimonykeesPreferencePage_FileAlreadyExists);
				if (!replace) {
					return;
				}
			}

			if (file.isDirectory()) {
				logger.error(Messages.SimonykeesPreferencePage_SelectedPathIsDirectory);
				SimonykeesMessageDialog.openMessageDialog(getShell(),
						Messages.SimonykeesPreferencePage_SelectedPathIsDirectory, MessageDialog.ERROR);
				return;
			}

			YAMLConfig config = new YAMLConfig();

			for (int i : indices) {
				String id = SimonykeesPreferenceManager.getAllProfileIds()
					.get(i);
				Optional<SimonykeesProfile> optProfile = SimonykeesPreferenceManager.getProfileFromName(id);

				optProfile.ifPresent(profile -> {
					YAMLProfile yamlProfile = new YAMLProfile();
					yamlProfile.setName(id);
					yamlProfile.setRules(profile.getEnabledRuleIds());

					config.getProfiles()
						.add(yamlProfile);
				});
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

	/**
	 * adds an integer suffix to the given profile name
	 * 
	 * @param profileName
	 * @return if the given profile already exists, an integer will be appended
	 *         to it to make it unique, otherwise the given profile name will be
	 *         returned without change
	 */
	private String addSuffixToProfileName(String profileName) {
		int index = 1;
		String currentProfileName = profileName;

		LinkedList<String> profileNameParts = new LinkedList<>(Arrays.asList(profileName.split("_"))); //$NON-NLS-1$
		List<String> currentProfiles = SimonykeesPreferenceManager.getAllProfileIds();

		/*
		 * if an integer has already been appended to the given profile name, we
		 * take it and increase its value. This prevents profiles being named
		 * like "profile1_1_1_1_1" after multiple imports. Instead it will be
		 * imported as "profile1_4"
		 */
		if (profileNameParts.size() > 1) {
			try {
				index = Integer.parseInt(profileNameParts.getLast());
				profileNameParts.removeLast();
				currentProfileName = String.join("_", profileNameParts); //$NON-NLS-1$
			} catch (NumberFormatException nfe) {
				// if the last part isn't an integer do nothing
			}
		}

		String newProfileName = currentProfileName;

		while (currentProfiles.contains(newProfileName)) {

			StringBuilder sb = new StringBuilder();
			sb.append(currentProfileName);
			sb.append("_"); //$NON-NLS-1$
			sb.append(index);

			newProfileName = sb.toString();
			index++;
		}

		return newProfileName;
	}

	private enum ProfileImportMode {
		SKIP,
		RENAME,
		REPLACE,
		IMPORT,
	}
}
