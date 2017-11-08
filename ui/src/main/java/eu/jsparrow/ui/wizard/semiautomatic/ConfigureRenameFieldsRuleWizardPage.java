package eu.jsparrow.ui.wizard.semiautomatic;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;

/**
 * {@link NewElementWizardPage} containing view for configuration of options for
 * renaming rule.
 * 
 * @author Andreja Sambolec
 * @since 2.3.0
 *
 */
@SuppressWarnings("restriction")
public class ConfigureRenameFieldsRuleWizardPage extends NewElementWizardPage {

	private ConfigureRenameFieldsRuleWizardPageModel model;
	private ConfigureRenameFieldsRuleWizardPageController controler;

	private Font boldFont;

	protected IStatus fSelectionStatus;

	public ConfigureRenameFieldsRuleWizardPage(ConfigureRenameFieldsRuleWizardPageModel model) {
		super(Messages.RenameFieldsRuleWizardPage_title);
		setTitle(Messages.RenameFieldsRuleWizardPage_title);
		setDescription(Messages.RenameFieldsRuleWizardPage_description);

		this.model = model;
		this.controler = new ConfigureRenameFieldsRuleWizardPageController(model);
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		setControl(composite);

		FontDescriptor boldDescriptor = FontDescriptor.createFrom(parent.getFont())
			.setStyle(SWT.BOLD);
		boldFont = boldDescriptor.createFont(composite.getDisplay());

		createFieldTypeChoosingPart(composite);
		createSearchScopeChoosingPart(composite);
		createConfigureReplacementsPart(composite);
		createTodoChoosingPart(composite);

		model.addListener(this::updateView);
	}

	/**
	 * Creates view part for choosing field type options to be searched for
	 * renaming.
	 * 
	 * @param parent
	 *            holding component
	 */
	private void createFieldTypeChoosingPart(Composite parent) {
		Label partTitle = new Label(parent, SWT.NONE);
		partTitle.setText(Messages.RenameFieldsRuleWizardPage_fieldTypeLabelText);
		partTitle.setFont(boldFont);

		Table table = new Table(parent, SWT.CHECK);
		table.setLayout(new GridLayout());
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		table.setLayoutData(gridData);
		for (String fieldType : model.getFieldTypeOptions()) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(fieldType);
			item.setChecked(true);
		}

		table.addListener(SWT.Selection, event -> {
			if (event.detail == SWT.CHECK) {
				controler.fieldTypeSelectionChanged(Arrays.asList(table.getItems())
					.stream()
					.filter(TableItem::getChecked)
					.map(TableItem::getText)
					.collect(Collectors.toList()));
			}
		});
	}

	/**
	 * Creates view part for choosing search scope option to be searched for
	 * renaming.
	 * 
	 * @param parent
	 *            holding component
	 */
	private void createSearchScopeChoosingPart(Composite parent) {
		Label partTitle = new Label(parent, SWT.NONE);
		partTitle.setText(Messages.RenameFieldsRuleWizardPage_searchScopeLabelText);
		partTitle.setFont(boldFont);

		Group scopesGroup = new Group(parent, SWT.NONE);
		scopesGroup.setLayout(new GridLayout());
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		scopesGroup.setLayoutData(gridData);

		for (String scopeType : model.getSearchScopeOptions()) {
			Button button = new Button(scopesGroup, SWT.RADIO);
			button.setText(scopeType);
			button.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					controler.searchScopeSelectionChanged(((Button) e.getSource()).getText());
				}
			});
		}
		((Button) scopesGroup.getChildren()[0]).setSelection(true);
		controler.searchScopeSelectionChanged(((Button) scopesGroup.getChildren()[0]).getText());
	}

	/**
	 * Creates view part for choosing replacement options.
	 * 
	 * @param parent
	 *            holding component
	 */
	private void createConfigureReplacementsPart(Composite parent) {
		Label partTitle = new Label(parent, SWT.NONE);
		partTitle.setText(Messages.RenameFieldsRuleWizardPage_replacemenentsLabelText);
		partTitle.setFont(boldFont);

		Group underscoreGroup = new Group(parent, SWT.NONE);
		underscoreGroup.setLayout(new GridLayout());
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		underscoreGroup.setLayoutData(gridData);
		underscoreGroup.setText(Messages.RenameFieldsRuleWizardPage_underscoreReplacementLabelText);
		for (String underscoreReplacement : model.getUnderscoreReplacementOptions()) {
			Button button = new Button(underscoreGroup, SWT.RADIO);
			button.setText(underscoreReplacement);
			button.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					controler.underscoreReplacementSelectionChanged(((Button) e.getSource()).getText());
				}
			});
		}
		((Button) underscoreGroup.getChildren()[0]).setSelection(true);
		controler.underscoreReplacementSelectionChanged(((Button) underscoreGroup.getChildren()[0]).getText());

		Group dollarSingGroup = new Group(parent, SWT.NONE);
		dollarSingGroup.setLayout(new GridLayout());
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		dollarSingGroup.setLayoutData(gridData);
		dollarSingGroup.setText(Messages.RenameFieldsRuleWizardPage_dollarSignReplacementLabelText);
		for (String dollarReplacement : model.getDollarSignReplacementOptions()) {
			Button button = new Button(dollarSingGroup, SWT.RADIO);
			button.setText(dollarReplacement);
			button.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					controler.dollarReplacementSelectionChanged(((Button) e.getSource()).getText());
				}
			});
		}
		((Button) dollarSingGroup.getChildren()[0]).setSelection(true);
		controler.dollarReplacementSelectionChanged(((Button) dollarSingGroup.getChildren()[0]).getText());
	}

	/**
	 * Creates view part for choosing if to-do comments should be added where
	 * renaming is not possible.
	 * 
	 * @param parent
	 *            holding component
	 */
	private void createTodoChoosingPart(Composite parent) {
		Label partTitle = new Label(parent, SWT.NONE);
		partTitle.setText(Messages.RenameFieldsRuleWizardPage_todoCommentsLabelText);
		partTitle.setFont(boldFont);

		Table table = new Table(parent, SWT.CHECK);
		table.setLayout(new GridLayout());
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		table.setLayoutData(gridData);
		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(model.getTodoOption());
		item.setChecked(true);

		table.addListener(SWT.Selection, event -> {
			if (event.detail == SWT.CHECK) {
				controler.todoSelectionChanged(table.getItem(0)
					.getChecked());
			}
		});
	}

	/**
	 * Updates title status with status info every time something is changed in
	 * model. If status has any message, warning will be shown, otherwise title
	 * will be shown.
	 */
	private void updateView() {
		if (model.getFieldTypes()
			.isEmpty()) {
			((StatusInfo) fSelectionStatus).setError(Messages.RenameFieldsRuleWizardPage_warning_noFieldSelected);
		} else {
			fSelectionStatus = new StatusInfo();
		}

		// status of all used components
		IStatus[] status;
		status = new IStatus[] { fSelectionStatus };

		/*
		 * the mode severe status will be displayed and the OK button
		 * enabled/disabled.
		 */
		updateStatus(status);
	}

	/**
	 * Open help dialog
	 */
	@Override
	public void performHelp() {
		SimonykeesMessageDialog.openDefaultHelpMessageDialog(getShell());
	}
}
