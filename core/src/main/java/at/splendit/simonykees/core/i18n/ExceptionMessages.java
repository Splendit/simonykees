package at.splendit.simonykees.core.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter
 * @since 0.9
 * 
 */
public class ExceptionMessages extends NLS {
	private static final String BUNDLE_NAME = "at.splendit.simonykees.core.i18n.exceptionMessages"; //$NON-NLS-1$
	public static String AbstractCompilationUnitAstVisitor_compilation_unit_no_context;
	public static String AbstractRefactorer_error_cannot_init_rule;
	public static String AbstractRefactorer_java_element_resoltuion_failed;
	public static String AbstractRefactorer_reconcile_failed;
	public static String AbstractRefactorer_rule_execute_failed;
	public static String AbstractRefactorer_user_java_element_resoltuion_failed;
	public static String AbstractRefactorer_user_reconcile_failed;
	public static String AbstractRefactorer_user_rule_execute_failed;
	public static String AbstractRefactorer_user_warn_no_compilation_units_found;
	public static String AbstractRefactorer_warn_no_compilation_units_found;
	public static String AbstractRefactorer_warn_no_working_copies_foung;
	public static String AbstractRefactorer_warn_working_copies_already_generated;
	public static String SimonykeesException_default_ui_message;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ExceptionMessages.class);
	}

	private ExceptionMessages() {
	}
}
