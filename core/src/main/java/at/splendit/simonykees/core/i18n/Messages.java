package at.splendit.simonykees.core.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa
 * @since 0.9
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "at.splendit.simonykees.core.i18n.messages"; //$NON-NLS-1$
	public static String aa_codename;
	public static String ui_ok;
	public static String ui_cancel;
	public static String AbstractSimonykeesHandler_error_activePartId_unknown;
	public static String AbstractSimonykeesHandler_error_unexpected_object_editor;
	public static String AbstractSimonykeesHandler_error_unexpected_object_explorer;
	public static String ArithmethicAssignmentRule_description;
	public static String ArithmethicAssignmentRule_name;
	public static String BracketsToControlRule_description;
	public static String BracketsToControlRule_name;
	public static String ChangePreviewWizard_ChangePreview;
	public static String ChangePreviewWizard_Wizard_Page_Description;
	public static String ChangePreviewWizard_WizardPage;
	public static String ChangePreviewWizard_WizardPageTitle;
	public static String CodeFormatterRule_description;
	public static String CodeFormatterRule_name;
	public static String CollectionRemoveAllRule_description;
	public static String CollectionRemoveAllRule_name;
	public static String HelpMessageDialog_default_message;
	public static String HelpMessageDialog_homepage_url;
	public static String MultiCatchRule_description;
	public static String MultiCatchRule_name;
	public static String ForToForEachRule_description;
	public static String ForToForEachRule_name;
	public static String FunctionalInterfaceRule_description;
	public static String FunctionalInterfaceRule_name;
	public static String OrganiseImportsRule_description;
	public static String OrganiseImportsRule_name;
	public static String Profile_DefaultProfile_profileName;
	public static String Profile_Java8Profile_profileName;
	public static String RefactoringRule_default_description;
	public static String RefactoringRule_default_name;
	public static String RefactoringRule_warning_workingcopy_already_present;
	public static String RemoveNewStringConstructorRule_description;
	public static String RemoveNewStringConstructorRule_name;
	public static String SelectRulesWizardPage_description;
	public static String SelectRulesWizardPage_page_name;
	public static String SelectRulesWizardPage_rule_description_default_text;
	public static String SelectRulesWizardPage_select_unselect_all;
	public static String SelectRulesWizardPage_title;
	public static String SelectRulesWizard_title;
	public static String SelectRulesWizard_warning_no_refactorings;
	public static String SimonykeesMessageDialog_bugreport_email;
	public static String SimonykeesMessageDialog_default_error_message;
	public static String SimonykeesPreferenceManager_builtIn;
	public static String SimonykeesPreferencePage_rules;
	public static String SimonykeesPreferencePage_selectProfile;
	public static String StringFormatLineSeperatorRule_description;
	public static String StringFormatLineSeperatorRule_name;
	public static String StringUtilsRule_description;
	public static String StringUtilsRule_name;
	public static String TryWithResourceRule_description;
	public static String TryWithResourceRule_name;
	public static String WhileToForRule_description;
	public static String WhileToForRule_name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
