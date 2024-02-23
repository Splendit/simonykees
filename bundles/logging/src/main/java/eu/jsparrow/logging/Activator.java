package eu.jsparrow.logging;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "eu.jsparrow.logging"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private static boolean containsJSparrowTestingBundle(BundleContext context) {
		Bundle[] bundleArray = context.getBundles();
		for (Bundle bundle : bundleArray) {
			if(isJSparrowTestingBundle(bundle)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isJSparrowTestingBundle(Bundle bundle) {
		String symbolicName = bundle.getSymbolicName();
		if(!symbolicName.endsWith(".test")) {
			return false;
		}
		return symbolicName.startsWith("eu.jsparrow.");
	}

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
		LoggingUtil.setBundle(context.getBundle());
		if (containsJSparrowTestingBundle(context)) {
			LoggingUtil.configureLoggerForTesting();
		} else {
			LoggingUtil.configureLogger();
		}
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
