package eu.jsparrow.ui.wizard.impl;

import java.util.List;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.preference.profile.CustomProfile;

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

	public void selectCustomProfile(List<String> customProfileRuleIds) {
		model.setCustomProfile(new CustomProfile(customProfileRuleIds));
		model.selectFromProfile(Messages.SelectRulesWizardPage_CustomProfileLabel);
	}
}
