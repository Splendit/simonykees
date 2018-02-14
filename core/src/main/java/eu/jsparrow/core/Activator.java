package eu.jsparrow.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.rules.api.RuleService;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa, Andreja Sambolec,
 *         Matthias Webhofer
 * @since 0.9
 */
public class Activator implements BundleActivator {

	public static final Logger logger = LoggerFactory.getLogger(Activator.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "eu.jsparrow.core"; //$NON-NLS-1$

	private static List<Job> jobs = Collections.synchronizedList(new ArrayList<>());

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
		// Needed for Bundle
	}

	@Override
	public void start(BundleContext context) throws Exception {
		plugin = this;
		// This is how to get all the services. Properly.
		ServiceReference<?> serviceReference = context.
			    getServiceReference(RuleService.class.getName());
		RuleService service = (RuleService) context.
			    getService(serviceReference);
		List<String> asdf = service.loadRules();

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;

		synchronized (jobs) {
			jobs.forEach(Job::cancel);
			jobs.clear();
		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
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
