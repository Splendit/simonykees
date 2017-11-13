package eu.jsparrow.ui.wizard.semiautomatic;

import java.util.List;

/**
 * Wizard page controller for configuring renaming rule when applying to
 * selected resources
 * 
 * @author Andreja Sambolec
 * @since 2.3.0
 *
 */
public class ConfigureRenameFieldsRuleWizardPageController {

	private ConfigureRenameFieldsRuleWizardPageModel model;

	public ConfigureRenameFieldsRuleWizardPageController(ConfigureRenameFieldsRuleWizardPageModel model) {
		this.model = model;
	}

	public void fieldTypeSelectionChanged(List<String> selection) {
		model.setFieldType(selection);
	}

	public void searchScopeSelectionChanged(String newValue) {
		model.setSearchScope(newValue);
	}

	public void underscoreReplacementSelectionChanged(String newValue) {
		model.setUnderscoreReplacementOption(newValue);
	}

	public void dollarReplacementSelectionChanged(String newValue) {
		model.setDollarReplacementOption(newValue);
	}

	public void todoSelectionChanged(boolean addTodo) {
		model.setTodoOption(addTodo);
	}
}
