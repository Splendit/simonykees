package eu.jsparrow.ui.wizard.semiautomatic;

import java.util.List;

public class RemoveUnusedCodeWizardPageController {
	
	private RemoveUnusedCodeWizardPageModel model;
	
	public RemoveUnusedCodeWizardPageController(RemoveUnusedCodeWizardPageModel model) {
		this.model = model;
	}
				
	public void classMemberSelectionChanged(List<String> selection) {
		model.setClassMemberTypes(selection);
	}
	
	public void searchScopeSelectionChanged(String newValue) {
		model.setSearchScope(newValue);
	}
	
	public void removeTestCodeSelectionChanged(boolean newValue) {
		model.setRemoveTestCode(newValue);
	}
	
	public void removeInitializersWithSideEffectsSelectionChanged(boolean newValue) {
		model.setRemoveInitializersWithSideEffects(newValue);
	}
}
