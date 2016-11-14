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
import org.eclipse.swt.widgets.Composite;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

public class SelectRulesPage extends AbstractWizardPage {

	private CheckboxTableViewer rulesCheckboxTableViewer;
	private StyledText descriptionStyledText;
	private RefactoringRule<? extends ASTVisitor> selectedRefactoringRule;

	protected SelectRulesPage() {
		super(Messages.SelectRulesPage_page_name);
		setTitle(Messages.SelectRulesPage_title);
		setDescription(Messages.SelectRulesPage_description);
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setControl(parent);
		GridLayout layout = new GridLayout();

		parent.setLayout(layout);

		createSelectAllButton(parent);

		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createRulesCheckboxTableViewer(sashForm);
		createRuleDescriptionViewer(sashForm);

	}

	private void createSelectAllButton(Composite parent) {
		Button selectAllButton = new Button(parent, SWT.CHECK);
		selectAllButton.setText(Messages.SelectRulesPage_select_unselect_all);
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
			descriptionStyledText.setText(Messages.SelectRulesPage_rule_description_default_text);
		} else {
			descriptionStyledText.setText(selectedRefactoringRule.getDescription());
		}
	}

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
