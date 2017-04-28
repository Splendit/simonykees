package at.splendit.simonykees.core.ui.preference;

import org.eclipse.core.runtime.IStatus;

import at.splendit.simonykees.core.ui.wizard.impl.AbstractSelectRulesWizardControler;

public class ConfigureProfileSelectRulesWizardPageControler extends AbstractSelectRulesWizardControler {

	ConfigureProfileSelectRulesWIzardPageModel model;
	
	public ConfigureProfileSelectRulesWizardPageControler(ConfigureProfileSelectRulesWIzardPageModel model) {
		super(model);
		this.model = model;
	}

	public IStatus nameTextChanged(String text) {
		return model.setName(text);
	}
	
}
