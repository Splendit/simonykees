package eu.jsparrow.ui.wizard.semiautomatic;

import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.ATTACH_EXCEPTION_OBJECT;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.MISSING_LOG_KEY;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.PRINT_STACKTRACE_KEY;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.SYSTEM_ERR_PRINT_EXCEPTION_KEY;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.SYSTEM_ERR_PRINT_KEY;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.SYSTEM_OUT_PRINT_EXCEPTION_KEY;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.SYSTEM_OUT_PRINT_KEY;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;

/**
 * Wizard page for configuring logger rule when applying to selected resources
 * 
 * @author Andreja Sambolec, Ardit Ymeri
 * @since 1.2
 *
 */
@SuppressWarnings("restriction")
public class LoggerRuleWizardPage extends NewElementWizardPage {

	private LoggerRuleWizardPageModel model;
	private LoggerRuleWizardPageControler controller;

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
		this.controller = controler;
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
				controller.selectionChanged(SYSTEM_OUT_PRINT_KEY,
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
				controller.selectionChanged(SYSTEM_ERR_PRINT_KEY,
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
				controller.selectionChanged(PRINT_STACKTRACE_KEY, value);
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

		Group checkBoxGroup = new Group(stackTraceGroup, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 15;
		gridData.widthHint = 400;
		checkBoxGroup.setLayoutData(gridData);
		checkBoxGroup.setLayout(new GridLayout(2, false));

		// 1. printStacktrace
		printStackTraceButton = createCheckBox(checkBoxGroup,
				Messages.LoggerRuleWizardPage_replacePrintstacktraceWithLogger,
				Messages.LoggerRuleWizardPage_print_stack_trace_popup_description,
				Messages.LoggerRuleWizardPage_print_stack_trace_example_before,
				Messages.LoggerRuleWizardPage_print_stack_trace_example_after, new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						Button btn = (Button) e.getSource();
						updatePrintStackTraceOptions(btn);
					}
				});

		// 2. Add missing log statement
		missingLogStatementButton = createCheckBox(checkBoxGroup,
				Messages.LoggerRuleWizardPage_insertNewLoggerStatementInEmptyCatch,
				Messages.LoggerRuleWizardPage_missing_logger_pupup_description,
				Messages.LoggerRuleWizardPage_missing_logger_example_before,
				Messages.LoggerRuleWizardPage_missing_logger_example_after, new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						Button btn = (Button) e.getSource();
						updateMissingLogStatementOptions(btn);
					}
				});

		// 3. Always log the exception object
		logExceptionObjectButton = createCheckBox(checkBoxGroup,
				Messages.LoggerRuleWizardPage_alwaysAddExceptionParamInLoggerStatement,
				Messages.LoggerRuleWizardPage_log_exception_object_popup_description,
				Messages.LoggerRuleWizardPage_log_exception_object_example_before,
				Messages.LoggerRuleWizardPage_log_exception_object_example_after, new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						Button btn = (Button) e.getSource();
						updateLogExceptionObjectOptions(btn);
					}
				});

		// 4. defaultForExceptionLog
		defaultForExceptionLogButton = createCheckBox(checkBoxGroup,
				Messages.LoggerRuleWizardPage_alwaysUsePrintStacktraceOptionForLoggingException,
				Messages.LoggerRuleWizardPage_default_log_for_exception_popup_description,
				Messages.LoggerRuleWizardPage_default_log_for_exception_example_before,
				Messages.LoggerRuleWizardPage_default_log_for_exception_example_after, new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						Button btn = (Button) e.getSource();
						updatePrintingExceptionsOptions(btn);
					}
				});
	}
	
	private Button createCheckBox(Composite parent, String text, String popupDescription, String exampleBefore,
			String exampleAfter, SelectionAdapter selectionAdapter) {
		Button button = new Button(parent, SWT.CHECK);
		GridData gridData = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
		gridData.horizontalSpan = 1;
		gridData.verticalIndent = 5;
		button.setLayoutData(gridData);
		button.addSelectionListener(selectionAdapter);
		button.addMouseTrackListener(new PopupTrackAdapter(popupDescription, exampleBefore, exampleAfter, button));

		Label label = new Label(parent, SWT.WRAP | SWT.LEFT);
		label.setText(text);

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
		controller.selectionChanged(MISSING_LOG_KEY, value);
	}
	
	private void updatePrintStackTraceOptions(Button btn) {
		String value;
		if(btn.getSelection()) {
			value = exceptionsCombo.getItem(exceptionsCombo.getSelectionIndex());
		} else {
			value = exceptionsCombo.getItem(0);
		}
		controller.selectionChanged(PRINT_STACKTRACE_KEY, value);
	}

	private void updatePrintingExceptionsOptions(Button btn) {
		if (btn.getSelection()) {
			String comboSelectedItem = exceptionsCombo.getItem(exceptionsCombo.getSelectionIndex());
			controller.selectionChanged(SYSTEM_OUT_PRINT_EXCEPTION_KEY, comboSelectedItem);
			controller.selectionChanged(SYSTEM_ERR_PRINT_EXCEPTION_KEY, comboSelectedItem);
		} else {
			controller.selectionChanged(SYSTEM_OUT_PRINT_EXCEPTION_KEY,
					systemOutCombo.getItem(systemOutCombo.getSelectionIndex()));
			controller.selectionChanged(SYSTEM_ERR_PRINT_EXCEPTION_KEY,
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
		controller.selectionChanged(ATTACH_EXCEPTION_OBJECT, value);
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
		Map<String, String> selection = model.getCurrentSelectionMap();
		systemOutCombo.select(systemOutCombo.indexOf(selection.get(SYSTEM_OUT_PRINT_KEY)));
		systemErrCombo.select(systemErrCombo.indexOf(selection.get(SYSTEM_ERR_PRINT_KEY)));
		exceptionsCombo.select(exceptionsCombo.indexOf(selection.get(PRINT_STACKTRACE_KEY)));
		logExceptionObjectButton.setSelection(Boolean.parseBoolean(selection.get(ATTACH_EXCEPTION_OBJECT)));
		missingLogStatementButton.setSelection(!selection.get(MISSING_LOG_KEY)
			.isEmpty());
		printStackTraceButton.setSelection(!selection.get(PRINT_STACKTRACE_KEY)
			.isEmpty());
		defaultForExceptionLogButton.setSelection(selection.get(SYSTEM_OUT_PRINT_EXCEPTION_KEY)
			.equals(PRINT_STACKTRACE_KEY));
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
	
	/**
	 * A {@link MouseTrackAdapter} for showing a popup on hover. 
	 *
	 */
	class PopupTrackAdapter extends MouseTrackAdapter {
		private Shell popup;
		private String popupDescription;
		private Control parent;
		private String before;
		private String after;
		
		/**
		 * Creates a {@link MouseTrackAdapter} for showing a popup on hover.
		 * 
		 * @param description
		 *            the description on the popup.
		 * @param before
		 *            code example before applying the rule
		 * @param after
		 *            code example after applying the rule
		 * @param parent
		 *            parent control of the popup
		 */
		public PopupTrackAdapter(String description, String before, String after, Control parent) {
			this.popupDescription = description;
			this.parent = parent;
			this.before = before;
			this.after = after;
		}
		
		@Override
		public void mouseEnter(MouseEvent e) {
			showPopup(popupDescription, before, after, parent);
		}
		
		@Override
		public void mouseExit(MouseEvent e) {
			closePopup();
		}
		
		private void showPopup(String popupDescription, String before, String after, Control parent) {
			if(popup == null) {	
				popup = new Shell(parent.getShell().getDisplay(),  SWT.RESIZE);
				
				GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
				gridData.horizontalSpan = 1;
				gridData.widthHint = 400;
				
				popup.setLayoutData(gridData);
				popup.setLayout(new GridLayout(1, false));
				
				Label description = new Label(popup, SWT.WRAP | SWT.LEFT);
				final GridData data = new GridData(SWT.HORIZONTAL, SWT.TOP, true, false, 1, 1);
				data.horizontalSpan = 1;
				data.widthHint = 380;
				description.setLayoutData(data);
				description.setText(popupDescription);
				
				RGB rgbWhite = new RGB(252, 252, 252);
				RGB rgbBlack = new RGB(60, 60, 60);
				
				FontData monospaceFontData = new FontData("Monospace", 9, SWT.NONE); //$NON-NLS-1$
				Text codeExampleBefore = new Text(popup, SWT.MULTI | SWT.BORDER);
				codeExampleBefore.setFont(new Font(codeExampleBefore.getDisplay(), monospaceFontData));
				codeExampleBefore.setEditable(false);
				codeExampleBefore.setText(before);
				gridData = new GridData(SWT.FILL, SWT.CENTER, false, true);
				gridData.horizontalSpan = 1;
				gridData.widthHint = 310;
				codeExampleBefore.setLayoutData(gridData);
				// a work around for disabling the cursor
				codeExampleBefore.setEnabled(false);
				codeExampleBefore.setBackground(new Color(Display.getCurrent(), rgbWhite));
				codeExampleBefore.setForeground(new Color(Display.getCurrent(), rgbBlack));
				
				Label willBeTransformedToLabel = new Label(popup, SWT.NONE);
				willBeTransformedToLabel.setText(Messages.LoggerRuleWizardPage_will_be_transformed_to);
				willBeTransformedToLabel.setFocus();
				
				Text codeExampleAfter = new Text(popup, SWT.MULTI | SWT.BORDER);
				codeExampleAfter.setEditable(false);
				codeExampleAfter.setText(after);
				codeExampleAfter.setFont(new Font(codeExampleAfter.getDisplay(), monospaceFontData));
				gridData = new GridData(SWT.FILL, SWT.CENTER, false, true);
				gridData.horizontalSpan = 1;
				gridData.widthHint = 310;
				codeExampleAfter.setLayoutData(gridData);
				// a work around for disabling the cursor
				codeExampleAfter.setEnabled(false);
				codeExampleAfter.setBackground(new Color(Display.getCurrent(), rgbWhite));
				codeExampleAfter.setForeground(new Color(Display.getCurrent(), rgbBlack));
				
				popup.setFocus();
				popup.pack();
				popup.open();
				
				
			}
		}
		
		private void closePopup() {
			if(popup != null && !popup.isDisposed()) {
				popup.close();
				popup = null;
			}
		}
	}
}

