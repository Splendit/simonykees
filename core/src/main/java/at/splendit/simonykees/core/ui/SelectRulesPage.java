package at.splendit.simonykees.core.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import at.splendit.simonykees.core.visitor.RulesContainer;

public class SelectRulesPage extends NewTypeWizardPage {
	
	private CheckboxTableViewer rulesCheckboxTableViewer;

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
		rulesCheckboxTableViewer = CheckboxTableViewer.newCheckList(parent, SWT.CHECK);
		rulesCheckboxTableViewer.setContentProvider(new ArrayContentProvider());
		rulesCheckboxTableViewer.setInput(RulesContainer.getAllRules());
	}
	
	protected List<Class<? extends ASTVisitor>> getSelectedRules() {
		List<Class<? extends ASTVisitor>> rules = new ArrayList<>();
		return rules;
	}

}
