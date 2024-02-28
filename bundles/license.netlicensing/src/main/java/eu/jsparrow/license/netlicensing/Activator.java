package eu.jsparrow.license.netlicensing;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator extends Plugin {

	public static final Logger logger = LoggerFactory.getLogger(Activator.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "eu.jsparrow.license.netlicensing"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		plugin = this;

		// start jSparrow logging bundle
		for (Bundle bundle : context.getBundles()) {
			if ("eu.jsparrow.logging".equals(bundle.getSymbolicName()) //$NON-NLS-1$
					&& bundle.getState() != Bundle.ACTIVE) {
				bundle.start();
				break;
			}
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
