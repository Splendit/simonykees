package at.splendit.simonykees.logging;

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
import org.slf4j.bridge.SLF4JBridgeHandler;

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

	private static final String ROLLING_FILE_APPENDER_NAME = "at.splendit.simonykees.logging.rollingFile"; //$NON-NLS-1$
	private static final String TEST_ROLLING_FILE_APPENDER_NAME = "at.splendit.simonykees.logging.test.rollingFile"; //$NON-NLS-1$

	private static Bundle bundle = null;

	private static boolean isLogbackConfigured = false;

	/**
	 * Triggers the logging configuration for plug in tests
	 * 
	 * @return true, if the configuration was successful, false otherwise
	 * @throws JoranException
	 * @throws IOException
	 */
	public static boolean configureLoggerForTesting() throws JoranException, IOException {
		if (bundle != null) {
			configureLogback(bundle);
			removeAppenderFromRootLogger(ROLLING_FILE_APPENDER_NAME);
			configureRollingFileAppender(TEST_ROLLING_FILE_APPENDER_NAME, getTestLogFilePath());
			return true;
		}
		return false;
	}

	/**
	 * Triggers the standard logging configuration
	 * 
	 * @return true, if the configuration was successful, false otherwise
	 * @throws JoranException
	 * @throws IOException
	 */
	public static boolean configureLogger() throws JoranException, IOException {
		if (bundle != null) {
			configureLogback(bundle);
			removeAppenderFromRootLogger(TEST_ROLLING_FILE_APPENDER_NAME);
			configureRollingFileAppender(ROLLING_FILE_APPENDER_NAME, getLogFilePath());
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
	 * @throws IOException
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

	/**
	 * Configures the java.util.logging to slf4j bridge
	 */
	private static void configureJulToSlf4jBridge() {
		LogManager.getLogManager().reset();
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		java.util.logging.Logger.getLogger("global").setLevel(Level.FINEST); //$NON-NLS-1$
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
	private static void configureRollingFileAppender(String fileAppenderName, String filePath) {
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

		addAppenderToRootLogger(rollingFileAppender);
	}

	/**
	 * adds the given appender to the root logger
	 * 
	 * @param appender
	 */
	private static void addAppenderToRootLogger(OutputStreamAppender<ILoggingEvent> appender) {
		Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		rootLogger.addAppender(appender);
	}

	/**
	 * Removes the appender with the given name from the root logger
	 * 
	 * @param appenderName
	 */
	private static void removeAppenderFromRootLogger(String appenderName) {
		Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		rootLogger.detachAppender(appenderName);
	}

	/**
	 * get path to log file in <eclipse-workspace>/.metadata/jSparrow.log
	 * 
	 * @return path to log file as string
	 */
	private static String getLogFilePath() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath logFilePath = workspace.getRoot().getLocation().append(".metadata").append("jSparrow.log"); //$NON-NLS-1$//$NON-NLS-2$
		return logFilePath.toString();
	}

	/**
	 * get path to log file for tests in <user.home>/.log/jSparrow.test.log
	 * 
	 * @return path to log file as string
	 */
	private static String getTestLogFilePath() {
		String userHomeDir = System.getProperty("user.home"); //$NON-NLS-1$

		Path logFilePath = Paths.get(userHomeDir, ".log"); //$NON-NLS-1$

		// create directory <user.home>/.log if it does not exist yet
		if (!logFilePath.toFile().exists()) {
			logFilePath.toFile().mkdirs();
		}

		logFilePath = Paths.get(logFilePath.toString(), "jSparrow.test.log"); //$NON-NLS-1$
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
		int pos = path.lastIndexOf("."); //$NON-NLS-1$
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
