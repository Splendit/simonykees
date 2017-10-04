package eu.jsparrow.core.rule.impl.logger;

import java.util.Map;

/**
 * A type for representing the information that the {@link StandardLoggerRule}
 * should provide to its clients.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public interface StandardLoggerOptions {

	/**
	 * Returns the available options for replacing the
	 * {@link System.out#print()} method together with their log level.
	 * 
	 * @return a map of replacement options.
	 */
	Map<String, Integer> getSystemOutReplaceOptions();

	/**
	 * Returns the available options for replacing the
	 * {@link System.err#print()} method together with their log level.
	 * 
	 * @return a map of replacement options.
	 */
	Map<String, Integer> getSystemErrReplaceOptions();
	
	/**
	 * Returns the available options for replacing the
	 * {@link System.out#print()} method used for logging an exception together
	 * with their log level.
	 * 
	 * @return a map of replacement options.
	 */
	Map<String, Integer> getSystemOutPrintExceptionReplaceOptions();
	
	/**
	 * Returns the available options for replacing the
	 * {@link System.err#print()} method used for logging an exception together
	 * with their log level.
	 * 
	 * @return a map of replacement options.
	 */
	Map<String, Integer> getSystemErrPrintExceptionReplaceOptions();

	/**
	 * Returns the available options for replacing the
	 * {@link Throwable#printStackTrace()} method together with their log level.
	 * 
	 * @return a map of replacement options.
	 */
	Map<String, Integer> getPrintStackTraceReplaceOptions();
	
	/**
	 * Returns the available options for inserting a new logging statement in
	 * the catch clauses which ignore the exception together with their log
	 * level.
	 * 
	 * @return a map of replacement options.
	 */
	Map<String, Integer> getNewLoggingStatementOptions();

	/**
	 * Returns the default replacement options for:
	 * <ul>
	 * <li>{@link System.out#print()}</li>
	 * <li>{@link System.err#print()}</li>
	 * <li>{@link Throwable#printStackTrace()}</li>
	 * </ul>
	 * 
	 * @return a map with the default replacement options.
	 */
	Map<String, String> getDefaultOptions();

	/**
	 * Provides the logger type which is available in the classpath.
	 * 
	 * @return an instance of {@link SupportedLogger} if one one of the
	 *         supported loggers is in the classpat or {@code null} otherwise.
	 */
	SupportedLogger getAvailableLoggerType();
}
