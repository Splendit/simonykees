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
	private Combo stackTraceCombo;
	private Combo missingLogCombo;

	private Button defaultForExceptionLogg;

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
		createStackTracePart(composite);
		createMissingLogPart(composite);

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
				updatePrintingExceptionsOptions(defaultForExceptionLogg);
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
				updatePrintingExceptionsOptions(defaultForExceptionLogg);
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

	private void createStackTracePart(Composite parent) {
		Group stackTraceGroup = new Group(parent, SWT.NONE);
		stackTraceGroup.setText(Messages.LoggerRuleWizardPage_stackTraceLabel);
		stackTraceGroup.setFont(boldFont);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 15;
		gridData.widthHint = 400;
		stackTraceGroup.setLayoutData(gridData);
		stackTraceGroup.setLayout(new GridLayout(2, false));

		Label stackTraceLabel = new Label(stackTraceGroup, SWT.NONE);
		stackTraceLabel.setText(Messages.LoggerRuleWizardPage_severityLevelLabel);

		stackTraceCombo = new Combo(stackTraceGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		populateStackTraceCombo();
		stackTraceCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				controler.selectionChanged(StandardLoggerConstants.PRINT_STACKTRACE_KEY,
						((Combo) e.getSource()).getItem(((Combo) e.getSource()).getSelectionIndex()));
				updatePrintingExceptionsOptions(defaultForExceptionLogg);
			}
		});
		gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
		gridData.widthHint = 200;
		stackTraceCombo.setLayoutData(gridData);

		Label stackTraceExplainLabel = new Label(stackTraceGroup, SWT.WRAP | SWT.LEFT);
		gridData = new GridData(SWT.HORIZONTAL, SWT.TOP, true, false, 1, 1);
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 5;
		stackTraceExplainLabel.setLayoutData(gridData);
		stackTraceExplainLabel.setText(Messages.LoggerRuleWizardPage_stackTraceMessageLabel);

		defaultForExceptionLogg = new Button(stackTraceGroup, SWT.CHECK);
		gridData = new GridData(SWT.HORIZONTAL, SWT.TOP, true, false, 1, 1);
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 5;
		defaultForExceptionLogg.setLayoutData(gridData);
		defaultForExceptionLogg
			.setText(Messages.LoggerRuleWizardPage_alwaysUsePrintStacktraceOptionForLoggingException);
		defaultForExceptionLogg.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Button btn = (Button) e.getSource();
				updatePrintingExceptionsOptions(btn);
			}
		});
	}

	private void updatePrintingExceptionsOptions(Button btn) {
		if (btn.getSelection()) {
			String comboSelectedItem = stackTraceCombo.getItem(stackTraceCombo.getSelectionIndex());
			controler.selectionChanged(StandardLoggerConstants.SYSTEM_OUT_PRINT_EXCEPTION_KEY, comboSelectedItem);
			controler.selectionChanged(StandardLoggerConstants.SYSTEM_ERR_PRINT_EXCEPTION_KEY, comboSelectedItem);
		} else {
			controler.selectionChanged(StandardLoggerConstants.SYSTEM_OUT_PRINT_EXCEPTION_KEY,
					systemOutCombo.getItem(systemOutCombo.getSelectionIndex()));
			controler.selectionChanged(StandardLoggerConstants.SYSTEM_ERR_PRINT_EXCEPTION_KEY,
					systemErrCombo.getItem(systemErrCombo.getSelectionIndex()));
		}
	}

	private void createMissingLogPart(Composite parent) {
		Group missingLogGroup = new Group(parent, SWT.NONE);
		missingLogGroup.setText(Messages.LoggerRuleWizardPage_missingLogLabel);
		missingLogGroup.setFont(boldFont);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 15;
		gridData.widthHint = 400;
		missingLogGroup.setLayoutData(gridData);
		missingLogGroup.setLayout(new GridLayout(2, false));

		Label missingLoggLabel = new Label(missingLogGroup, SWT.NONE);
		missingLoggLabel.setText(Messages.LoggerRuleWizardPage_severityLevelLabel);

		missingLogCombo = new Combo(missingLogGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		populateMissingLogCombo();
		missingLogCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				controler.selectionChanged(StandardLoggerConstants.MISSING_LOG_KEY,
						((Combo) e.getSource()).getItem(((Combo) e.getSource()).getSelectionIndex()));
			}
		});
		gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
		gridData.widthHint = 200;
		missingLogCombo.setLayoutData(gridData);

		Label missingLogExplainLabel = new Label(missingLogGroup, SWT.WRAP | SWT.LEFT);
		gridData = new GridData(SWT.HORIZONTAL, SWT.TOP, true, false, 1, 1);
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 5;
		missingLogExplainLabel.setLayoutData(gridData);
		missingLogExplainLabel.setText(Messages.LoggerRuleWizardPage_missingLogMessageLabel);

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
		severityLevels.forEach(stackTraceCombo::add);
	}

	/**
	 * Set all items for the dropdown ({@link Combo})
	 */
	private void populateMissingLogCombo() {
		Set<String> severityLevels = model.getMissingLogInsertOptions();
		severityLevels.forEach(missingLogCombo::add);
	}

	private void initializeData() {
		systemOutCombo.select(systemOutCombo.indexOf(model.getCurrentSelectionMap()
			.get(StandardLoggerConstants.SYSTEM_OUT_PRINT_KEY)));
		systemErrCombo.select(systemErrCombo.indexOf(model.getCurrentSelectionMap()
			.get(StandardLoggerConstants.SYSTEM_ERR_PRINT_KEY)));
		stackTraceCombo.select(stackTraceCombo.indexOf(model.getCurrentSelectionMap()
			.get(StandardLoggerConstants.PRINT_STACKTRACE_KEY)));
		missingLogCombo.select(missingLogCombo.indexOf(model.getCurrentSelectionMap()
			.get(StandardLoggerConstants.MISSING_LOG_KEY)));
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
