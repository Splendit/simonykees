package at.splendit.simonykees.license;


import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator extends Plugin {
	
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
		 * BundleContext)
		 */
		public void start(BundleContext context) throws Exception {
			plugin = this;
			
			// start jSparrow logging bundle
			for(Bundle bundle : context.getBundles()) {
				if(bundle.getSymbolicName().equals("jSparrow.logging") 
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


//
//		public static void log(int severity, String message, Exception e) {
//			log(new SimonykeesStatus(severity, PLUGIN_ID, message, e));
//		}
//
//		public static void log(String message, Exception e) {
//			log(new SimonykeesStatus(IStatus.INFO, PLUGIN_ID, message, e));
//		}
//
//		public static void log(int severity, String message) {
//			log(new SimonykeesStatus(severity, PLUGIN_ID, message));
//		}
//
//		public static void log(String message) {
//			log(new SimonykeesStatus(IStatus.INFO, PLUGIN_ID, message));
//		}
//
//		private static void log(Status status) {
//			final ILog log = getDefault().getLog();
//			log.log(status);
//		}

}
