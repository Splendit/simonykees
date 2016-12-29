package at.splendit.simonykees.core.i18n;

import org.eclipse.osgi.util.NLS;

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
	public static String HelpMessageDialog_default_message;
	public static String HelpMessageDialog_homepage_url;
	public static String MultiCatchRule_description;
	public static String MultiCatchRule_name;
	public static String FunctionalInterfaceRule_description;
	public static String FunctionalInterfaceRule_name;
	public static String LicenseManager_cannot_reach_licensing_provider_on_checkin;
	public static String LicenseManager_cannot_reach_licensing_provider_on_prevalidation;
	public static String LicenseManager_cannot_read_hardware_information;
	public static String LicenseManager_session_check_in;
	public static String LicenseManager_updating_licensee_credentials;
	public static String LicenseManager_wait_for_validation_was_interrupted;
	public static String LicenseValidator_cannot_reach_license_provider_on_validation_call;
	public static String LicenseValidator_received_validation_response;
	public static String OrganiseImportsRule_description;
	public static String OrganiseImportsRule_name;
	public static String Profile_DefaultProfile_profileName;
	public static String Profile_Java8Profile_profileName;
	public static String RefactoringRule_default_description;
	public static String RefactoringRule_default_name;
	public static String RefactoringRule_warning_workingcopy_already_present;
	public static String SelectRulesPage_description;
	public static String SelectRulesPage_page_name;
	public static String SelectRulesPage_rule_description_default_text;
	public static String SelectRulesPage_select_unselect_all;
	public static String SelectRulesPage_title;
	public static String SelectRulesWizard_title;
	public static String SelectRulesWizard_warning_no_refactorings;
	public static String SimonykeesMessageDialog_bugreport_email;
	public static String SimonykeesMessageDialog_default_error_message;
	public static String SimonykeesPreferencePage_selectProfile;
	public static String StringUtilsRule_description;
	public static String StringUtilsRule_name;
	public static String TryWithResourceRule_description;
	public static String TryWithResourceRule_name;
	public static String ValidateExecutor_shutting_down_validation_scheduler;
	public static String ValidateExecutor_validation_scheduler_started;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
