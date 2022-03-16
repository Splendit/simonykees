package eu.jsparrow.ui.wizard.semiautomatic;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
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
 * A wizard page for configuring the rules that remove unused code. 
 * 
 * @since 4.8.0
 *
 */
@SuppressWarnings("restriction")
public class RemoveUnusedCodeWizardPage extends NewElementWizardPage {

	private RemoveUnusedCodeWizardPageModel model;
	private RemoveUnusedCodeWizardPageController controller;
	
	private Font boldFont;
	protected IStatus fSelectionStatus;
	
	public RemoveUnusedCodeWizardPage(RemoveUnusedCodeWizardPageModel model) {
		super(Messages.RemoveUnusedCodeWizardPage_pageName);
		setTitle(Messages.RemoveUnusedCodeWizardPage_pageTitle);
		setDescription(Messages.RemoveUnusedCodeWizardPage_pageDescription);
		this.model = model;
		this.controller = new RemoveUnusedCodeWizardPageController(model);

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
		createSingleCheckBoxSection(composite, 
				Messages.RemoveUnusedCodeWizardPage_removeUnusedTestsSectionTitle,
				Messages.RemoveUnusedCodeWizardPage_removeUnusedTestsDescription,
				controller::removeTestCodeSelectionChanged, false);
		createSingleCheckBoxSection(composite, 
				Messages.RemoveUnusedCodeWizardPage_removeInitializersSectionTitle,
				Messages.RemoveUnusedCodeWizardPage_removeInitializersSectionDescription,
				controller::removeInitializersWithSideEffectsSelectionChanged, false);


		model.addListener(this::updateView);
	}
	
	@Override
	public void dispose() {
		boldFont.dispose();
		super.dispose();
	}
	
	private void createClassMemberChoosingPart(Composite parent) {
		Label partTitle = new Label(parent, SWT.NONE);
		partTitle.setText(Messages.RemoveUnusedCodeWizardPage_applyOn);
		partTitle.setFont(boldFont);

		Table table = new Table(parent, SWT.CHECK);
		table.setLayout(new GridLayout());
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		table.setLayoutData(gridData);
		List<String> defaultChecked = model.getDefaultClassMemberTypes();
		for (String fieldType : model.getAllClassMemberTypes()) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(fieldType);
			boolean checked = defaultChecked.contains(fieldType);
			item.setChecked(checked);
		}

		table.addListener(SWT.Selection, event -> {
			if (event.detail == SWT.CHECK) {
				List<String>checkedItems = Arrays.asList(table.getItems())
						.stream()
						.filter(TableItem::getChecked)
						.map(TableItem::getText)
						.collect(Collectors.toList());
				controller.classMemberSelectionChanged(checkedItems);
			}
		});
	}

	private void createSearchScopeChoosingPart(Composite parent) {
		Label partTitle = new Label(parent, SWT.NONE);
		partTitle.setText(Messages.RemoveUnusedCodeWizardPage_searchScopeText);
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
	
	private void createSingleCheckBoxSection(Composite parent, String title, String buttonText, Consumer<Boolean>controllerUpdater, boolean defaultSelection) {
		Label partTitle = new Label(parent, SWT.NONE);
		partTitle.setText(title);
		partTitle.setFont(boldFont);

		Group scopesGroup = new Group(parent, SWT.NONE);
		scopesGroup.setLayout(new GridLayout());
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		scopesGroup.setLayoutData(gridData);

		Button button = new Button(scopesGroup, SWT.CHECK);
		button.setText(buttonText);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selection = ((Button) e.getSource()).getSelection();
				
				controller.removeTestCodeSelectionChanged(selection);
				controllerUpdater.accept(selection);
			}
		});

		button.setSelection(defaultSelection);
		controllerUpdater.accept(defaultSelection);
	}

	private void updateView() {
		fSelectionStatus = new StatusInfo();
		if (model.getClassMemberTypes()
			.isEmpty()) {
			((StatusInfo) fSelectionStatus).setError(Messages.RemoveUnusedCodeWizardPage_atLeastOneOptionNeedToBeSelected);
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
