package at.splendit.simonykees.core.ui.wizard.impl;

public class SelectRulesWizardPageControler extends AbstractSelectRulesWizardControler {

	SelectRulesWizardPageModel model;
	public SelectRulesWizardPageControler(SelectRulesWizardPageModel model) {
		super(model);
		this.model = model;
	}

	public void nameFilterTextChanged(String text) {
		model.setNameFilter(text);
	}

}
