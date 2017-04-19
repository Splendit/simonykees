package at.splendit.simonykees.logging;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.LogManager;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "jSparrow.logging"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		configureLogbackInBundle(context.getBundle());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	/**
	 * Configures logback and the jul-to-slf4j logging bridge
	 * @param bundle current Bundle
	 * @throws JoranException
	 * @throws IOException
	 */
	private void configureLogbackInBundle(Bundle bundle) throws JoranException, IOException {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(context);
        context.reset();

        // this assumes that the logback.xml file is in the root of the bundle.
        URL logbackConfigFileUrl = FileLocator.find(bundle, new Path("logback.xml"),null);
        jc.doConfigure(logbackConfigFileUrl.openStream());
        
        configureFileAppender(context);
        
        // configure jul-to-slf4j bridge
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        java.util.logging.Logger.getLogger("global").setLevel(Level.FINEST);
	}
	
	/**
	 * configures a file appender to set the log file path from code 
	 * and attaches it to the root logger
	 * @param loggerContext
	 */
	private void configureFileAppender(LoggerContext loggerContext) {
		
		// get path to logfile in <eclipse-workspace>/.metadata/jSparrow.log
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath logFilePath = workspace.getRoot().getLocation().append(".metadata").append("jSparrow.log");
		String logFilePathStr = logFilePath.toString();
		
		// set pattern according to logback documentation (https://logback.qos.ch/manual/layouts.html)
		PatternLayoutEncoder ple = new PatternLayoutEncoder();
		ple.setContext(loggerContext);
		ple.setPattern("%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n");
		ple.start();
		
		// create and configure file appender
		FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
		fileAppender.setContext(loggerContext);
		fileAppender.setName("FILE");
		fileAppender.setFile(logFilePathStr);
		fileAppender.setEncoder(ple);
		fileAppender.start();
		
		// add file appender to the root logger
		Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		rootLogger.addAppender(fileAppender);
	}
}
