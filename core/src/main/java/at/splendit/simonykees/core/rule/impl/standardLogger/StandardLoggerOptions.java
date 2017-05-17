package at.splendit.simonykees.core.rule.impl.standardLogger;

import java.util.Map;

import org.eclipse.jdt.core.IType;

/**
 * A type for representing the information that the {@link StandardLoggerRule} should
 * provide to its clients. 
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public interface StandardLoggerOptions {
	
	String SLF4J_LOGGER = "org.slf4j.Logger"; //$NON-NLS-1$
	String LOGBACK_LOGGER = "ch.qos.logback.classic.Logger"; //$NON-NLS-1$
	String LOG4J_LOGGER = "org.apache.logging.log4j.Logger"; //$NON-NLS-1$
	
	String SYSTEM_OUT_PRINT = "System.out.print"; //$NON-NLS-1$
	String SYSTEM_ERR_PRINT = "System.err.print"; //$NON-NLS-1$
	String PRINT_STACKTRACE = "printStackTrace"; //$NON-NLS-1$

	/**
	 * Returns the available options for replacing the {@link System.out#print()} method
	 * together with their log level. 
	 * 
 	 * @return a map of replacement options.
	 */
	Map<String, Integer> getSystemOutReplaceOptions();
	
	/**
	 * Returns the available options for replacing the {@link System.err#print()} method
	 * together with their log level. 
	 * 
 	 * @return a map of replacement options.
	 */
	Map<String, Integer> getSystemErrReplaceOptions();
	
	/**
	 * Returns the available options for replacing the {@link Throwable#printStackTrace()} method
	 * together with their log level. 
	 * 
 	 * @return a map of replacement options.
	 */
	Map<String, Integer> getPrintStackTraceReplaceOptions();
	
	/**
	 * Returns the default replacement options for: 
	 * <ul>
	 * <li> {@link System.out#print()} </li>
	 * <li> {@link System.err#print()} </li>
	 * <li> {@link Throwable#printStackTrace()} </li>
	 * </ul>
	 * 
	 * @return a map with the default replacement options. 
	 */
	Map<String, String> getDefaultOptions();
	
	/**
	 * Provides the logger type which is available in the classpath. 
	 * 
	 * @return an instance of {@link SupportedLogger} if one one of the supported loggers
	 * is in the classpat or {@code null} otherwise.
	 */
	SupportedLogger getAvailableLoggerType();
	
	/**
	 * Provides the {@link IType} of the supported logger available in the classpath. 
	 * 
	 * @return an {@link IType} or {@code null} if there is no supported logger
	 * in the classpath.
	 */
	IType getLoggerType();
}
