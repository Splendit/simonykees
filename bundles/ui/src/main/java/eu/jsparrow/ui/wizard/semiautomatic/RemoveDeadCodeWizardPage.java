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

@SuppressWarnings("restriction")
public class RemoveDeadCodeWizardPage extends NewElementWizardPage {

	private RemoveDeadCodeWizardPageModel model;
	private RemoveDeadCodeWizardPageController controller;
	
	private Font boldFont;
	protected IStatus fSelectionStatus;
	
	public RemoveDeadCodeWizardPage(RemoveDeadCodeWizardPageModel model) {
		super("Remove Dead Code");
		setTitle("Remove Dead Code");
		setDescription("Remove Dead Code Configuration");
		this.model = model;
		this.controller = new RemoveDeadCodeWizardPageController(model);

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
		
		createClassMemberChoosingPart(composite);
		createSearchScopeChoosingPart(composite);
		createRemoveTestPart(composite);
		createRemoveInitializersWithSideEffectsSelectionChangedPart(composite);


		model.addListener(this::updateView);
	}
	
	private void createClassMemberChoosingPart(Composite parent) {
		Label partTitle = new Label(parent, SWT.NONE);
		partTitle.setText("Apply on:");
		partTitle.setFont(boldFont);

		Table table = new Table(parent, SWT.CHECK);
		table.setLayout(new GridLayout());
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		table.setLayoutData(gridData);
		for (String fieldType : model.getClassMemberTypes()) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(fieldType);
			item.setChecked(true);
		}

		table.addListener(SWT.Selection, event -> {
			if (event.detail == SWT.CHECK) {
				controller.classMemberSelectionChanged(Arrays.asList(table.getItems())
					.stream()
					.filter(TableItem::getChecked)
					.map(TableItem::getText)
					.collect(Collectors.toList()));
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
					controller.searchScopeSelectionChanged(((Button) e.getSource()).getText());
				}
			});
		}
		((Button) scopesGroup.getChildren()[0]).setSelection(true);
		controller.searchScopeSelectionChanged(((Button) scopesGroup.getChildren()[0]).getText());
	}
	
	private void createRemoveTestPart(Composite parent) {
		Label partTitle = new Label(parent, SWT.NONE);
		partTitle.setText("Remove tests for unused code");
		partTitle.setFont(boldFont);

		Group scopesGroup = new Group(parent, SWT.NONE);
		scopesGroup.setLayout(new GridLayout());
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		scopesGroup.setLayoutData(gridData);

		Button button = new Button(scopesGroup, SWT.CHECK);
		button.setText("Remove test cases having references of unused code.");
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				controller.removeTestCodeSelectionChaged(((Button) e.getSource()).getText());// FIXME; change to boolean
			}
		});

		button.setSelection(true);
		controller.removeTestCodeSelectionChaged(button.getText()); // FIXME; change to boolean
	}
	
	private void createRemoveInitializersWithSideEffectsSelectionChangedPart(Composite parent) {
		Label partTitle = new Label(parent, SWT.NONE);
		partTitle.setText("Remove initializers of unused fields");
		partTitle.setFont(boldFont);

		Group scopesGroup = new Group(parent, SWT.NONE);
		scopesGroup.setLayout(new GridLayout());
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		scopesGroup.setLayoutData(gridData);

		Button button = new Button(scopesGroup, SWT.CHECK);
		button.setText("Remove fields initialized with expressions with probable side effects");
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				controller.removeInitializersWithSideEffectsSelectionChanged(((Button) e.getSource()).getText());// FIXME; change to boolean
			}
		});

		button.setSelection(true);
		controller.removeInitializersWithSideEffectsSelectionChanged(button.getText()); // FIXME; change to boolean
	}

	private void updateView() {
		if (model.getClassMemberTypes()
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
	
	@Override
	public void performHelp() {
		SimonykeesMessageDialog.openDefaultHelpMessageDialog(getShell());
	}
}
