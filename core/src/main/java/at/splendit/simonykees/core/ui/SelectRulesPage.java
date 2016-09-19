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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;

public class SelectRulesPage extends WizardPage {
	
	private CheckboxTableViewer rulesCheckboxTableViewer;
	private Text descriptionText;
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
		
		GridLayout layout = new GridLayout(2, false);
//		layout.numColumns = 2;
//		GridData gridData = new GridData(GridData.FILL_BOTH);
		
		parent.setLayout(layout);
//		parent.setLayoutData(gridData);
		
		createRulesCheckboxTableViewer(parent);
		createRuleDescriptionViewer(parent);
		
		setControl(parent);
	}

	private void createRulesCheckboxTableViewer(Composite parent) {
		List<RefactoringRule<? extends ASTVisitor>> rules = RulesContainer.getAllRules();
		
		rulesCheckboxTableViewer = CheckboxTableViewer.newCheckList(parent, SWT.CHECK);
		rulesCheckboxTableViewer.setContentProvider(new ArrayContentProvider());
		rulesCheckboxTableViewer.addSelectionChangedListener(createSelectionChangedListener());
		rulesCheckboxTableViewer.setInput(rules);

		// FIXME check if this is needed
//		rulesCheckboxTableViewer.setCheckStateProvider(new CheckStateProvider(rules));
		
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
		selectedRefactoringRule = RulesContainer.getAllRules().stream().findFirst().get(); // de facto null safe
		
		descriptionText = new Text(parent, INFORMATION);
		
		populateDescriptionTextViewer();
	}
	
	private void populateDescriptionTextViewer() {
		descriptionText.setText(selectedRefactoringRule.getDescription());
	}
	
	private ISelectionChangedListener createSelectionChangedListener() {
		return new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();

				if (sel.size() == 1) {
					@SuppressWarnings("unchecked")
					RefactoringRule<? extends ASTVisitor> newSelection = (RefactoringRule<? extends ASTVisitor>) sel.getFirstElement();
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
		Arrays.asList(rulesCheckboxTableViewer.getCheckedElements()).forEach(rule -> rules.add((RefactoringRule<? extends ASTVisitor>) rule));;
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
