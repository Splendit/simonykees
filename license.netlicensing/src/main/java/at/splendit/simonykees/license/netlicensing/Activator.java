package at.splendit.simonykees.license.netlicensing;


import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator extends Plugin {
	
	public static final Logger logger = LoggerFactory.getLogger(Activator.class);

	// The plug-in ID
		public static final String PLUGIN_ID = "jSparrow.core"; //$NON-NLS-1$

		// The shared instance
		private static Activator plugin;
		
		// is used for configuring the test fragment
		private static BundleActivator testFragmentActivator;


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
				if(bundle.getSymbolicName().equals("jSparrow.logging")  //$NON-NLS-1$
						&& bundle.getState() != Bundle.ACTIVE) {
					bundle.start();
					break;
				}
			}
			
			// load pseudo-activator from test fragment and execute its start method
			try {
				Class<? extends BundleActivator> frgActClass = Class
						.forName("at.splendit.simonykees.license.netlicensing.TestFragmentActivator").asSubclass(BundleActivator.class); //$NON-NLS-1$
				testFragmentActivator = frgActClass.newInstance();
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
}
