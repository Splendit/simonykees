
package eu.jsparrow.ui.wizard.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;

/**
 * Wizard page for selecting rules when applying rules to selected resources
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
public class SelectRulesWizardPage extends AbstractSelectRulesWizardPage {

	private Composite filterComposite;

	private static final String EMPTY_PROFILE = Messages.SelectRulesWizardPage_EmptyProfileLabel;
	private static final String CUSTOM_PROFILE = Messages.SelectRulesWizardPage_CustomProfileLabel;

	private Label selectProfileLabel;
	private Combo selectProfileCombo;

	private Label nameFilterLabel;
	private Text nameFilterText;

	private Composite tagsComposite;

	private Button removeDisabledRulesButton;

	private boolean update = true;

	public SelectRulesWizardPage(SelectRulesWizardPageModel model, SelectRulesWizardPageControler controler) {
		super(model, controler);
		setTitle(Messages.SelectRulesWizardPage_title);
		setDescription(Messages.SelectRulesWizardPage_description);
	}

	/**
	 * Creates filtering part of the wizard view which contains label and combo for
	 * filtering by group, label and text field for filtering by group and check box
	 * button to show or hide disabled rules
	 * 
	 * @param parent
	 */
	@Override
	protected void createFilteringPart(Composite parent) {
		filterComposite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		filterComposite.setLayoutData(gridData);
		GridLayout gridLayout = new GridLayout(4, false);
		gridLayout.horizontalSpacing = 3;
		filterComposite.setLayout(gridLayout);

		nameFilterLabel = new Label(filterComposite, SWT.NONE);
		nameFilterLabel.setText(Messages.SelectRulesWizardPage_filterByName);

		nameFilterText = new Text(filterComposite, SWT.SEARCH | SWT.CANCEL | SWT.ICON_SEARCH);

		// content for autocomplete proposal window with specified size
		SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(
				((SelectRulesWizardPageModel) model).getTags());
		ContentProposalAdapter proposalAdapter = new ContentProposalAdapter(nameFilterText, new TextContentAdapter(),
				proposalProvider, null, null);
		proposalProvider.setFiltering(true);
		proposalAdapter.setPropagateKeys(true);
		proposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		proposalAdapter.setPopupSize(new Point(100, 80));

		nameFilterText.setMessage(Messages.SelectRulesWizardPage_searchString);
		gridData = new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1);
		gridData.widthHint = 180;
		nameFilterText.setLayoutData(gridData);
		nameFilterText.addModifyListener((ModifyEvent e) -> {
			Text source = (Text) e.getSource();
			((SelectRulesWizardPageControler) controler)
					.nameFilterTextChanged(StringUtils.lowerCase(source.getText().trim()));
		});
		// following doesn't work under Windows7
		nameFilterText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.detail == SWT.CANCEL) {
					Text text = (Text) e.getSource();
					text.setText(Messages.SelectRulesWizardPage_emptyString);
				} else if (e.detail == SWT.ICON_SEARCH) {
					Text text = (Text) e.getSource();
					String input = StringUtils.lowerCase(text.getText().trim());
					if (!StringUtils.isEmpty(input)
							&& !((SelectRulesWizardPageModel) model).getAppliedTags().contains(input)) {
						((SelectRulesWizardPageControler) controler).searchPressed(input);
						addTagInComposite(input);
						nameFilterText.setText(""); //$NON-NLS-1$
					}
				}
			}
		});

		// when enter is pressed behave same as if search was pressed
		nameFilterText.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					String input = StringUtils.lowerCase(((Text) e.getSource()).getText().trim());
					if (!StringUtils.isEmpty(input)
							&& !((SelectRulesWizardPageModel) model).getAppliedTags().contains(input)) {
						((SelectRulesWizardPageControler) controler).searchPressed(input);
						addTagInComposite(input);
						nameFilterText.setText(""); //$NON-NLS-1$
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// nothing
			}
		});

		selectProfileLabel = new Label(filterComposite, SWT.NONE);
		selectProfileLabel.setText(Messages.SelectRulesWizardPage_selectProfile);
		gridData = new GridData(GridData.END, GridData.CENTER, true, false);
		selectProfileLabel.setLayoutData(gridData);

		selectProfileCombo = new Combo(filterComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		populateGroupFilterCombo();
		selectProfileCombo.addSelectionListener(createSelectProfileSelectionListener());
		gridData = new GridData(GridData.END, GridData.FILL, false, false);
		gridData.widthHint = 200;
		selectProfileCombo.setLayoutData(gridData);
		initializeGroupFilterCombo();

		tagsComposite = new Composite(filterComposite, SWT.NONE);
		RowLayout tagsLayout = new RowLayout();
		tagsLayout.marginBottom = 0;
		tagsLayout.marginTop = 0;
		tagsComposite.setLayout(tagsLayout);
		gridData = new GridData(GridData.FILL, GridData.BEGINNING, false, false);
		gridData.horizontalSpan = 4;
		tagsComposite.setLayoutData(gridData);

		createRemoveDisabledRulesButton(filterComposite);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		removeDisabledRulesButton.setLayoutData(gridData);

		Dialog.applyDialogFont(parent);
	}

	/**
	 * Set all items for the dropdown ({@link Combo}) and select All as default
	 * group
	 */
	private void populateGroupFilterCombo() {
		List<String> profiles = SimonykeesPreferenceManager.getAllProfileIds();
		selectProfileCombo.add(EMPTY_PROFILE);
		profiles.forEach(selectProfileCombo::add);
	}

	/**
	 * Initializes profile combo to None if No profile option is selected in
	 * preferences or to currently selected profile otherwise
	 */
	private void initializeGroupFilterCombo() {
		if (SimonykeesPreferenceManager.useProfile()) {
			selectProfileCombo.select(selectProfileCombo.indexOf(SimonykeesPreferenceManager.getCurrentProfileId()));
			((SelectRulesWizardPageControler) controler)
					.profileChanged(SimonykeesPreferenceManager.getCurrentProfileId());
		} else {
			selectProfileCombo.select(selectProfileCombo.indexOf(EMPTY_PROFILE));
		}
	}

	/**
	 * {@link SelectionListener} for the profile dropdown ({@link Combo}).
	 * 
	 * @return {@link SelectionListener} that reacts to changes of the selected
	 *         element.
	 */
	private SelectionListener createSelectProfileSelectionListener() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectedProfileId = selectProfileCombo.getItem(selectProfileCombo.getSelectionIndex());
				if (!selectedProfileId.equals(CUSTOM_PROFILE)) {
					selectProfileCombo.remove(CUSTOM_PROFILE);
					if (update) {
						nameFilterText.setText(""); //$NON-NLS-1$
						((SelectRulesWizardPageModel) model).getAppliedTags().clear();
						removeAllTagButtons();
						((SelectRulesWizardPageControler) controler).profileChanged(selectedProfileId);
					} else {
						update = true;
					}
				}
			}
		};
	}

	/**
	 * Adds a button to select / deselect all rules.
	 * 
	 * @param parent
	 */
	private void createRemoveDisabledRulesButton(Composite parent) {
		removeDisabledRulesButton = new Button(parent, SWT.CHECK);
		removeDisabledRulesButton.setText(Messages.SelectRulesWizardPage_removeDisabledRulesButtonText);
		removeDisabledRulesButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// if button is selected, disabled rules shouldn't be shown in
				// left table view
				Button btn = (Button) e.getSource();
				model.removeDisabledPosibilities(btn.getSelection());
			}
		});
	}

	private void addTagInComposite(String tag) {
		Button tagButton = new Button(tagsComposite, SWT.PUSH | SWT.ICON_CANCEL);
		tagButton.setText(tag);
		tagButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				((SelectRulesWizardPageControler) controler).tagButtonPressed(((Button) e.getSource()).getText());
				tagButton.dispose();
				tagsComposite.layout(true, true);
				filterComposite.layout(true, true);
				recalculateLayout();
			}
		});
		tagsComposite.layout(true, true);
		filterComposite.layout(true, true);
		recalculateLayout();
	}

	private void removeAllTagButtons() {
		for (Control child : tagsComposite.getChildren()) {
			child.dispose();
		}
		tagsComposite.layout(true, true);
		filterComposite.layout(true, true);
		recalculateLayout();
	}

	@Override
	protected void doStatusUpdate() {
		super.doStatusUpdate(null);
	}

	@Override
	protected void updateData() {
		super.updateData();

		this.addChangeListeners();
	}

	private void selectCustomProfile() {
		if (!Arrays.asList(selectProfileCombo.getItems()).contains(CUSTOM_PROFILE)) {
			selectProfileCombo.add(CUSTOM_PROFILE);
		}
		selectProfileCombo.select(selectProfileCombo.indexOf(CUSTOM_PROFILE));
	}

	private void addChangeListeners() {
		super.getAddAllButton().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				selectCustomProfile();
			}

		});

		super.getRemoveAllButton().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				selectCustomProfile();
			}

		});

		super.getAddButton().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				selectCustomProfile();
			}

		});

		super.getRemoveButton().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				selectCustomProfile();
			}

		});

		super.getLeftTreeViewer().addDoubleClickListener(doubleClickEvent -> selectCustomProfile());

		super.getRightTableViewer().addDoubleClickListener(doubleClickEvent -> selectCustomProfile());
	}
}
