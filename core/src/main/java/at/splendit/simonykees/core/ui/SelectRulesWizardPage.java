package at.splendit.simonykees.core.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.core.ui.preference.SimonykeesPreferenceManager;

/**
 * Lists all rules as checkboxes and a description for the currently selected
 * rule.
 * 
 * @author Ludwig Werzowa
 * @since 0.9
 */
public class SelectRulesWizardPage extends AbstractWizardPage {

	private CheckboxTableViewer rulesCheckboxTableViewer;
	private StyledText descriptionStyledText;
	private RefactoringRule<? extends ASTVisitor> selectedRefactoringRule;

	private Combo selectProfileCombo;

	protected SelectRulesWizardPage() {
		super(Messages.SelectRulesWizardPage_page_name);
		setTitle(Messages.SelectRulesWizardPage_title);
		setDescription(Messages.SelectRulesWizardPage_description);
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setControl(parent);
		GridLayout layout = new GridLayout();

		parent.setLayout(layout);

		selectProfileCombo = new Combo(parent, SWT.READ_ONLY);
		populateSelectProfileCombo();
		
		// TODO maybe select the current profile name
		selectProfileCombo.select(0); 
		
		// Create a horizontal separator
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createSelectAllButton(parent);

		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createRulesCheckboxTableViewer(sashForm);
		createRuleDescriptionViewer(sashForm);

	}

	private void populateSelectProfileCombo() {
		SimonykeesPreferenceManager.getAllProfileNamesWithBuiltInSuffix().stream()
				.forEach(profileName -> selectProfileCombo.add(profileName));
	}

	/**
	 * Adds a button to select / deselect all rules.
	 * 
	 * @param parent
	 */
	private void createSelectAllButton(Composite parent) {
		Button selectAllButton = new Button(parent, SWT.CHECK);
		selectAllButton.setText(Messages.SelectRulesWizardPage_select_unselect_all);
		selectAllButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				rulesCheckboxTableViewer.setAllChecked(selectAllButton.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing
			}
		});
	}

	private void createRulesCheckboxTableViewer(Composite parent) {

		// SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		// sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// createSelectAllButton(sashForm);

		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = RulesContainer.getAllRules();

		rulesCheckboxTableViewer = CheckboxTableViewer.newCheckList(parent, SWT.CHECK | SWT.BORDER);
		rulesCheckboxTableViewer.setContentProvider(new ArrayContentProvider());

		/*
		 * the selection listener for the selected row (has nothing to do with
		 * the checkbox)
		 */
		rulesCheckboxTableViewer.addSelectionChangedListener(createSelectionChangedListener());

		rulesCheckboxTableViewer.setInput(rules);

		// set label text
		rulesCheckboxTableViewer.setLabelProvider(new StyledCellLabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public void update(ViewerCell cell) {
				cell.setText(((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) cell.getElement()).getName());
			}
		});

		// set selected rules according to the current profile
		rulesCheckboxTableViewer.setCheckedElements(rules.stream()
				.filter(r -> SimonykeesPreferenceManager.isRuleSelectedInCurrentProfile(r.getId())).toArray());
	}

	private void createRuleDescriptionViewer(Composite parent) {

		/*
		 * There is a known issue with automatically showing and hiding
		 * scrollbars and SWT.WRAP. Using StyledText and
		 * setAlwaysShowScrollBars(false) makes the vertical scroll work
		 * correctly at least.
		 */
		descriptionStyledText = new StyledText(parent, SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		descriptionStyledText.setAlwaysShowScrollBars(false);

		descriptionStyledText.setEditable(false);

		populateDescriptionTextViewer();
	}

	private void populateDescriptionTextViewer() {
		if (selectedRefactoringRule == null) {
			descriptionStyledText.setText(Messages.SelectRulesWizardPage_rule_description_default_text);
		} else {
			descriptionStyledText.setText(selectedRefactoringRule.getDescription());
		}
	}

	/**
	 * {@link ISelectionChangedListener} used to get the currently selected
	 * rule, which is needed to populate the rule description.
	 * 
	 * @return
	 */
	private ISelectionChangedListener createSelectionChangedListener() {
		return new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();

				if (sel.size() == 1) {
					@SuppressWarnings("unchecked")
					RefactoringRule<? extends ASTVisitor> newSelection = (RefactoringRule<? extends ASTVisitor>) sel
							.getFirstElement();
					if (!newSelection.equals(selectedRefactoringRule)) {
						selectedRefactoringRule = newSelection;
						populateDescriptionTextViewer();
					}
				}
			}
		};
	}

	@SuppressWarnings("unchecked")
	protected List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getSelectedRules() {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = new ArrayList<>();
		Arrays.asList(rulesCheckboxTableViewer.getCheckedElements())
				.forEach(rule -> rules.add((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) rule));
		return rules;
	}
}
