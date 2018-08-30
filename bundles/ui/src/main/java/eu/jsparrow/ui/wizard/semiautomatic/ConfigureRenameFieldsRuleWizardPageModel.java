package eu.jsparrow.ui.wizard.semiautomatic;

import static eu.jsparrow.ui.wizard.semiautomatic.ConfigureRenameFieldsRuleWizardPageConstants.DOLLAR_SAME;
import static eu.jsparrow.ui.wizard.semiautomatic.ConfigureRenameFieldsRuleWizardPageConstants.DOLLAR_UPPER;
import static eu.jsparrow.ui.wizard.semiautomatic.ConfigureRenameFieldsRuleWizardPageConstants.SCOPE_PROJECT;
import static eu.jsparrow.ui.wizard.semiautomatic.ConfigureRenameFieldsRuleWizardPageConstants.SCOPE_WORKSPACE;
import static eu.jsparrow.ui.wizard.semiautomatic.ConfigureRenameFieldsRuleWizardPageConstants.TYPE_PACKAGEPRIVATE;
import static eu.jsparrow.ui.wizard.semiautomatic.ConfigureRenameFieldsRuleWizardPageConstants.TYPE_PRIVATE;
import static eu.jsparrow.ui.wizard.semiautomatic.ConfigureRenameFieldsRuleWizardPageConstants.TYPE_PROTECTED;
import static eu.jsparrow.ui.wizard.semiautomatic.ConfigureRenameFieldsRuleWizardPageConstants.TYPE_PUBLIC;
import static eu.jsparrow.ui.wizard.semiautomatic.ConfigureRenameFieldsRuleWizardPageConstants.UNDERSCORE_SAME;
import static eu.jsparrow.ui.wizard.semiautomatic.ConfigureRenameFieldsRuleWizardPageConstants.UNDERSCORE_UPPER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.wizard.WizardPage;

import eu.jsparrow.core.rule.impl.FieldsRenamingRule;
import eu.jsparrow.core.visitor.renaming.FieldDeclarationOptionKeys;
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
		searchScope = SCOPE_PROJECT;
		underscoreReplacementOption = UNDERSCORE_UPPER;
		dollarReplacementOption = DOLLAR_UPPER;
		addTodoComments = false;
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
		listeners.forEach(IValueChangeListener::valueChanged);
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
		fieldTypesOptions.add(TYPE_PRIVATE);
		fieldTypesOptions.add(TYPE_PROTECTED);
		fieldTypesOptions.add(TYPE_PACKAGEPRIVATE);
		fieldTypesOptions.add(TYPE_PUBLIC);
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
		searchScopes.add(SCOPE_PROJECT);
		searchScopes.add(SCOPE_WORKSPACE);
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
		underscoreReplacements.add(UNDERSCORE_UPPER);
		underscoreReplacements.add(UNDERSCORE_SAME);
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
		dollarSignReplacements.add(DOLLAR_UPPER);
		dollarSignReplacements.add(DOLLAR_SAME);
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
	 * Getter used by {@link FieldsRenamingRule} to get selected field
	 * types chosen by user for renaming.
	 * 
	 * @return List with field types chosen to be renamed
	 */
	public List<String> getFieldTypes() {
		return fieldTypes;
	}

	/**
	 * Getter used by {@link FieldsRenamingRule} to get selected search
	 * scope chosen by user.
	 * 
	 * @return selected search scope
	 */
	public String getSearchScope() {
		return searchScope;
	}

	/**
	 * Getter used by {@link FieldsRenamingRule} to get selected
	 * replacement option for underscore replacement chosen by user.
	 * 
	 * @return selected underscore replacement option
	 */
	public boolean setUpperCaseForUnderscoreReplacementOption() {
		return UNDERSCORE_UPPER.equals(underscoreReplacementOption);
	}

	/**
	 * Getter used by {@link FieldsRenamingRule} to get selected
	 * replacement option for dollar sign replacement chosen by user.
	 * 
	 * @return selected dollar sign replacement option
	 */
	public boolean setUpperCaseForDollarReplacementOption() {
		return DOLLAR_UPPER.equals(dollarReplacementOption);
	}

	/**
	 * Getter used by {@link FieldsRenamingRule} to get if user wants to
	 * add to-do comments where renaming wasn't able.
	 * 
	 * @return true if to-do comments are wanted, false otherwise
	 */
	public boolean isAddTodoComments() {
		return addTodoComments;
	}

	public Map<String, Boolean> getOptionsMap() {
		
		List<String> fields = getFieldTypes();
		
		boolean renamePrivate = fields.contains(TYPE_PRIVATE);
		boolean renameProtected = fields.contains(TYPE_PROTECTED);
		boolean renamePackageProtected = fields.contains(TYPE_PACKAGEPRIVATE);
		boolean renamePublic = fields.contains(TYPE_PUBLIC);
		boolean uppercaseAfterUnderscore = setUpperCaseForUnderscoreReplacementOption();
		boolean uppercaseAfterDollar = setUpperCaseForDollarReplacementOption();
		boolean addTodos = isAddTodoComments();
		
		Map<String, Boolean> options = new HashMap<>();
		
		options.put(FieldDeclarationOptionKeys.RENAME_PRIVATE_FIELDS, renamePrivate);
		options.put(FieldDeclarationOptionKeys.RENAME_PROTECTED_FIELDS, renameProtected);
		options.put(FieldDeclarationOptionKeys.RENAME_PACKAGE_PROTECTED_FIELDS, renamePackageProtected);
		options.put(FieldDeclarationOptionKeys.RENAME_PUBLIC_FIELDS, renamePublic);
		options.put(FieldDeclarationOptionKeys.UPPER_CASE_FOLLOWING_UNDERSCORE, uppercaseAfterUnderscore);
		options.put(FieldDeclarationOptionKeys.UPPER_CASE_FOLLOWING_DOLLAR_SIGN, uppercaseAfterDollar);
		options.put(FieldDeclarationOptionKeys.ADD_COMMENT, addTodos);
		
		return options;
	}

}
