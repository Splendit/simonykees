package at.splendit.simonykees.core.ui.wizard.semiautomatic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.splendit.simonykees.core.ui.wizard.IValueChangeListener;

public class RenameFieldsRuleWizardPageModel {

	Set<IValueChangeListener> listeners = new HashSet<>();
	
	private List<String> fieldTypes;
	private String searchScope;
	private String underscoreReplacementOption;
	private String dollarReplacementOption;
	private boolean addTodoComments;

	public RenameFieldsRuleWizardPageModel() {
	}

	/**
	 * Adds listener to model which notifies view to refresh data when ever
	 * something in model changes
	 */
	public void addListener(IValueChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Called from every method in model that changes anything in model.
	 * Notifies view to redraw all elements with new data.
	 */
	public void notifyListeners() {
		for (IValueChangeListener listener : listeners) {
			listener.valueChanged();
		}
	}

	// GETTERS useb by Wizard page
	public List<String> getFieldTypeOptions() {
		List<String> fieldTypes = new ArrayList<>();
		fieldTypes.add("private fields");
		fieldTypes.add("public fields");
		return fieldTypes;
	}
	
	public List<String> getSearchScopeOptions() {
		List<String> searchScopes = new ArrayList<>();
		searchScopes.add("Project (References will be searched for in the enclosing project.)");
		searchScopes.add("Workspace (Search for references in the whole workspace. This is slower.)");
		return searchScopes;
	}
	
	public List<String> getUnderscoreReplacementOptions() {
		List<String> underscoreReplacements = new ArrayList<>();
		underscoreReplacements.add("Upper case (example \"variable_name\" to \"variableName\")");
		underscoreReplacements.add("Leave case as is (example \"variable_name\" to \"variablename\")");
		return underscoreReplacements;
	}
	
	public List<String> getDolarSignReplacementOptions() {
		List<String> dolarSignReplacements = new ArrayList<>();
		dolarSignReplacements.add("Upper case (example \"variable$name\" to \"variableName\")");
		dolarSignReplacements.add("Leave case as is (example \"variable$name\" to \"variablename\")");
		return dolarSignReplacements;
	}

	public String getTodoOption() {
		return "Add TODO comments";
	}

	// SETTERS used by controler
	public void setFieldType(List<String> selection) {
		this.fieldTypes = selection;
		notifyListeners();
	}

	public void setSearchScope(String newValue) {
		this.searchScope = newValue;
		notifyListeners();
	}

	public void setUnderscoreReplacementOption(String newValue) {
		this.underscoreReplacementOption = newValue;
		notifyListeners();
	}

	public void setDollarReplacementOption(String newValue) {
		this.dollarReplacementOption = newValue;
		notifyListeners();
	}

	public void setTodoOption(boolean addTodo) {
		this.addTodoComments = addTodo;
		notifyListeners();
	}

	//GETTERS used by rule
	public List<String> getFieldTypes() {
		return fieldTypes;
	}
	
	public String getSearchScope() {
		return searchScope;
	}

	public String getUnderscoreReplacementOption() {
		return underscoreReplacementOption;
	}

	public String getDollarReplacementOption() {
		return dollarReplacementOption;
	}

	public boolean isAddTodoComments() {
		return addTodoComments;
	}
	
	
}
