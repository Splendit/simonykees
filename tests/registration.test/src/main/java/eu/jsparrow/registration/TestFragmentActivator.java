package eu.jsparrow.registration;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import eu.jsparrow.logging.LoggingUtil;

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