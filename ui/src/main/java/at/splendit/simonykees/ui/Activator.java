package at.splendit.simonykees.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.i18n.Messages;
import at.splendit.simonykees.license.api.LicenseValidationService;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa, Andreja Sambolec,
 *         Matthias Webhofer
 * @since 0.9
 */
public class Activator extends AbstractUIPlugin {

	private static final Logger logger = LoggerFactory.getLogger(Activator.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "jSparrow.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	// is used for configuring the test fragment
	private static BundleActivator testFragmentActivator;

	private static List<Job> jobs = Collections.synchronizedList(new ArrayList<>());

	private long loggingBundleID = 0;

	// Flag is jSparrow is already running
	private static boolean running = false;

	private static BundleContext bundleContext;
	private static IEclipseContext eclipseContext;

	@Inject
	private LicenseValidationService licenseValidationService;

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
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		bundleContext = context;

		eclipseContext = EclipseContextFactory.getServiceContext(context);
		ContextInjectionFactory.inject(this, eclipseContext);

		// start jSparrow logging bundle
		for (Bundle bundle : context.getBundles()) {
			if (bundle.getSymbolicName().equals("jSparrow.logging") //$NON-NLS-1$
					/*
					 * name of the logging api bundle
					 */
					&& bundle.getState() != Bundle.ACTIVE) {
				bundle.start();
				loggingBundleID = bundle.getBundleId();
				break;
			}
		}

		// load pseudo-activator from test fragment and execute its start method
		try {
			Class<? extends BundleActivator> fragmentActivatorClass = Class
					.forName("at.splendit.simonykees.core.TestFragmentActivator").asSubclass(BundleActivator.class); //$NON-NLS-1$
			testFragmentActivator = fragmentActivatorClass.newInstance();
			testFragmentActivator.start(context);
		} catch (ClassNotFoundException e) {
			/*
			 * Ignore! Exception is thrown, if the test fragment is not
			 * available.
			 * 
			 * Note: The test fragment is always available, except in the
			 * deployed version. We do not want to have any log message at all
			 * in that case because customers should not know about test
			 * fragments.
			 */
		}

		logger.info(Messages.Activator_start);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {

		running = false;

		// FIXME (see SIM-331) figure out better logging configuration
		logger.info(Messages.Activator_stop);

		plugin = null;
		bundleContext = null;

		synchronized (jobs) {
			jobs.forEach(job -> job.cancel());
			jobs.clear();
		}

		// stop test fragment pseudo-activator
		if (testFragmentActivator != null) {
			testFragmentActivator.stop(context);
		}

		// stop jSparrow.logging
		Bundle loggingBundle = context.getBundle(loggingBundleID);
		if (loggingBundle.getState() == Bundle.ACTIVE) {
			loggingBundle.stop();
		}

		super.stop(context);
	}

	/**
	 * starts the license validation service after it has been injected
	 */
	@PostConstruct
	private void startValidation() {
		licenseValidationService.startValidation();
	}

	/**
	 * stops the license validation service before it gets uninjected
	 */
	@PreDestroy
	private void stopValidation() {
		licenseValidationService.stopValidation();
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
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static void registerJob(Job job) {
		synchronized (jobs) {
			jobs.add(job);
		}
	}

	public static void unregisterJob(Job job) {
		synchronized (jobs) {
			jobs.remove(job);
		}
	}

	public static boolean isRunning() {
		return running;
	}

	public static void setRunning(boolean isRunning) {
		running = isRunning;
	}

	public static BundleContext getBundleContext() {
		return bundleContext;
	}

	public static IEclipseContext getEclipseContext() {
		return eclipseContext;
	}
}