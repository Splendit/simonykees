package at.splendit.simonykees.core.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import at.splendit.simonykees.core.visitor.RulesContainer;

public class SelectRulesPage extends NewTypeWizardPage {
	
	private CheckboxTableViewer rulesCheckboxTableViewer;
	
	private final Map<Object, Boolean> checkedRules = new HashMap<>();

	protected SelectRulesPage() {
		super(true, "Select Rules");
		setTitle("Select Rules");
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		
		createRulesCheckboxTableViewer(parent);
		
		setControl(parent);
	}

	private void createRulesCheckboxTableViewer(Composite parent) {
		List<Class<? extends ASTVisitor>> rules = RulesContainer.getAllRules();
		
		rulesCheckboxTableViewer = CheckboxTableViewer.newCheckList(parent, SWT.CHECK);
		rulesCheckboxTableViewer.setContentProvider(new ArrayContentProvider());
		rulesCheckboxTableViewer.setInput(rules);

		// FIXME check if this is needed
//		rulesCheckboxTableViewer.setCheckStateProvider(new CheckStateProvider(rules));
		
		// set label text
		rulesCheckboxTableViewer.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				// FIXME provide a useful name
				super.update(cell);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	protected List<Class<? extends ASTVisitor>> getSelectedRules() {
		List<Class<? extends ASTVisitor>> rules = new ArrayList<>();
		Arrays.asList(rulesCheckboxTableViewer.getCheckedElements()).forEach(rule -> rules.add((Class<? extends ASTVisitor>) rule));;
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
