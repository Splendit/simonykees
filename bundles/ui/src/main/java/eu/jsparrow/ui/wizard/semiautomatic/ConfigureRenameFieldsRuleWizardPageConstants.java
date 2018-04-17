package eu.jsparrow.ui.wizard.semiautomatic;

import eu.jsparrow.i18n.Messages;

/**
 * Wizard page constants for configuring renaming rule when applying to selected
 * resources
 * 
 * @author Andreja Sambolec
 * @since 2.3.0
 *
 */
public class ConfigureRenameFieldsRuleWizardPageConstants {

	private ConfigureRenameFieldsRuleWizardPageConstants() {
	}

	// Constants
	public static final String SCOPE_PROJECT = Messages.RenameFieldsRuleWizardPageModel_scopeOption_project;
	public static final String SCOPE_WORKSPACE = Messages.RenameFieldsRuleWizardPageModel_scopeOption_workspace;
	public static final String UNDERSCORE_UPPER = Messages.RenameFieldsRuleWizardPageModel_underscoreReplacementOption_upperCase;
	public static final String UNDERSCORE_SAME = Messages.RenameFieldsRuleWizardPageModel_underscoreReplacementOption_leaveAsIs;
	public static final String DOLLAR_UPPER = Messages.RenameFieldsRuleWizardPageModel_dollarSignReplacementOption_upperCase;
	public static final String DOLLAR_SAME = Messages.RenameFieldsRuleWizardPageModel_dollarSignReplacementOption_leaveAsIs;

	public static final String TYPE_PRIVATE = Messages.RenameFieldsRuleWizardPageModel_typeOption_privateFields;
	public static final String TYPE_PROTECTED = Messages.RenameFieldsRuleWizardPageModel_typeOption_protectedFields;
	public static final String TYPE_PACKAGEPRIVATE = Messages.RenameFieldsRuleWizardPageModel_typeOption_packagePrivateFields;
	public static final String TYPE_PUBLIC = Messages.RenameFieldsRuleWizardPageModel_typeOption_publicFields;

}
