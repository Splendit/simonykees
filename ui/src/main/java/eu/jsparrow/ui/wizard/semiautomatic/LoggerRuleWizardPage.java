package eu.jsparrow.ui.wizard.semiautomatic;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;

/**
 * Wizard page for configuring logger rule when applying to selected resources
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
@SuppressWarnings("restriction")
public class LoggerRuleWizardPage extends NewElementWizardPage {

	private LoggerRuleWizardPageModel model;
	private LoggerRuleWizardPageControler controler;

	private Combo systemOutCombo;
	private Combo systemErrCombo;
	private Combo exceptionsCombo;

	private Button defaultForExceptionLogButton;
	private Button missingLogStatementButton;
	private Button printStackTraceButton;
	private Button logExceptionObjectButton;

	private Font boldFont;

	protected IStatus fSelectionStatus;

	public LoggerRuleWizardPage(LoggerRuleWizardPageModel model, LoggerRuleWizardPageControler controler) {
		super(Messages.LoggerRuleWizardPage_pageName);
		setTitle(Messages.LoggerRuleWizard_title);
		setDescription(Messages.LoggerRuleWizardPage_description);

		this.model = model;
		this.controler = controler;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite;
		initializeDialogUnits(parent);

		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		setControl(composite);

		FontDescriptor boldDescriptor = FontDescriptor.createFrom(parent.getFont())
			.setStyle(SWT.BOLD);
		boldFont = boldDescriptor.createFont(composite.getDisplay());

		createSystemOutPart(composite);
		createSystemErrPart(composite);
		createLogExceptionsPart(composite);

		model.addListener(this::doStatusUpdate);

		initializeData();
		doStatusUpdate();

		Dialog.applyDialogFont(parent);
	}

	private void createSystemOutPart(Composite parent) {
		Group sysOutGroup = new Group(parent, SWT.NONE);
		sysOutGroup.setText(Messages.LoggerRuleWizardPage_sysOutLabel);
		sysOutGroup.setFont(boldFont);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 2;
		gridData.widthHint = 400;
		sysOutGroup.setLayoutData(gridData);
		sysOutGroup.setLayout(new GridLayout(2, false));

		Label systemOutLabel = new Label(sysOutGroup, SWT.NONE);
		systemOutLabel.setText(Messages.LoggerRuleWizardPage_severityLevelLabel);

		systemOutCombo = new Combo(sysOutGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		populateSystemOutCombo();
		systemOutCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				controler.selectionChanged(StandardLoggerConstants.SYSTEM_OUT_PRINT_KEY,
						((Combo) e.getSource()).getItem(((Combo) e.getSource()).getSelectionIndex()));
				updatePrintingExceptionsOptions(defaultForExceptionLogButton);
			}
		});
		gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
		gridData.widthHint = 150;
		systemOutCombo.setLayoutData(gridData);

		Label systemOutExplainLabel = new Label(sysOutGroup, SWT.WRAP | SWT.LEFT);
		gridData = new GridData(SWT.HORIZONTAL, SWT.TOP, true, false, 1, 1);
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 5;
		systemOutExplainLabel.setLayoutData(gridData);
		systemOutExplainLabel.setText(Messages.LoggerRuleWizardPage_sysOutMessageLabel);
	}

	private void createSystemErrPart(Composite parent) {
		Group sysErrGroup = new Group(parent, SWT.NONE);
		sysErrGroup.setText(Messages.LoggerRuleWizardPage_sysErrLabel);
		sysErrGroup.setFont(boldFont);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 15;
		gridData.widthHint = 400;
		sysErrGroup.setLayoutData(gridData);
		sysErrGroup.setLayout(new GridLayout(2, false));

		Label systemErrLabel = new Label(sysErrGroup, SWT.NONE);
		systemErrLabel.setText(Messages.LoggerRuleWizardPage_severityLevelLabel);

		systemErrCombo = new Combo(sysErrGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		populateSystemErrCombo();
		systemErrCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				controler.selectionChanged(StandardLoggerConstants.SYSTEM_ERR_PRINT_KEY,
						((Combo) e.getSource()).getItem(((Combo) e.getSource()).getSelectionIndex()));
				updatePrintingExceptionsOptions(defaultForExceptionLogButton);
			}
		});
		gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
		gridData.widthHint = 150;
		systemErrCombo.setLayoutData(gridData);

		Label systemErrExplainLabel = new Label(sysErrGroup, SWT.WRAP | SWT.LEFT);
		gridData = new GridData(SWT.HORIZONTAL, SWT.TOP, true, false, 1, 1);
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 5;
		systemErrExplainLabel.setLayoutData(gridData);
		systemErrExplainLabel.setText(Messages.LoggerRuleWizardPage_sysErrMessageLabel);
	}

	private void createLogExceptionsPart(Composite parent) {
		Group stackTraceGroup = new Group(parent, SWT.NONE);
		stackTraceGroup.setText(Messages.LoggerRuleWizardPage_loggingExceptionsLabel);
		stackTraceGroup.setFont(boldFont);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 15;
		gridData.widthHint = 400;
		stackTraceGroup.setLayoutData(gridData);
		stackTraceGroup.setLayout(new GridLayout(2, false));

		Label stackTraceLabel = new Label(stackTraceGroup, SWT.NONE);
		stackTraceLabel.setText(Messages.LoggerRuleWizardPage_severityLevelLabel);

		exceptionsCombo = new Combo(stackTraceGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		populateStackTraceCombo();
		exceptionsCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo combo = (Combo) e.getSource();
				int selectedIndex = combo.getSelectionIndex();
				String value = combo.getItem(selectedIndex);
				controler.selectionChanged(StandardLoggerConstants.PRINT_STACKTRACE_KEY, value);
				setExceptionButtonsEnabled(selectedIndex != 0);
				updatePrintingExceptionsOptions(defaultForExceptionLogButton);
				updateMissingLogStatementOptions(missingLogStatementButton);
				updatePrintStackTraceOptions(printStackTraceButton);
				updateLogExceptionObjectOptions(logExceptionObjectButton);
			}
		});
		gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
		gridData.widthHint = 200;
		exceptionsCombo.setLayoutData(gridData);

		// 1. printStacktrace
		printStackTraceButton = createExceptionGroupButton(stackTraceGroup,
				Messages.LoggerRuleWizardPage_replacePrintstacktraceWithLogger, true,
				new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						Button btn = (Button) e.getSource();
						updatePrintStackTraceOptions(btn);
					}
				});

		// 2. Add missing log statement
		missingLogStatementButton = createExceptionGroupButton(stackTraceGroup,
				Messages.LoggerRuleWizardPage_insertNewLoggerStatementInEmptyCatch, true,
				new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						Button btn = (Button) e.getSource();
						updateMissingLogStatementOptions(btn);
					}
				});

		// 3. defaultForExceptionLog
		defaultForExceptionLogButton = createExceptionGroupButton(stackTraceGroup,
				Messages.LoggerRuleWizardPage_alwaysUsePrintStacktraceOptionForLoggingException,
				new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						Button btn = (Button) e.getSource();
						updatePrintingExceptionsOptions(btn);
					}
				});

		// 4. Always log the exception object
		logExceptionObjectButton = createExceptionGroupButton(stackTraceGroup,
				Messages.LoggerRuleWizardPage_alwaysAddExceptionParamInLoggerStatement, true, new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						Button btn = (Button) e.getSource();
						updateLogExceptionObjectOptions(btn);
					}
				});
	}
	
	private Button createExceptionGroupButton(Composite parent, String text, boolean selection,
			SelectionAdapter selectionAdapter) {
		Button button = createExceptionGroupButton(parent, text, selectionAdapter);
		button.setSelection(selection);
		return button;
	}
	
	private Button createExceptionGroupButton(Composite parent, String text, SelectionAdapter selectionAdapter) {
		Button button = new Button(parent, SWT.CHECK);
		GridData gridData = new GridData(SWT.HORIZONTAL, SWT.TOP, true, false, 1, 1);
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 5;
		button.setLayoutData(gridData);
		button.setText(text);
		button.addSelectionListener(selectionAdapter);
		return button;
	}
	
	private void setExceptionButtonsEnabled(boolean value) {
		logExceptionObjectButton.setEnabled(value);
		missingLogStatementButton.setEnabled(value);
		defaultForExceptionLogButton.setEnabled(value);
		printStackTraceButton.setEnabled(value);
	}
	
	private void updateMissingLogStatementOptions(Button btn) {
		String value;
		if(btn.getSelection()) {
			value = exceptionsCombo.getItem(exceptionsCombo.getSelectionIndex());
		} else {
			value = exceptionsCombo.getItem(0);
		}
		controler.selectionChanged(StandardLoggerConstants.MISSING_LOG_KEY, value);
	}
	
	private void updatePrintStackTraceOptions(Button btn) {
		String value;
		if(btn.getSelection()) {
			value = exceptionsCombo.getItem(exceptionsCombo.getSelectionIndex());
		} else {
			value = exceptionsCombo.getItem(0);
		}
		controler.selectionChanged(StandardLoggerConstants.PRINT_STACKTRACE_KEY, value);
	}

	private void updatePrintingExceptionsOptions(Button btn) {
		if (btn.getSelection()) {
			String comboSelectedItem = exceptionsCombo.getItem(exceptionsCombo.getSelectionIndex());
			controler.selectionChanged(StandardLoggerConstants.SYSTEM_OUT_PRINT_EXCEPTION_KEY, comboSelectedItem);
			controler.selectionChanged(StandardLoggerConstants.SYSTEM_ERR_PRINT_EXCEPTION_KEY, comboSelectedItem);
		} else {
			controler.selectionChanged(StandardLoggerConstants.SYSTEM_OUT_PRINT_EXCEPTION_KEY,
					systemOutCombo.getItem(systemOutCombo.getSelectionIndex()));
			controler.selectionChanged(StandardLoggerConstants.SYSTEM_ERR_PRINT_EXCEPTION_KEY,
					systemErrCombo.getItem(systemErrCombo.getSelectionIndex()));
		}
	}
	
	private void updateLogExceptionObjectOptions(Button btn) {
		String value;
		if(btn.getSelection() && exceptionsCombo.getSelectionIndex() != 0) {
			value = Boolean.TRUE.toString();
		} else {
			value = Boolean.FALSE.toString();
		}
		controler.selectionChanged(StandardLoggerConstants.ATTACH_EXCEPTION_OBJECT, value);
	}

	@Override
	public void dispose() {
		super.dispose();
		boldFont.dispose();
	}

	/**
	 * Set all items for the dropdown ({@link Combo})
	 */
	private void populateSystemOutCombo() {
		Set<String> severityLevels = model.getSystemOutReplaceOptions();
		severityLevels.forEach(systemOutCombo::add);
	}

	/**
	 * Set all items for the dropdown ({@link Combo})
	 */
	private void populateSystemErrCombo() {
		Set<String> severityLevels = model.getSystemErrReplaceOptions();
		severityLevels.forEach(systemErrCombo::add);
	}

	/**
	 * Set all items for the dropdown ({@link Combo})
	 */
	private void populateStackTraceCombo() {
		Set<String> severityLevels = model.getPrintStackTraceReplaceOptions();
		severityLevels.forEach(exceptionsCombo::add);
	}

	private void initializeData() {
		systemOutCombo.select(systemOutCombo.indexOf(model.getCurrentSelectionMap()
			.get(StandardLoggerConstants.SYSTEM_OUT_PRINT_KEY)));
		systemErrCombo.select(systemErrCombo.indexOf(model.getCurrentSelectionMap()
			.get(StandardLoggerConstants.SYSTEM_ERR_PRINT_KEY)));
		exceptionsCombo.select(exceptionsCombo.indexOf(model.getCurrentSelectionMap()
			.get(StandardLoggerConstants.PRINT_STACKTRACE_KEY)));
	}

	/**
	 * Updates title status with status info every time something is changed in
	 * model. If status has any message, warning will be shown, otherwise title
	 * will be shown.
	 */
	protected void doStatusUpdate() {
		if (StringUtils.isEmpty(model.getSelectionStatus())) {
			fSelectionStatus = new StatusInfo();
		} else if (model.getSelectionStatus()
			.equals(Messages.LoggerRuleWizardPageModel_err_noTransformation)) {
			((StatusInfo) fSelectionStatus).setError(model.getSelectionStatus());
		} else {
			((StatusInfo) fSelectionStatus).setWarning(model.getSelectionStatus());
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
