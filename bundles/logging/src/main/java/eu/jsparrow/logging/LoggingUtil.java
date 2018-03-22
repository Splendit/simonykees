package eu.jsparrow.logging;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.LogManager;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.osgi.framework.Bundle;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;

/**
 * Provides methods to configure the logging framework for testing and for
 * normal use.
 * <p>
 * Two different logging configurations get built from scratch for testing and
 * normal use respectively. See {@link LoggingUtil#getLogFilePath()} and
 * {@link LoggingUtil#getTestLogFilePath()} for the corresponding normal- and
 * test-configuration logging paths.
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public class LoggingUtil {

	private static final int ROLLING_POLICY_MIN_INDEX = 0;
	private static final int ROLLING_POLICY_MAX_INDEX = 5;
	private static final String TRIGGER_MAX_FILE_SIZE = "5MB"; //$NON-NLS-1$
	private static final String ROLLING_FILE_APPENDER_NAME = "eu.jsparrow.logging.rollingFile"; //$NON-NLS-1$
	private static final String JUL_ROLLING_FILE_APPENDER_NAME = "eu.jsparrow.logging.jul.rollingFile"; //$NON-NLS-1$
	private static final String ROOT_LOGGER_NAME = org.slf4j.Logger.ROOT_LOGGER_NAME;
	private static final String JUL_LOGGER_NAME = "jul"; //$NON-NLS-1$
	private static final String LOG_FILE_NAME = "jsparrow.log"; //$NON-NLS-1$
	private static final String JUL_LOG_FILE_NAME = "jsparrow.jul.log"; //$NON-NLS-1$
	private static Bundle bundle = null;
	private static boolean isLogbackConfigured = false;

	// sonar lint suggestion to hide the public default constructor
	private LoggingUtil() {

	}

	/**
	 * @see {@link #configureLoggerForTesting(boolean)}
	 * @return
	 * @throws JoranException
	 * @throws IOException
	 */
	public static boolean configureLoggerForTesting() throws JoranException, IOException {
		return configureLoggerForTesting(false);
	}

	/**
	 * Triggers the logging configuration for plug in tests
	 * 
	 * @param useDebugLogLevel
	 *            if true, the default log level of the root logger is set to
	 *            "DEBUG" instead of "INFO"
	 * @return true, if the configuration was successful, false otherwise
	 * @throws JoranException
	 *             from {@link #configureLogback(Bundle)}
	 * @throws IOException
	 *             from {@link #configureLogback(Bundle)}
	 */
	public static boolean configureLoggerForTesting(boolean useDebugLogLevel) throws JoranException, IOException {
		boolean returnValue = initLogger(getTestLogFilePath(LOG_FILE_NAME), getTestLogFilePath(JUL_LOG_FILE_NAME),
				useDebugLogLevel);
		/**
		 * ignoring logging from eu.jsparrow.core for automated testing.
		 */
		Logger logger = (Logger) LoggerFactory.getLogger("eu.jsparrow.core"); //$NON-NLS-1$
		logger.setLevel(ch.qos.logback.classic.Level.OFF);
		return returnValue;
	}

	/**
	 * @see {@link #configureLogger(boolean)}
	 * @return
	 * @throws JoranException
	 * @throws IOException
	 */
	public static boolean configureLogger() throws JoranException, IOException {
		return configureLogger(false);
	}

	/**
	 * Triggers the standard logging configuration
	 * 
	 * @param useDebugLogLevel
	 *            if true, the default log level of the root logger is set to
	 *            "DEBUG" instead of "INFO"
	 * @return true, if the configuration was successful, false otherwise
	 * @throws JoranException
	 *             from {@link #configureLogback(Bundle)}
	 * @throws IOException
	 *             from {@link #configureLogback(Bundle)}
	 */
	public static boolean configureLogger(boolean useDebugLogLevel) throws JoranException, IOException {
		return initLogger(getLogFilePath(LOG_FILE_NAME), getLogFilePath(JUL_LOG_FILE_NAME), useDebugLogLevel);
	}

	/**
	 * initialises the slf4j logger
	 * 
	 * @param mainLogFilePath
	 * @param julLogFilePath
	 * @return
	 * @throws JoranException
	 * @throws IOException
	 */
	private static boolean initLogger(String mainLogFilePath, String julLogFilePath, boolean useDebugLogLevel)
			throws JoranException, IOException {
		if (bundle != null) {
			configureLogback(bundle);
			if (useDebugLogLevel)
				setDebugLogLevel();
			removeAppenderFromRootLogger(ROLLING_FILE_APPENDER_NAME);
			removeAppenderFromLogger(JUL_ROLLING_FILE_APPENDER_NAME, JUL_LOGGER_NAME);
			configureRollingFileAppender(ROLLING_FILE_APPENDER_NAME, mainLogFilePath, ROOT_LOGGER_NAME);
			configureRollingFileAppender(JUL_ROLLING_FILE_APPENDER_NAME, julLogFilePath, JUL_LOGGER_NAME);
			return true;
		}
		return false;
	}

	/**
	 * Configures logback with the help of the logback-test.xml file (located in
	 * the project root) and {@link JoranConfigurator}.
	 * 
	 * @param bundle
	 *            current Bundle
	 * @throws JoranException
	 *             thrown by
	 *             {@link JoranConfigurator#doConfigure(java.io.InputStream)}
	 * @throws IOException
	 *             - if an I/O error occurs during openStream
	 */
	private static void configureLogback(Bundle bundle) throws JoranException, IOException {
		if (!isLogbackConfigured) {
			LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
			context.reset();

			JoranConfigurator jc = new JoranConfigurator();
			jc.setContext(context);

			// this assumes that the logback.xml file is in the root of the
			// bundle.
			URL logbackConfigFileUrl = FileLocator.find(bundle, new org.eclipse.core.runtime.Path("logback.xml"), //$NON-NLS-1$
					null);
			jc.doConfigure(logbackConfigFileUrl.openStream());

			configureJulToSlf4jBridge();

			isLogbackConfigured = true;
		}
	}

	private static void setDebugLogLevel() {
		if (isLogbackConfigured) {
			Logger rootLogger = (Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);
			rootLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
		}
	}

	/**
	 * Configures the java.util.logging to slf4j bridge
	 */
	private static void configureJulToSlf4jBridge() {
		LogManager.getLogManager()
			.reset();
		CustomSLF4JBridgeHandler.removeHandlersForRootLogger();
		CustomSLF4JBridgeHandler.install();
		java.util.logging.Logger.getLogger("global") //$NON-NLS-1$
			.setLevel(Level.FINEST);
	}

	/**
	 * Configures a rolling file appender where the roll over is done as soon as
	 * the file size exceeds the value specified in
	 * {@link #TRIGGER_MAX_FILE_SIZE}}}.
	 * 
	 * The minimum index is {@link #ROLLING_POLICY_MIN_INDEX}. The maximum index
	 * is {@link #ROLLING_POLICY_MAX_INDEX}. The oldest log file will be deleted
	 * as soon as the roll over takes place and there is no free index available
	 * anymore.
	 * 
	 * @param fileAppenderName
	 * @param filePath
	 */
	private static void configureRollingFileAppender(String fileAppenderName, String filePath, String loggerName) {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		PatternLayoutEncoder ple = new PatternLayoutEncoder();
		ple.setContext(loggerContext);
		ple.setPattern("%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"); //$NON-NLS-1$
		ple.start();

		RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
		rollingFileAppender.setContext(loggerContext);
		rollingFileAppender.setName(fileAppenderName);
		rollingFileAppender.setFile(filePath);
		rollingFileAppender.setEncoder(ple);

		FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
		rollingPolicy.setContext(loggerContext);
		rollingPolicy.setParent(rollingFileAppender);
		rollingPolicy.setFileNamePattern(getFileNameFromPath(filePath) + ".%i.log.gz"); //$NON-NLS-1$
		rollingPolicy.setMinIndex(ROLLING_POLICY_MIN_INDEX);
		rollingPolicy.setMaxIndex(ROLLING_POLICY_MAX_INDEX);
		rollingPolicy.start();

		SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<>();
		triggeringPolicy.setContext(loggerContext);
		triggeringPolicy.setMaxFileSize(FileSize.valueOf(TRIGGER_MAX_FILE_SIZE));
		triggeringPolicy.start();

		rollingFileAppender.setRollingPolicy(rollingPolicy);
		rollingFileAppender.setTriggeringPolicy(triggeringPolicy);
		rollingFileAppender.start();

		addAppenderToLogger(rollingFileAppender, loggerName);
	}

	/**
	 * Removes the appender with the given name from the root logger
	 * 
	 * @param appenderName
	 */
	private static void removeAppenderFromRootLogger(String appenderName) {
		removeAppenderFromLogger(appenderName, ROOT_LOGGER_NAME);
	}

	/**
	 * adds the given appender to the logger with the given name
	 * 
	 * @param appender
	 * @param loggerName
	 */
	private static void addAppenderToLogger(OutputStreamAppender<ILoggingEvent> appender, String loggerName) {
		Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
		logger.addAppender(appender);
	}

	/**
	 * removes the appender with the given name from the logger with the given
	 * name
	 * 
	 * @param appenderName
	 * @param loggerName
	 */
	private static void removeAppenderFromLogger(String appenderName, String loggerName) {
		Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
		logger.detachAppender(appenderName);
	}

	/**
	 * get path to log file in <eclipse-workspace>/.metadata/jSparrow.log
	 * 
	 * @return path to log file as string
	 */
	private static String getLogFilePath(String fileName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath logFilePath = workspace.getRoot()
			.getLocation()
			.append(".metadata") //$NON-NLS-1$
			.append(fileName);
		return logFilePath.toString();
	}

	/**
	 * get path to log file for tests in <user.home>/.log/jSparrow.test.log
	 * 
	 * @return path to log file as string
	 */
	private static String getTestLogFilePath(String fileName) {
		String userHomeDir = System.getProperty("user.home"); //$NON-NLS-1$

		Path logFilePath = Paths.get(userHomeDir, ".log"); //$NON-NLS-1$

		// create directory <user.home>/.log if it does not exist yet
		if (!logFilePath.toFile()
			.exists()) {
			logFilePath.toFile()
				.mkdirs();
		}

		logFilePath = Paths.get(logFilePath.toString(), fileName);
		return logFilePath.toString();
	}

	/**
	 * file name without extension
	 * 
	 * @param path
	 *            log file path
	 * @return file name without extension
	 */
	private static String getFileNameFromPath(String path) {
		int pos = path.lastIndexOf('.');
		String fname = ""; //$NON-NLS-1$
		if (pos > 0) {
			fname = path.substring(0, pos);
		}
		return fname;
	}

	public static Bundle getBundle() {
		return bundle;
	}

	public static void setBundle(Bundle newBundle) {
		bundle = newBundle;
	}

}
