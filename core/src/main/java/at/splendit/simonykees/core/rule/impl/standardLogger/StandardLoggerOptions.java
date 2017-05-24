package at.splendit.simonykees.core.rule.impl.standardLogger;

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
	 * {@link Throwable#printStackTrace()} method together with their log level.
	 * 
	 * @return a map of replacement options.
	 */
	Map<String, Integer> getPrintStackTraceReplaceOptions();

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
