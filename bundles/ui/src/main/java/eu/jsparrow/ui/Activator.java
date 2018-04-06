package eu.jsparrow.ui;

import javax.inject.Inject;

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

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.util.LicenseUtilService;

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
	public static final String PLUGIN_ID = "eu.jsparrow.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	// is used for configuring the test fragment
	private static BundleActivator testFragmentActivator;

	// Flag is jSparrow is already running
	private static boolean running = false;

	private static BundleContext bundleContext;

	private static IEclipseContext eclipseContext;

	private long loggingBundleID = 0;
	
	@Inject
	private LicenseUtilService licenseUtil;

	/**
	 * The constructor
	 */
	public Activator() {
		//
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		bundleContext = context;

		setEclipseContext(EclipseContextFactory.getServiceContext(context));
		ContextInjectionFactory.inject(this, eclipseContext);
		

		// start jSparrow logging bundle
		for (Bundle bundle : context.getBundles()) {
			if ("eu.jsparrow.logging".equals(bundle.getSymbolicName()) //$NON-NLS-1$
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
				.forName("at.splendit.simonykees.core.TestFragmentActivator") //$NON-NLS-1$
				.asSubclass(BundleActivator.class);
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
			System.out.println("Error catching"); //$NON-NLS-1$
		}

		logger.info(Messages.Activator_start);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {

		setRunning(false);
		
		licenseUtil.stop();

		// FIXME (see SIM-331) figure out better logging configuration
		logger.info(Messages.Activator_stop);

		plugin = null;
		bundleContext = null;

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

	public static void setEclipseContext(IEclipseContext eclipseContext) {
			Activator.eclipseContext = eclipseContext;
	}
}