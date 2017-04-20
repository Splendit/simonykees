package at.splendit.simonykees.core.ui.wizard.impl;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Controler for manipulating with data required for
 * {@link SelectRulesWizardPage}
 * 
 * @author Andreja Sambolec
 * @since 1.3
 */
public class SelectRulesWizardPageControler {

	private SelectRulesWizardPageModel model;

	public SelectRulesWizardPageControler(SelectRulesWizardPageModel model) {
		this.model = model;
	}

	public void addButtonClicked(IStructuredSelection selection) {
		model.moveToRight(selection);
	}

	public void addAllButtonClicked() {
		model.moveAllToRight();
	}

	public void removeButtonClicked(IStructuredSelection selection) {
		model.moveToLeft(selection);
	}

	public void removeAllButtonClicked() {
		model.moveAllToLeft();
	}

	public void nameFilterTextChanged(String text) {
		model.setNameFilter(text);
	}

}
