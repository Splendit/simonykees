package eu.jsparrow.ui.wizard.semiautomatic;

import java.util.List;

public class RemoveDeadCodeWizardPageController {
	
	private RemoveDeadCodeWizardPageModel model;
	
	public RemoveDeadCodeWizardPageController(RemoveDeadCodeWizardPageModel model) {
		this.model = model;
	}
				
	public void classMemberSelectionChanged(List<String> selection) {
		model.setClasMemberTypes(selection);
	}
	
	public void searchScopeSelectionChanged(String newValue) {
		model.setSearchScope(newValue);
	}
	
	public void removeTestCodeSelectionChaged(String newValue) {
		model.setRemoveTestCode(newValue);
	}
	
	public void removeInitializersWithSideEffectsSelectionChanged(String newValue) {
		model.setRemoveInitializersWithSideEffects(newValue);
	}
}
