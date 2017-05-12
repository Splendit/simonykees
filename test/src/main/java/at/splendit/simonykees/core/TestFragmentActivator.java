package at.splendit.simonykees.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import at.splendit.simonykees.logging.LoggingUtil;

/**
 * Pseudo Activator for test fragment. Gets called by the Activator from the
 * fragment host via reflection.
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public class TestFragmentActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {

		LoggingUtil.configureLoggerForTesting();

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LoggingUtil.configureLogger();
	}

}
