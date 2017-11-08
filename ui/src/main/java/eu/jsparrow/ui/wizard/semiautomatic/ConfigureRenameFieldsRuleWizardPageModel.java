package eu.jsparrow.ui.wizard.semiautomatic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.wizard.WizardPage;

import eu.jsparrow.core.rule.impl.PublicFieldsRenamingRule;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.wizard.IValueChangeListener;

/**
 * Wizard page model for configuring renaming rule when applying to selected
 * resources
 * 
 * @author Andreja Sambolec
 * @since 2.3.0
 *
 */
public class ConfigureRenameFieldsRuleWizardPageModel {

	Set<IValueChangeListener> listeners = new HashSet<>();

	private List<String> fieldTypes;
	private String searchScope;
	private String underscoreReplacementOption;
	private String dollarReplacementOption;
	private boolean addTodoComments;

	public ConfigureRenameFieldsRuleWizardPageModel() {
		// initialize defaults
		fieldTypes = getFieldTypeOptions();
		searchScope = ConfigureRenameFieldsRuleWizardPageConstants.SCOPE_PROJECT;
		underscoreReplacementOption = ConfigureRenameFieldsRuleWizardPageConstants.UNDERSCORE_UPPER;
		dollarReplacementOption = ConfigureRenameFieldsRuleWizardPageConstants.DOLLAR_UPPER;
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
	/**
	 * Getter for {@link List} containing possible field type options used by
	 * {@link WizardPage}.
	 * 
	 * @return List with options
	 */
	public List<String> getFieldTypeOptions() {
		List<String> fieldTypesOptions = new ArrayList<>();
		fieldTypesOptions.add(ConfigureRenameFieldsRuleWizardPageConstants.TYPE_PRIVATE);
		fieldTypesOptions.add(ConfigureRenameFieldsRuleWizardPageConstants.TYPE_PROTECTED);
		fieldTypesOptions.add(ConfigureRenameFieldsRuleWizardPageConstants.TYPE_PACKAGEPROTECTED);
		fieldTypesOptions.add(ConfigureRenameFieldsRuleWizardPageConstants.TYPE_PUBLIC);
		return fieldTypesOptions;
	}

	/**
	 * Getter for {@link List} containing possible search scope options used by
	 * {@link WizardPage}.
	 * 
	 * @return List with options
	 */
	public List<String> getSearchScopeOptions() {
		List<String> searchScopes = new ArrayList<>();
		searchScopes.add(ConfigureRenameFieldsRuleWizardPageConstants.SCOPE_PROJECT);
		searchScopes.add(ConfigureRenameFieldsRuleWizardPageConstants.SCOPE_WORKSPACE);
		return searchScopes;
	}

	/**
	 * Getter for {@link List} containing possible underscore replacement
	 * options used by {@link WizardPage}.
	 * 
	 * @return List with options
	 */
	public List<String> getUnderscoreReplacementOptions() {
		List<String> underscoreReplacements = new ArrayList<>();
		underscoreReplacements.add(ConfigureRenameFieldsRuleWizardPageConstants.UNDERSCORE_UPPER);
		underscoreReplacements.add(ConfigureRenameFieldsRuleWizardPageConstants.UNDERSCORE_SAME);
		return underscoreReplacements;
	}

	/**
	 * Getter for {@link List} containing possible dollar sign replacement
	 * options used by {@link WizardPage}.
	 * 
	 * @return List with options
	 */
	public List<String> getDollarSignReplacementOptions() {
		List<String> dollarSignReplacements = new ArrayList<>();
		dollarSignReplacements.add(ConfigureRenameFieldsRuleWizardPageConstants.DOLLAR_UPPER);
		dollarSignReplacements.add(ConfigureRenameFieldsRuleWizardPageConstants.DOLLAR_SAME);
		return dollarSignReplacements;
	}

	/**
	 * Getter for to-do replacement option used by {@link WizardPage}.
	 * 
	 * @return string option
	 */
	public String getTodoOption() {
		return Messages.RenameFieldsRuleWizardPageModel_addTodoCommentsText;
	}

	// SETTERS used by controler
	/**
	 * Setter used by {@link ConfigureRenameFieldsRuleWizardPageController} to
	 * set selected field type options.
	 * 
	 * @param selection
	 *            List with selected options
	 */
	public void setFieldType(List<String> selection) {
		this.fieldTypes = selection;
		notifyListeners();
	}

	/**
	 * Setter used by {@link ConfigureRenameFieldsRuleWizardPageController} to
	 * set selected search scope option.
	 * 
	 * @param newValue
	 *            String with selected option
	 */
	public void setSearchScope(String newValue) {
		this.searchScope = newValue;
		notifyListeners();
	}

	/**
	 * Setter used by {@link ConfigureRenameFieldsRuleWizardPageController} to
	 * set selected underscore replacement option.
	 * 
	 * @param newValue
	 *            String with selected option
	 */
	public void setUnderscoreReplacementOption(String newValue) {
		this.underscoreReplacementOption = newValue;
		notifyListeners();
	}

	/**
	 * Setter used by {@link ConfigureRenameFieldsRuleWizardPageController} to
	 * set selected dollar replacement option.
	 * 
	 * @param newValue
	 *            String with selected option
	 */
	public void setDollarReplacementOption(String newValue) {
		this.dollarReplacementOption = newValue;
		notifyListeners();
	}

	/**
	 * Setter used by {@link ConfigureRenameFieldsRuleWizardPageController} to
	 * set selected wanted to-do replacement.
	 * 
	 * @param addTodo
	 *            true if selected, false otherwise
	 */
	public void setTodoOption(boolean addTodo) {
		this.addTodoComments = addTodo;
		notifyListeners();
	}

	// GETTERS used by rule
	/**
	 * Getter used by {@link PublicFieldsRenamingRule} to get selected field
	 * types chosen by user for renaming.
	 * 
	 * @return List with field types chosen to be renamed
	 */
	public List<String> getFieldTypes() {
		return fieldTypes;
	}

	/**
	 * Getter used by {@link PublicFieldsRenamingRule} to get selected search
	 * scope chosen by user.
	 * 
	 * @return selected search scope
	 */
	public String getSearchScope() {
		return searchScope;
	}

	/**
	 * Getter used by {@link PublicFieldsRenamingRule} to get selected
	 * replacement option for underscore replacement chosen by user.
	 * 
	 * @return selected underscore replacement option
	 */
	public boolean setUpperCaseForUnderscoreReplacementOption() {
		return ConfigureRenameFieldsRuleWizardPageConstants.UNDERSCORE_UPPER.equals(underscoreReplacementOption);
	}

	/**
	 * Getter used by {@link PublicFieldsRenamingRule} to get selected
	 * replacement option for dollar sign replacement chosen by user.
	 * 
	 * @return selected dollar sign replacement option
	 */
	public boolean setUpperCaseForDollarReplacementOption() {
		return ConfigureRenameFieldsRuleWizardPageConstants.DOLLAR_UPPER.equals(dollarReplacementOption);
	}

	/**
	 * Getter used by {@link PublicFieldsRenamingRule} to get if user wants to
	 * add to-do comments where renaming wasn't able.
	 * 
	 * @return true if to-do comments are wanted, false otherwise
	 */
	public boolean isAddTodoComments() {
		return addTodoComments;
	}

}
