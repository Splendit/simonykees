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

	public RenameFieldsRuleWizardPageModel() {
		// initialize defaults
		fieldTypes = getFieldTypeOptions();
		searchScope = RenameFieldsRuleWizardPageConstants.SCOPE_PROJECT;
		underscoreReplacementOption = RenameFieldsRuleWizardPageConstants.UNDERSCORE_UPPER;
		dollarReplacementOption = RenameFieldsRuleWizardPageConstants.DOLLAR_UPPER;
		addTodoComments = true;
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
		List<String> fieldTypesOptions = new ArrayList<>();
		fieldTypesOptions.add(RenameFieldsRuleWizardPageConstants.TYPE_PRIVATE);
		fieldTypesOptions.add(RenameFieldsRuleWizardPageConstants.TYPE_PROTECTED);
		fieldTypesOptions.add(RenameFieldsRuleWizardPageConstants.TYPE_PACKAGEPROTECTED);
		fieldTypesOptions.add(RenameFieldsRuleWizardPageConstants.TYPE_PUBLIC);
		return fieldTypesOptions;
	}

	public List<String> getSearchScopeOptions() {
		List<String> searchScopes = new ArrayList<>();
		searchScopes.add(RenameFieldsRuleWizardPageConstants.SCOPE_PROJECT);
		searchScopes.add(RenameFieldsRuleWizardPageConstants.SCOPE_WORKSPACE);
		return searchScopes;
	}

	public List<String> getUnderscoreReplacementOptions() {
		List<String> underscoreReplacements = new ArrayList<>();
		underscoreReplacements.add(RenameFieldsRuleWizardPageConstants.UNDERSCORE_UPPER);
		underscoreReplacements.add(RenameFieldsRuleWizardPageConstants.UNDERSCORE_SAME);
		return underscoreReplacements;
	}

	public List<String> getDolarSignReplacementOptions() {
		List<String> dolarSignReplacements = new ArrayList<>();
		dolarSignReplacements.add(RenameFieldsRuleWizardPageConstants.DOLLAR_UPPER);
		dolarSignReplacements.add(RenameFieldsRuleWizardPageConstants.DOLLAR_SAME);
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

	// GETTERS used by rule
	public List<String> getFieldTypes() {
		return fieldTypes;
	}

	public String getSearchScope() {
		return searchScope;
	}

	public boolean setUpperCaseForUnderscoreReplacementOption() {
		return RenameFieldsRuleWizardPageConstants.UNDERSCORE_UPPER.equals(underscoreReplacementOption);
	}

	public boolean setUpperCaseForDollarReplacementOption() {
		return RenameFieldsRuleWizardPageConstants.DOLLAR_UPPER.equals(dollarReplacementOption);
	}

	public boolean isAddTodoComments() {
		return addTodoComments;
	}

}
