package eu.jsparrow.core.ui.preference;

import org.eclipse.core.runtime.IStatus;

import eu.jsparrow.core.ui.wizard.impl.AbstractSelectRulesWizardControler;

/**
 * Controller for Wizard page for selecting rules when creating new profile in
 * preferences page
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
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
