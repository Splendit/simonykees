package at.splendit.simonykees.core.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;

public class SelectRulesPage extends WizardPage {

	private CheckboxTableViewer rulesCheckboxTableViewer;
	private StyledText descriptionStyledText;
	private RefactoringRule<? extends ASTVisitor> selectedRefactoringRule;

	private final Map<Object, Boolean> checkedRules = new HashMap<>();

	protected SelectRulesPage() {
		super(Messages.SelectRulesPage_page_name);
		setTitle(Messages.SelectRulesPage_title);
		setDescription(Messages.SelectRulesPage_description);
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setControl(parent);

		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);

		createRulesCheckboxTableViewer(sashForm);
		createRuleDescriptionViewer(sashForm);
	}

	private void createRulesCheckboxTableViewer(Composite parent) {
		List<RefactoringRule<? extends ASTVisitor>> rules = RulesContainer.getAllRules();

		rulesCheckboxTableViewer = CheckboxTableViewer.newCheckList(parent, SWT.CHECK | SWT.BORDER);
		rulesCheckboxTableViewer.setContentProvider(new ArrayContentProvider());
		rulesCheckboxTableViewer.addSelectionChangedListener(createSelectionChangedListener());
		rulesCheckboxTableViewer.setInput(rules);

		// FIXME check if this is needed
		// rulesCheckboxTableViewer.setCheckStateProvider(new
		// CheckStateProvider(rules));

		// set label text
		rulesCheckboxTableViewer.setLabelProvider(new StyledCellLabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public void update(ViewerCell cell) {
				cell.setText(((RefactoringRule<? extends ASTVisitor>) cell.getElement()).getName());
			}
		});
	}

	private void createRuleDescriptionViewer(Composite parent) {

		/*
		 *  There is a known issue with automatically showing and hiding scrollbars and SWT.WRAP.
		 *  Using StyledText and setAlwaysShowScrollBars(false) makes the vertical scroll work correctly at least. 
		 */
		descriptionStyledText = new StyledText(parent, SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		descriptionStyledText.setAlwaysShowScrollBars(false);
		
		descriptionStyledText.setEditable(false);

		populateDescriptionTextViewer();
	}

	private void populateDescriptionTextViewer() {
		if (selectedRefactoringRule != null) {
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
	protected List<RefactoringRule<? extends ASTVisitor>> getSelectedRules() {
		List<RefactoringRule<? extends ASTVisitor>> rules = new ArrayList<>();
		Arrays.asList(rulesCheckboxTableViewer.getCheckedElements())
				.forEach(rule -> rules.add((RefactoringRule<? extends ASTVisitor>) rule));
		;
		return rules;
	}

	private final class CheckStateProvider implements ICheckStateProvider {

		protected CheckStateProvider(List<? extends Object> rules) {
			rules.forEach(rule -> checkedRules.put(rule, Boolean.FALSE));
		}

		@Override
		public boolean isChecked(Object element) {
			return Boolean.TRUE.equals(checkedRules.get(element));
		}

		@Override
		public boolean isGrayed(Object element) {
			return false;
		}

	}

}
