package eu.jsparrow.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter
 * @since 0.9
 */
public class ExceptionMessages extends NLS {
	private static final String BUNDLE_NAME = "eu.jsparrow.i18n.exceptionMessages"; //$NON-NLS-1$
	public static String AbstractCompilationUnitAstVisitor_compilation_unit_no_context;
	public static String RefactoringPipeline_java_element_resolution_failed;
	public static String RefactoringPipeline_reconcile_failed;
	public static String RefactoringPipeline_rule_execute_failed;
	public static String RefactoringState_unable_to_discard_working_copy;
	public static String RefactoringState_unable_to_reset_working_copy;
	public static String RefactoringPipeline_syntax_errors_exist;
	public static String RefactoringPipeline_user_java_element_resolution_failed;
	public static String RefactoringPipeline_user_reconcile_failed;
	public static String RefactoringPipeline_user_rule_execute_failed;
	public static String RefactoringPipeline_user_warn_changes_already_generated;
	public static String RefactoringPipeline_user_warn_no_compilation_units_found;
	public static String RefactoringPipeline_user_warn_no_java_files_found_to_apply_rules;
	public static String RefactoringPipeline_warn_no_compilation_units_found;
	public static String RefactoringPipeline_warn_no_working_copies_found;
	public static String RefactoringPipeline_warn_working_copies_already_generated;
	public static String RefactoringState_no_changes_found;
	public static String RefactoringState_warning_workingcopy_already_present;
	public static String LicenseUtil_error_moreInformation;
	public static String LicenseUtil_license_service_unavailable;
	public static String PersistenceManager_decryption_error;
	public static String PersistenceManager_encryption_error;
	public static String SimonykeesException_default_ui_message;
	public static String SimonykeesPreferencePageLicense_license_service_unavailable;
	public static String SimonykeesUpdateLicenseDialog_license_service_unavailable;
	public static String TableLabelProvider_not_supported;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ExceptionMessages.class);
	}

	private ExceptionMessages() {
	}
}
