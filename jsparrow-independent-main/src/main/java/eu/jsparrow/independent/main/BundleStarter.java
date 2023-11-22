package eu.jsparrow.independent.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;



public class BundleStarter {
	

	private static final String JSPARROW_BUNDLE_PREFIX = "eu.jsparrow."; //$NON-NLS-1$
	protected static final String STANDALONE_BUNDLE_NAME = "eu.jsparrow.independent"; //$NON-NLS-1$
	protected static final String ORG_APACHE_FELIX_SCR = "org.apache.felix.scr"; //$NON-NLS-1$
	private static final String JSPARROW_MANIFEST = "manifest.independent"; //$NON-NLS-1$

	private Framework framework;
	private BundleContext bundleContext = null;


	private boolean standaloneStarted = false;
	private long standaloneBundleID;

	public BundleStarter() {


		
	}

	/**
	 * Starts the equinox framework with the given configuration, starts the
	 * related bundles and stops the framework afterwards.
	 * 
	 * @param configuration
	 *            the configuration to start the framework with.
	 * @throws BundleException
	 * @throws MojoExecutionException
	 * @throws InterruptedException
	 */
	public void runStandalone(Map<String, String> configuration)
			throws BundleException, InterruptedException {

		startEquinoxFramework(configuration);

		List<Bundle> bundles = loadBundles();
		startBundles(bundles);

		stopEquinoxFramework();
	}

	/**
	 * Starts the equinox framework with the given configuration
	 * 
	 * @param configuration
	 *            the configuration to start the framework with
	 * @throws BundleException
	 *             if the framework cannot be started.
	 */
	private void startEquinoxFramework(Map<String, String> configuration) throws BundleException {
		//log.debug(Messages.BundleStarter_startEquinox);

		ServiceLoader<FrameworkFactory> ffs = ServiceLoader.load(FrameworkFactory.class);
		FrameworkFactory frameworkFactory = ffs.iterator()
			.next();

		framework = frameworkFactory.newFramework(configuration);
		framework.start();
	}

	/**
	 * Starts eu.jsparrow.standalone bundle which starts all the other needed
	 * bundles.
	 * 
	 * @param bundles
	 *            list of bundles
	 */
	protected void startBundles(List<Bundle> bundles) {
		startApacheFelixSCR(bundles);

		bundles.stream()
			.filter(bundle -> bundle.getHeaders()
				.get(Constants.FRAGMENT_HOST) == null)
			.filter(bundle -> bundle.getSymbolicName() != null)
			.filter(bundle -> bundle.getSymbolicName()
				.startsWith(JSPARROW_BUNDLE_PREFIX))
			.forEach(bundle -> {
				try {
//					String loggerInfo = NLS.bind(Messages.BundleStarter_startingBundle, bundle.getSymbolicName(),
//							bundle.getState());
//					log.debug(loggerInfo);

					bundle.start();
					if (bundle.getSymbolicName()
						.startsWith(STANDALONE_BUNDLE_NAME)) {
						standaloneBundleID = bundle.getBundleId();
						standaloneStarted = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
//					log.debug(e.getMessage(), e);
//					log.error(e.getMessage());
				}
			});
	}

	protected void startApacheFelixSCR(List<Bundle> bundles) {
		/*
		 * org.apache.felix.scr has to be started before we start
		 * eu.jsparrow.standalone and other jSparrow bundles. See also SIM-1406
		 * and SIM-1997
		 */
		for (Bundle bundle : bundles) {
			String symbolicName = bundle.getSymbolicName();
			if (symbolicName.startsWith(ORG_APACHE_FELIX_SCR)) {
				try {
					String message = String.format("Starting bundle %s:%s [%d]", symbolicName, bundle.getVersion(), bundle.getState()); //$NON-NLS-1$
//					log.debug(message);
					bundle.start();
				} catch (BundleException e) {
//					log.debug(e.getMessage(), e);
//					log.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * Loads the manifest.standalone file, reads the names of the needed bundles
	 * and installs them in the framework's bundle context
	 * 
	 * @return a list of the installed bundles
	 * @throws BundleException
	 */
	protected List<Bundle> loadBundles() throws BundleException {
//		log.debug(Messages.BundleStarter_loadOsgiBundles);

		bundleContext = getBundleContext();
		final List<Bundle> bundles = new ArrayList<>();

		try (InputStream is = getManifestInputStream()) {
			if (is != null) {
				try (BufferedReader reader = getBufferedReaderFromInputStream(is)) {

					String line = ""; //$NON-NLS-1$
					while ((line = reader.readLine()) != null) {
						InputStream fileStream = getBundleResourceInputStream(line);
						if (!line.startsWith("org.eclipse.osgi_")) { //$NON-NLS-1$
							Bundle bundle = bundleContext.installBundle("file://" + line, fileStream); //$NON-NLS-1$
							bundles.add(bundle);
						}
					}
				}
			} else {
//				throw new MojoExecutionException(
//						"The standalone manifest file could not be found. Please read the readme-file."); //$NON-NLS-1$
			}
		} catch (IOException e) {
//			log.debug(e.getMessage(), e);
//			log.error(e.getMessage());
		}

		return bundles;
	}

	/**
	 * Stops the equinox framework
	 * 
	 * @throws InterruptedException
	 * @throws BundleException
	 * @throws MojoExecutionException
	 */
	private void stopEquinoxFramework() throws InterruptedException, BundleException {
		framework.stop();
		framework.waitForStop(0);
		standaloneStarted = false;

//		log.debug(Messages.BundleStarter_equinoxStopped);

		String exitMessage = bundleContext.getProperty("eu.jsparrow.standalone.exit.message"); //$NON-NLS-1$
		if (exitMessage != null && !exitMessage.isEmpty()) {
//			throw new MojoExecutionException(exitMessage);
		}
	}

	/**
	 * Shuts down the standalone bundle and equinox
	 */
	public void shutdownFramework() {
		if (null != framework && null != framework.getBundleContext()) {
			try {
				Bundle standaloneBundle = framework.getBundleContext()
					.getBundle(this.getStandaloneBundleID());
				if (standaloneBundle.getState() == Bundle.ACTIVE) {
					standaloneBundle.stop();
				}

				this.stopEquinoxFramework();
			} catch (BundleException | InterruptedException e) {
//				log.debug(e.getMessage(), e);
//				log.error(e.getMessage());
			}
		}
	}

	public Framework getFramework() {
		return framework;
	}

	public long getStandaloneBundleID() {
		return standaloneBundleID;
	}

	protected BundleContext getBundleContext() {
		return framework.getBundleContext();
	}

	protected InputStream getManifestInputStream() {
		return getClass().getResourceAsStream("/" + JSPARROW_MANIFEST); //$NON-NLS-1$
	}

	protected InputStream getBundleResourceInputStream(String resouceName) {
		return getClass().getResourceAsStream("/" + resouceName); //$NON-NLS-1$
	}

	protected BufferedReader getBufferedReaderFromInputStream(InputStream is) {
		return new BufferedReader(new InputStreamReader(is));
	}

	public boolean isStandaloneStarted() {
		return this.standaloneStarted;
	}

}
