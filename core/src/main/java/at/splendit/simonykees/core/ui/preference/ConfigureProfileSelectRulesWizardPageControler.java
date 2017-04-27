package at.splendit.simonykees.core.ui.preference;

import at.splendit.simonykees.core.ui.wizard.impl.AbstractSelectRulesWizardControler;

public class ConfigureProfileSelectRulesWizardPageControler extends AbstractSelectRulesWizardControler {

	ConfigureProfileSelectRulesWIzardPageModel model;
	
	public ConfigureProfileSelectRulesWizardPageControler(ConfigureProfileSelectRulesWIzardPageModel model) {
		super(model);
		this.model = model;
	}

	public void nameTextChanged(String text) {
		model.setName(text);
	}
	
}
