package at.splendit.simonykees.ui.wizard.impl;

/**
 * Controller for Wizard page for selecting rules when applying rules to
 * selected resources
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
public class SelectRulesWizardPageControler extends AbstractSelectRulesWizardControler {

	SelectRulesWizardPageModel model;

	public SelectRulesWizardPageControler(SelectRulesWizardPageModel model) {
		super(model);
		this.model = model;
	}

	public void nameFilterTextChanged(String text) {
		model.setNameFilter(text);
	}

	public void searchPressed(String text) {
		model.addTag(text);
	}

	public void tagButtonPressed(String text) {
		model.removeTag(text);
	}

}
