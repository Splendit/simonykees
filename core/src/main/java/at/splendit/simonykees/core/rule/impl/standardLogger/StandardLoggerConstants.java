package at.splendit.simonykees.core.rule.impl.standardLogger;

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
	
	public static final String SYSTEM_OUT_PRINT = "System.out.print"; //$NON-NLS-1$
	public static final String SYSTEM_ERR_PRINT = "System.err.print"; //$NON-NLS-1$
	public static final String PRINT_STACKTRACE = "printStackTrace"; //$NON-NLS-1$

	private StandardLoggerConstants() {
		// we don't want instances of that class
	}

}
