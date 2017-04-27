
package at.splendit.simonykees.core.ui.wizard.impl;

import java.util.List;

import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import at.splendit.simonykees.core.ui.preference.SimonykeesPreferenceManager;
import at.splendit.simonykees.i18n.Messages;

public class SelectRulesWizardPage extends AbstractSelectRulesWizardPage {

	private SelectRulesWizardPageModel model;
	private SelectRulesWizardPageControler controler;

	private Label groupFilterLabel;
	private Combo selectProfileCombo;

	private Label nameFilterLabel;
	private Text nameFilterText;

	private Button removeDisabledRulesButton;

	public SelectRulesWizardPage(SelectRulesWizardPageModel model, SelectRulesWizardPageControler controler) {
		super(model, controler);
		setTitle(Messages.SelectRulesWizardPage_title);
		setDescription(Messages.SelectRulesWizardPage_description);

		this.model = model;
		this.controler = controler;
	}

	/**
	 * Creates filtering part of the wizard view which contains label and combo
	 * for filtering by group, label and text field for filtering by group and
	 * check box button to show or hide disabled rules
	 * 
	 * @param parent
	 */
	protected void createFilteringPart(Composite parent) {
		Composite filterComposite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		filterComposite.setLayoutData(gridData);
		GridLayout gridLayout = new GridLayout(4, false);
		filterComposite.setLayout(gridLayout);

		nameFilterLabel = new Label(filterComposite, SWT.NONE);
		nameFilterLabel.setText(Messages.SelectRulesWizardPage_filterByName);

		nameFilterText = new Text(filterComposite, SWT.SEARCH | SWT.CANCEL | SWT.ICON_SEARCH);
		new AutoCompleteField(nameFilterText, new TextContentAdapter(), model.getTags());
		nameFilterText.setMessage(Messages.SelectRulesWizardPage_searchString);
		gridData = new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1);
		gridData.widthHint = 180;
		nameFilterText.setLayoutData(gridData);
		nameFilterText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				Text source = (Text) e.getSource();
				controler.nameFilterTextChanged(source.getText());
			}
		});
		// following doesn't work under Windows7
		nameFilterText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.detail == SWT.CANCEL) {
					Text text = (Text) e.getSource();
					text.setText(Messages.SelectRulesWizardPage_emptyString);
				}
			}
		});


		groupFilterLabel = new Label(filterComposite, SWT.NONE);
		groupFilterLabel.setText(Messages.SelectRulesWizardPage_selectProfile);
		gridData = new GridData(GridData.END, GridData.CENTER, true, false);
		groupFilterLabel.setLayoutData(gridData);

		selectProfileCombo = new Combo(filterComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		populateGroupFilterCombo();
		selectProfileCombo.addSelectionListener(createSelectProfileSelectionListener());
		gridData = new GridData(GridData.END, GridData.FILL, false, false);
		gridData.widthHint = 200;
		selectProfileCombo.setLayoutData(gridData);

		createRemoveDisabledRulesButton(filterComposite);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		removeDisabledRulesButton.setLayoutData(gridData);
	}

	/**
	 * Set all items for the dropdown ({@link Combo}) and select All as default
	 * group
	 */
	private void populateGroupFilterCombo() {
//		Map<String, String> profiles = SimonykeesPreferenceManager.getAllProfileNamesAndIdsMap();
//		for(String key : profiles.keySet()) {
//			selectProfileCombo.add(key);
//		}
		List<String> profiles = SimonykeesPreferenceManager.getAllProfileIds();
		for(String profile : profiles) {
			selectProfileCombo.add(profile);
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
				if (selectedProfileId.equals(model.getCurrentProfileId())) {
					// nothing
				} else {
					nameFilterText.setText(""); //$NON-NLS-1$
					controler.profileChanged(selectedProfileId);
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
}
