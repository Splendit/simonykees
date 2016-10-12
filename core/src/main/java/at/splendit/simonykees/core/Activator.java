package at.splendit.simonykees.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "simonykees.core"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private static List<Job> jobs = Collections.synchronizedList(new ArrayList<>());
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin  = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		
		synchronized (jobs) {
			jobs.forEach(job -> job.cancel());
			jobs.clear();
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
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public static void log(int severity, String message, Exception e) {
		log(new SimonykeesStatus(severity, PLUGIN_ID, message, e));
	}
	
	public static void log(String message, Exception e) {
		log(new SimonykeesStatus(IStatus.INFO, PLUGIN_ID, message, e));
	}
	
	public static void log(String message) {
		log(new SimonykeesStatus(IStatus.INFO, PLUGIN_ID, message));
	}
	
	private static void log(Status status) {
		final ILog log = getDefault().getLog();
		log.log(status);
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
