package at.splendit.simonykees.core;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	public static final Logger logger = LoggerFactory.getLogger(Activator.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "jSparrow.core"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		plugin = this;

		// start jSparrow logging bundle
		for (Bundle bundle : context.getBundles()) {
			if (bundle.getSymbolicName().equals("jSparrow.logging") //$NON-NLS-1$
					&& bundle.getState() != Bundle.ACTIVE) {
				bundle.start();
				break;
			}
		}
//
//		// load pseudo-activator from test fragment and execute its start method
//		try {
//			Class<? extends BundleActivator> frgActClass = Class
//					.forName("at.splendit.simonykees.license.netlicensing.TestFragmentActivator") //$NON-NLS-1$
//					.asSubclass(BundleActivator.class);
//			testFragmentActivator = frgActClass.newInstance();
//			testFragmentActivator.start(context);
//		} catch (ClassNotFoundException e) {
//			/*
//			 * Ignore! Exception is thrown, if the test fragment is not
//			 * available.
//			 * 
//			 * Note: The test fragment is always available, except in the
//			 * deployed version. We do not want to have any log message at all
//			 * in that case because customers should not know about test
//			 * fragments.
//			 */
//		}
	}

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
