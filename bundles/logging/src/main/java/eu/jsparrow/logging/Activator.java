package eu.jsparrow.logging;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.qos.logback.core.joran.spi.JoranException;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public class Activator implements BundleActivator {
	private static final String DEBUG_ENABLED = "debug.enabled"; //$NON-NLS-1$
	// The plug-in ID
	public static final String PLUGIN_ID = "eu.jsparrow.logging"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		plugin = this;
		configureLogger(context);
	}

	void configureLogger(BundleContext context) throws JoranException, IOException {
		List<String> jSparrowBundleSymbolicNames = getJSparrowBundleSymbolicNames(context);
		boolean debugEnabled = isDebugEnabled(context);
		if (containsTest(jSparrowBundleSymbolicNames)) {
			LoggingUtil.configureLoggerForTesting();
		} else if (isStandalone(jSparrowBundleSymbolicNames)) {
			LoggingUtil.configureLogger(debugEnabled);
		} else {
			LoggingUtil.configureLogger();
		}

	}

	private List<String> getJSparrowBundleSymbolicNames(BundleContext context) {
		return Arrays.stream(context.getBundles())
			.map(Bundle::getSymbolicName)
			.filter(symbolicName -> symbolicName.startsWith("eu.jsparrow."))
			.collect(Collectors.toList());
	}

	private boolean isDebugEnabled(BundleContext context) {
		String debugEnabledValue = context.getProperty(DEBUG_ENABLED);
		if (debugEnabledValue == null) {
			return false;
		}
		return Boolean.parseBoolean(debugEnabledValue);
	}

	private boolean containsTest(List<String> symbolicNames) {
		for (String symbolicName : symbolicNames) {
			if (symbolicName.endsWith(".test")) {
				return true;
			}
		}
		return false;
	}

	private boolean isStandalone(List<String> symbolicNames) {
		boolean jSparrowStandaloneFound = false;
		for (String symbolicName : symbolicNames) {
			if (symbolicName.endsWith(".ui")) {
				return false;
			}
			if (symbolicName.endsWith(".standalone")) {
				jSparrowStandaloneFound = true;
			}
		}
		return jSparrowStandaloneFound;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
