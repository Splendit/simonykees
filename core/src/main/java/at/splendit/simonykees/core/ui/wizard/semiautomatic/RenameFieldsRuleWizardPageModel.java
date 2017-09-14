package at.splendit.simonykees.core.ui.wizard.semiautomatic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.splendit.simonykees.core.ui.wizard.IValueChangeListener;
import at.splendit.simonykees.i18n.Messages;

public class RenameFieldsRuleWizardPageModel {

	Set<IValueChangeListener> listeners = new HashSet<>();
	
	private List<String> fieldTypes;
	private String searchScope;
	private String underscoreReplacementOption;
	private String dollarReplacementOption;
	private boolean addTodoComments;

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
		List<String> fieldTypesOptions = new ArrayList<>();
		fieldTypesOptions.add(Messages.RenameFieldsRuleWizardPageModel_typeOption_privateFields);
		fieldTypesOptions.add(Messages.RenameFieldsRuleWizardPageModel_typeOption_protectedFields);
		fieldTypesOptions.add(Messages.RenameFieldsRuleWizardPageModel_typeOption_packageProtectedFields);
		fieldTypesOptions.add(Messages.RenameFieldsRuleWizardPageModel_typeOption_publicFields);
		return fieldTypesOptions;
	}
	
	public List<String> getSearchScopeOptions() {
		List<String> searchScopes = new ArrayList<>();
		searchScopes.add(Messages.RenameFieldsRuleWizardPageModel_scopeOption_project);
		searchScopes.add(Messages.RenameFieldsRuleWizardPageModel_scopeOption_workspace);
		return searchScopes;
	}
	
	public List<String> getUnderscoreReplacementOptions() {
		List<String> underscoreReplacements = new ArrayList<>();
		underscoreReplacements.add(Messages.RenameFieldsRuleWizardPageModel_underscoreReplacementOption_upperCase);
		underscoreReplacements.add(Messages.RenameFieldsRuleWizardPageModel_underscoreReplacementOption_leaveAsIs);
		return underscoreReplacements;
	}
	
	public List<String> getDolarSignReplacementOptions() {
		List<String> dolarSignReplacements = new ArrayList<>();
		dolarSignReplacements.add(Messages.RenameFieldsRuleWizardPageModel_dollarSignReplacementOption_upperCase);
		dolarSignReplacements.add(Messages.RenameFieldsRuleWizardPageModel_dollarSignReplacementOption_leaveAsIs);
		return dolarSignReplacements;
	}

	public String getTodoOption() {
		return Messages.RenameFieldsRuleWizardPageModel_addTodoCommentsText;
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

	public boolean setUpperCaseForUnderscoreReplacementOption() {
		return Messages.RenameFieldsRuleWizardPageModel_underscoreReplacementOption_upperCase.equals(underscoreReplacementOption);
	}

	public boolean setUpperCaseForDollarReplacementOption() {
		return Messages.RenameFieldsRuleWizardPageModel_dollarSignReplacementOption_upperCase.equals(dollarReplacementOption);
	}

	public boolean isAddTodoComments() {
		return addTodoComments;
	}
	
	
}
