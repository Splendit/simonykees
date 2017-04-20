package at.splendit.simonykees.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.i18n.Messages;
import at.splendit.simonykees.license.LicenseManager;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa, Andreja Sambolec
 * @since 0.9
 */
public class Activator extends AbstractUIPlugin {

	private static final Logger logger = LoggerFactory.getLogger(Activator.class);
	
	// The plug-in ID
	public static final String PLUGIN_ID = "jSparrow.core"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private static List<Job> jobs = Collections.synchronizedList(new ArrayList<>());

	private long loggingBundleID = 0;
	
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

		/*
		 * JNA first tries to read from jna.boot.library.path. If system
		 * property jna.boot.library.path is set to wrong version from another
		 * project in Eclipse where jSparrow is installed, jSparrow throws
		 * exception. If property is reset it will try to read from
		 * jna.library.path. To avoid that jna.nosys is set to true. This should
		 * force libraries to be unpacked from the jar.
		 * 
		 * See SIM-323 and the explanatory comment. 
		 */
		System.setProperty("jna.boot.library.path", ""); //$NON-NLS-1$ //$NON-NLS-2$
		System.setProperty("jna.nosys", "true"); //$NON-NLS-1$ //$NON-NLS-2$

		// start jSparrow logging bundle
		for(Bundle bundle : context.getBundles()) {
			if(bundle.getSymbolicName().equals("jSparrow.logging")  //$NON-NLS-1$ //name of the logging api bundle
					&& bundle.getState() != Bundle.ACTIVE) {
				bundle.start();
				loggingBundleID = bundle.getBundleId();
				break;
			}
		}
		
		// starting the license heartbeat
		LicenseManager.getInstance();
		logger.info(Messages.Activator_start);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {

		/*
		 * release the current license session (in case of a floating license)
		 */
		LicenseManager.getInstance().checkIn();
		// FIXME (see SIM-331) figure out better logging configuration
		logger.info(Messages.Activator_stop);
		
		plugin = null;

		synchronized (jobs) {
			jobs.forEach(job -> job.cancel());
			jobs.clear();
		}
		
		// stop jSparrow.logging
		Bundle loggingBundle = context.getBundle(loggingBundleID);
		if(loggingBundle.getState() == Bundle.ACTIVE) {
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
}
