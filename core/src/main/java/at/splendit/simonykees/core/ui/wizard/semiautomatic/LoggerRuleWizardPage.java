package at.splendit.simonykees.core.ui.wizard.semiautomatic;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import at.splendit.simonykees.core.ui.wizard.IValueChangeListener;
import at.splendit.simonykees.i18n.Messages;

public class LoggerRuleWizardPage extends NewElementWizardPage {

	private LoggerRuleWizardPageModel model;
	private LoggerRuleWizardPageControler controler;
	
	private Composite composite;
	
	private Combo systemOutCombo;
	private Combo systemErrCombo;
	private Combo stackTraceCombo;
	
	private final String EMPTY_PROFIL = "None"; 

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
		
		createSystemOutPart(composite);		
		createSystemErrPart(composite);
		createStackTracePart(composite);
		
		model.addListener(new IValueChangeListener() {

			@Override
			public void valueChanged() {
				updateData();
			}
		});
	}

	private void createSystemOutPart(Composite parent) {
		Label systemOutLabel = new Label(parent, SWT.NONE);
		systemOutLabel.setText("System.out.print");
		
		systemOutCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		populateSystemOutCombo();
		systemOutCombo.addSelectionListener(createComboSelectionListener());
		GridData gridData = new GridData(GridData.END, GridData.FILL, false, false);
		gridData.widthHint = 200;
		systemOutCombo.setLayoutData(gridData);
		initializeSystemOutCombo();
	}

	private void createSystemErrPart(Composite parent) {
		Label systemErrLabel = new Label(parent, SWT.NONE);
		systemErrLabel.setText("System.err.print");
		
		systemErrCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		populateSystemErrCombo();
		systemErrCombo.addSelectionListener(createComboSelectionListener());
		GridData gridData = new GridData(GridData.END, GridData.FILL, false, false);
		gridData.widthHint = 200;
		systemErrCombo.setLayoutData(gridData);
		initializeSystemErrCombo();
	}

	private void createStackTracePart(Composite parent) {
		Label stackTraceLabel = new Label(parent, SWT.NONE);
		stackTraceLabel.setText("Print.stack.trace");
		
		stackTraceCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		populateStackTraceCombo();
		stackTraceCombo.addSelectionListener(createComboSelectionListener());
		GridData gridData = new GridData(GridData.END, GridData.FILL, false, false);
		gridData.widthHint = 200;
		stackTraceCombo.setLayoutData(gridData);
		initializeStackTraceCombo();
	}

	/**
	 * {@link SelectionListener} for the profile dropdown ({@link Combo}).
	 * 
	 * @return {@link SelectionListener} that reacts to changes of the selected
	 *         element.
	 */
	private SelectionListener createComboSelectionListener() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				//TODO validateSelection();
				controler.selectionChanged(e.getSource(), ((Combo)e.getSource()).getSelectionIndex());
			}
		};
	}
	
	/**
	 * Set all items for the dropdown ({@link Combo}) 
	 */
	private void populateSystemOutCombo() {
		//TODO add all severity levels defined in rule for sys out log
		List<String> severityLevels = new ArrayList<>();  
		systemOutCombo.add(EMPTY_PROFIL);
		for (String severityLevel : severityLevels) {
			systemOutCombo.add(severityLevel);
		}
	}
	
	/**
	 * Set all items for the dropdown ({@link Combo}) 
	 */
	private void populateSystemErrCombo() {
		//TODO add all severity levels defined in rule for sys err log
		List<String> severityLevels = new ArrayList<>();  
		systemErrCombo.add(EMPTY_PROFIL);
		for (String severityLevel : severityLevels) {
			systemErrCombo.add(severityLevel);
		}
	}
	
	/**
	 * Set all items for the dropdown ({@link Combo}) 
	 */
	private void populateStackTraceCombo() {
		//TODO add all severity levels defined in rule for stack trace log
		List<String> severityLevels = new ArrayList<>();  
		stackTraceCombo.add(EMPTY_PROFIL);
		for (String severityLevel : severityLevels) {
			stackTraceCombo.add(severityLevel);
		}
	}
	
	/**
	 * Initializes systemOut combo to INFO severity level
	 */
	private void initializeSystemOutCombo() {
		//TODO init to INFO
	}
	
	/**
	 * Initializes systemErr combo to ERROR severity level
	 */
	private void initializeSystemErrCombo() {
		//TODO init to ERROR
	}
	
	/**
	 * Initializes stackTrace combo to ERROR severity level
	 */
	private void initializeStackTraceCombo() {
		//TODO init to ERROR
	}
	
	/**
	 * Updates view with data every time something is changed in model.
	 */
	private void updateData() {
		// TODO implement
	}
}
