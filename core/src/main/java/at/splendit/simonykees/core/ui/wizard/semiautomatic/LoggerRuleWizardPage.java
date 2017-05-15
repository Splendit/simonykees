package at.splendit.simonykees.core.ui.wizard.semiautomatic;

import java.util.Set;

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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import at.splendit.simonykees.core.rule.impl.standardLogger.StandardLoggerOptions;
import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;
import at.splendit.simonykees.core.ui.wizard.IValueChangeListener;
import at.splendit.simonykees.i18n.Messages;

/**
 * Wizard page for configuring logger rule when applying to selected resources
 * 
 * @author andreja.sambolec
 * @since 1.2
 *
 */
@SuppressWarnings("restriction")
public class LoggerRuleWizardPage extends NewElementWizardPage {

	private LoggerRuleWizardPageModel model;
	private LoggerRuleWizardPageControler controler;

	private Composite composite;

	private Combo systemOutCombo;
	private Combo systemErrCombo;
	private Combo stackTraceCombo;

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
		initializeDialogUnits(parent);

		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		setControl(composite);

		FontDescriptor boldDescriptor = FontDescriptor.createFrom(parent.getFont()).setStyle(SWT.BOLD);
		boldFont = boldDescriptor.createFont(composite.getDisplay());

		createSystemOutPart(composite);
		createSystemErrPart(composite);
		createStackTracePart(composite);

		model.addListener(new IValueChangeListener() {

			@Override
			public void valueChanged() {
				doStatusUpdate();
			}
		});

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
				controler.selectionChanged(StandardLoggerOptions.SYSTEM_OUT_PRINT,
						((Combo) e.getSource()).getItem(((Combo) e.getSource()).getSelectionIndex()));
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
		systemOutExplainLabel.setText(
				Messages.LoggerRuleWizardPage_sysOutMessageLabel);
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
				controler.selectionChanged(StandardLoggerOptions.SYSTEM_ERR_PRINT,
						((Combo) e.getSource()).getItem(((Combo) e.getSource()).getSelectionIndex()));
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
		systemErrExplainLabel.setText(
				Messages.LoggerRuleWizardPage_sysErrMessageLabel);
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
				controler.selectionChanged(StandardLoggerOptions.PRINT_STACKTRACE,
						((Combo) e.getSource()).getItem(((Combo) e.getSource()).getSelectionIndex()));
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
		stackTraceExplainLabel.setText(
				Messages.LoggerRuleWizardPage_stackTraceMessageLabel);
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
		for (String severityLevel : severityLevels) {
			systemOutCombo.add(severityLevel);
		}
	}

	/**
	 * Set all items for the dropdown ({@link Combo})
	 */
	private void populateSystemErrCombo() {
		Set<String> severityLevels = model.getSystemErrReplaceOptions();
		for (String severityLevel : severityLevels) {
			systemErrCombo.add(severityLevel);
		}
	}

	/**
	 * Set all items for the dropdown ({@link Combo})
	 */
	private void populateStackTraceCombo() {
		Set<String> severityLevels = model.getPrintStackTraceReplaceOptions();
		for (String severityLevel : severityLevels) {
			stackTraceCombo.add(severityLevel);
		}
	}

	private void initializeData() {
		systemOutCombo.select(
				systemOutCombo.indexOf(model.getCurrentSelectionMap().get(StandardLoggerOptions.SYSTEM_OUT_PRINT)));
		systemErrCombo.select(
				systemErrCombo.indexOf(model.getCurrentSelectionMap().get(StandardLoggerOptions.SYSTEM_ERR_PRINT)));
		stackTraceCombo.select(
				stackTraceCombo.indexOf(model.getCurrentSelectionMap().get(StandardLoggerOptions.PRINT_STACKTRACE)));
	}

	/**
	 * Updates title status with status info every time something is changed in
	 * model. If status has any message, warning will be shown, otherwise title
	 * will be shown.
	 */
	protected void doStatusUpdate() {
		if (model.getSelectionStatus().isEmpty()) {
			fSelectionStatus = new StatusInfo();
		} else if (model.getSelectionStatus().equals(Messages.LoggerRuleWizardPageModel_err_noTransformation)) {
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
