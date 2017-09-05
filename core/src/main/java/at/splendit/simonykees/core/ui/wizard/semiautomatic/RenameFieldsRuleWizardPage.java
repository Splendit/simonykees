package at.splendit.simonykees.core.ui.wizard.semiautomatic;

import java.util.Arrays;
import java.util.stream.Collectors;

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

import at.splendit.simonykees.core.ui.wizard.IValueChangeListener;

public class RenameFieldsRuleWizardPage extends NewElementWizardPage {

	private RenameFieldsRuleWizardPageModel model;
	private RenameFieldsRuleWizardPageControler controler;

	private Composite composite;

	private Font boldFont;

	public RenameFieldsRuleWizardPage(RenameFieldsRuleWizardPageModel model,
			RenameFieldsRuleWizardPageControler controler) {
		super("Rename public fields rule");
		setTitle("Rename public fields rule");
		setDescription("Configure rename public fields rule");

		this.model = model;
		this.controler = controler;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		setControl(composite);

		FontDescriptor boldDescriptor = FontDescriptor.createFrom(parent.getFont()).setStyle(SWT.BOLD);
		boldFont = boldDescriptor.createFont(composite.getDisplay());

		createFieldTypeChoosingPart(composite);
		createSearchScopeChoosingPart(composite);
		createConfigureReplacementsPart(composite);
		createTodoChoosingPart(composite);

		model.addListener(new IValueChangeListener() {

			@Override
			public void valueChanged() {
				// TODO update page
			}
		});
	}

	private void createFieldTypeChoosingPart(Composite parent) {
		Label partTitle = new Label(parent, SWT.NONE);
		partTitle.setText("Apply on the following fields");
		partTitle.setFont(boldFont);

		Table table = new Table(parent, SWT.CHECK);
		table.setLayout(new GridLayout());
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		table.setLayoutData(gridData);
		for (String fieldType : model.getFieldTypeOptions()) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(fieldType);
		}

		table.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				controler.fieldTypeSelectionChanged(Arrays.asList(table.getSelection()).stream().map(item -> item.getText()).collect(Collectors.toList()));
			}
		});
	}

	private void createSearchScopeChoosingPart(Composite parent) {
		Label partTitle = new Label(parent, SWT.NONE);
		partTitle.setText("Search scope for references");
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
	}

	private void createConfigureReplacementsPart(Composite parent) {
		Label partTitle = new Label(parent, SWT.NONE);
		partTitle.setText("Configure replacements");
		partTitle.setFont(boldFont);

		Group underscoreGroup = new Group(parent, SWT.NONE);
		underscoreGroup.setLayout(new GridLayout());
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		underscoreGroup.setLayoutData(gridData);
		underscoreGroup.setText("How should the first character after an underscore be handled?");
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

		Group dollarSingGroup = new Group(parent, SWT.NONE);
		dollarSingGroup.setLayout(new GridLayout());
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		dollarSingGroup.setLayoutData(gridData);
		dollarSingGroup.setText("How should the first character after a $ (dollar) sign be handled?");
		for (String dollarReplacement : model.getDolarSignReplacementOptions()) {
			Button button = new Button(dollarSingGroup, SWT.RADIO);
			button.setText(dollarReplacement);
			button.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					controler.dollarReplacementSelectionChanged(((Button) e.getSource()).getText());
				}
			});
		}
	}

	private void createTodoChoosingPart(Composite parent) {
		Label partTitle = new Label(parent, SWT.NONE);
		partTitle.setText("Should TODO comments be added if renaming cannot be done?");
		partTitle.setFont(boldFont);

		Table table = new Table(parent, SWT.CHECK);
		table.setLayout(new GridLayout());
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		table.setLayoutData(gridData);
		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(model.getTodoOption());

		table.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				controler.todoSelectionChanged(table.getItem(0).getChecked());
			}
		});
	}
	
	private void updateView() {
		// TODO use this or set imediately default and rest doesn't have to be updated
	}
}
