package at.splendit.simonykees.core.i18n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "at.splendit.simonykees.core.i18n.messages"; //$NON-NLS-1$
	public static String ArithmethicAssignmentRule_description;
	public static String ArithmethicAssignmentRule_name;
	public static String RefactoringRule_default_description;
	public static String RefactoringRule_default_name;
	public static String RefactoringRule_warning_workingcopy_already_present;
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
