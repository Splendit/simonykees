package at.splendit.simonykees.core.ui.wizard.semiautomatic;

import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import at.splendit.simonykees.core.rule.impl.standardLogger.StandardLoggerOptions;
import at.splendit.simonykees.core.ui.wizard.IValueChangeListener;
import at.splendit.simonykees.i18n.Messages;

public class LoggerRuleWizardPage extends NewElementWizardPage {

	private LoggerRuleWizardPageModel model;
	private LoggerRuleWizardPageControler controler;

	private Composite composite;

	private Combo systemOutCombo;
	private Combo systemErrCombo;
	private Combo stackTraceCombo;

	protected IStatus fSelectionStatus;

	private final String NO_SEVERITY_LEVEL = ""; //$NON-NLS-1$

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
		composite.setLayout(new GridLayout(2, true));

		setControl(composite);

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
	}

	private void createSystemOutPart(Composite parent) {
		Label systemOutLabel = new Label(parent, SWT.NONE);
		systemOutLabel.setText("System.out.print to Logger?");

		systemOutCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		populateSystemOutCombo();
		systemOutCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				controler.selectionChanged(StandardLoggerOptions.SYSTEM_OUT_PRINT,
						((Combo) e.getSource()).getItem(((Combo) e.getSource()).getSelectionIndex()));
			}
		});
		GridData gridData = new GridData(GridData.END, GridData.FILL, false, false);
		gridData.widthHint = 200;
		systemOutCombo.setLayoutData(gridData);
	}

	private void createSystemErrPart(Composite parent) {
		Label systemErrLabel = new Label(parent, SWT.NONE);
		systemErrLabel.setText("System.err.print to Logger?");

		systemErrCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		populateSystemErrCombo();
		systemErrCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				controler.selectionChanged(StandardLoggerOptions.SYSTEM_ERR_PRINT,
						((Combo) e.getSource()).getItem(((Combo) e.getSource()).getSelectionIndex()));
			}
		});
		GridData gridData = new GridData(GridData.END, GridData.FILL, false, false);
		gridData.widthHint = 200;
		systemErrCombo.setLayoutData(gridData);
	}

	private void createStackTracePart(Composite parent) {
		Label stackTraceLabel = new Label(parent, SWT.NONE);
		stackTraceLabel.setText("printStackTrace to Logger?");

		stackTraceCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		populateStackTraceCombo();
		stackTraceCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				controler.selectionChanged(StandardLoggerOptions.PRINT_STACKTRACE,
						((Combo) e.getSource()).getItem(((Combo) e.getSource()).getSelectionIndex()));
			}
		});
		GridData gridData = new GridData(GridData.END, GridData.FILL, false, false);
		gridData.widthHint = 200;
		stackTraceCombo.setLayoutData(gridData);
	}

	/**
	 * Set all items for the dropdown ({@link Combo})
	 */
	private void populateSystemOutCombo() {
		Set<String> severityLevels = model.getSystemOutReplaceOptions();
		systemOutCombo.add(NO_SEVERITY_LEVEL);
		for (String severityLevel : severityLevels) {
			systemOutCombo.add(severityLevel);
		}
	}

	/**
	 * Set all items for the dropdown ({@link Combo})
	 */
	private void populateSystemErrCombo() {
		Set<String> severityLevels = model.getSystemErrReplaceOptions();
		systemErrCombo.add(NO_SEVERITY_LEVEL);
		for (String severityLevel : severityLevels) {
			systemErrCombo.add(severityLevel);
		}
	}

	/**
	 * Set all items for the dropdown ({@link Combo})
	 */
	private void populateStackTraceCombo() {
		Set<String> severityLevels = model.getPrintStackTraceReplaceOptions();
		stackTraceCombo.add(NO_SEVERITY_LEVEL);
		for (String severityLevel : severityLevels) {
			stackTraceCombo.add(severityLevel);
		}
	}

	/**
	 * Updates view with data every time something is changed in model.
	 */
	private void updateData() {
		// TODO update view
	}

	private void initializeData() {
		systemOutCombo.select(
				systemOutCombo.indexOf(model.getCurrentSelectionMap().get(StandardLoggerOptions.SYSTEM_OUT_PRINT)));
		systemErrCombo.select(
				systemErrCombo.indexOf(model.getCurrentSelectionMap().get(StandardLoggerOptions.SYSTEM_ERR_PRINT)));
		stackTraceCombo.select(
				stackTraceCombo.indexOf(model.getCurrentSelectionMap().get(StandardLoggerOptions.PRINT_STACKTRACE)));
	}

	protected void doStatusUpdate() {
		if (!model.getSelectionStatus().isEmpty()) {
			((StatusInfo) fSelectionStatus).setError(model.getSelectionStatus());
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
}
