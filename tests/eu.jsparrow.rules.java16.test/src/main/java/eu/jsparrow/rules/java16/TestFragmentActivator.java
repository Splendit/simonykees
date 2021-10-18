package eu.jsparrow.rules.java16;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import eu.jsparrow.logging.LoggingUtil;

/**
 * Pseudo Activator for test fragment. Gets called by the Activator from the
 * fragment host via reflection.
 * 
 * @since 4.4.0
 *
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