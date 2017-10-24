package at.splendit.simonykees.core.ui.wizard.semiautomatic;

import java.util.List;

public class ConfigureRenameFieldsRuleWizardPageControler {

	private ConfigureRenameFieldsRuleWizardPageModel model;
	
	public ConfigureRenameFieldsRuleWizardPageControler(ConfigureRenameFieldsRuleWizardPageModel model) {
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
