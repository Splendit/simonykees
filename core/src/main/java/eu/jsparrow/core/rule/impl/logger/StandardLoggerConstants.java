package eu.jsparrow.core.rule.impl.logger;

/**
 * Constants for the {@link StandardLoggerRule}
 * 
 * @author Ludwig Werzowa
 * @since 1.2
 */
public final class StandardLoggerConstants {

	// option to set specific logger for testing
	public static final String LOGGER_QUALIFIED_NAME = "loggerQualifiedName"; //$NON-NLS-1$

	public static final String SLF4J_LOGGER = "org.slf4j.Logger"; //$NON-NLS-1$
	public static final String LOG4J_LOGGER = "org.apache.logging.log4j.Logger"; //$NON-NLS-1$

	public static final String SYSTEM_OUT_PRINT_KEY = "system-out-print"; //$NON-NLS-1$
	public static final String SYSTEM_ERR_PRINT_KEY = "system-err-print"; //$NON-NLS-1$
	public static final String PRINT_STACKTRACE_KEY = "print-stacktrace"; //$NON-NLS-1$
	public static final String SYSTEM_OUT_PRINT_EXCEPTION_KEY = "system-out-print-exception"; //$NON-NLS-1$
	public static final String SYSTEM_ERR_PRINT_EXCEPTION_KEY = "system-err-print-exception"; //$NON-NLS-1$
	public static final String NEW_LOGGING_STATEMENT_KEY = "new-logging-statement"; //$NON-NLS-1$

	private StandardLoggerConstants() {
		// we don't want instances of that class
	}

}
