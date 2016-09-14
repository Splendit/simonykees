package at.splendit.simonykees.core.i18n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "at.splendit.simonykees.core.i18n.messages"; //$NON-NLS-1$
	public static String aa_codename;
	public static String ui_ok;
	public static String ui_cancel;
	public static String AbstractRefactorer_error_cannot_init_rule;
	public static String AbstractRefactorer_warn_no_compilation_units_found;
	public static String AbstractRefactorer_warn_no_working_copies_foung;
	public static String AbstractRefactorer_warn_working_copies_already_generated;
	public static String AbstractSimonykeesHandler_error_activePartId_unknown;
	public static String AbstractSimonykeesHandler_error_unexpected_object_editor;
	public static String AbstractSimonykeesHandler_error_unexpected_object_explorer;
	public static String ArithmethicAssignmentRule_description;
	public static String ArithmethicAssignmentRule_name;
	public static String RefactoringRule_default_description;
	public static String RefactoringRule_default_name;
	public static String RefactoringRule_warning_workingcopy_already_present;
	public static String SelectRulesPage_description;
	public static String SelectRulesPage_page_name;
	public static String SelectRulesPage_title;
	public static String SelectRulesWizard_title;
	public static String SelectRulesWizard_warning_no_refactorings;
	public static String StringUtilsRule_description;
	public static String StringUtilsRule_name;
	public static String TryWithResourceRule_description;
	public static String TryWithResourceRule_name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
