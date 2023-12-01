package eu.jsparrow.independent.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class BundleStarter {

	private static final String JSPARROW_BUNDLE_PREFIX = "eu.jsparrow."; //$NON-NLS-1$
	private static final String STANDALONE_BUNDLE_NAME = "eu.jsparrow.independent"; //$NON-NLS-1$
	private static final String ORG_APACHE_FELIX_SCR = "org.apache.felix.scr"; //$NON-NLS-1$
	static final String JSPARROW_MANIFEST = "manifest.independent"; //$NON-NLS-1$

	private Framework framework;
	private BundleContext bundleContext = null;

	private boolean standaloneStarted = false;
	private long standaloneBundleID;

	/**
	 * Starts the equinox framework with the given configuration, starts the
	 * related bundles and stops the framework afterwards.
	 * 
	 * @param configuration
	 *            the configuration to start the framework with.
	 * @throws BundleException
	 * @throws MojoExecutionException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void runStandalone(Map<String, String> configuration)
			throws BundleException, InterruptedException, IOException {

		startEquinoxFramework(configuration);

		List<Bundle> bundles = installBundles();
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
		// log.debug(Messages.BundleStarter_startEquinox);

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

		List<Bundle> jSparrowBundles = bundles.stream()
			.filter(bundle -> bundle.getHeaders()
				.get(Constants.FRAGMENT_HOST) == null)
			.filter(bundle -> bundle.getSymbolicName() != null)
			.filter(bundle -> bundle.getSymbolicName()
				.startsWith(JSPARROW_BUNDLE_PREFIX))
			.collect(Collectors.toList());

		jSparrowBundles.stream()
			.filter(bundle -> !bundle.getSymbolicName()
				.startsWith(STANDALONE_BUNDLE_NAME))
			.forEach(bundle -> {
				try {
					bundle.start();
				} catch (BundleException e) {
					e.printStackTrace();
				}
			});

		jSparrowBundles.stream()
			.filter(bundle -> bundle.getSymbolicName()
				.startsWith(STANDALONE_BUNDLE_NAME))
			.findFirst()
			.ifPresent(bundle -> {
				try {
					bundle.start();
				} catch (BundleException e) {
					e.printStackTrace();
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
					// String message = String.format("Starting bundle %s:%s
					// [%d]", symbolicName, bundle.getVersion(), //$NON-NLS-1$
					// bundle.getState());
					// log.debug(message);
					bundle.start();
				} catch (BundleException e) {
					// log.debug(e.getMessage(), e);
					// log.error(e.getMessage());
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
	 * @throws IOException
	 */
	protected List<Bundle> installBundles() throws BundleException, IOException {

		List<String> namesOfbundlesToInstall = ProductPlugInHelper.getProductPlugInNames()
			.stream()
			.filter(name -> !name.startsWith("org.eclipse.osgi_")) //$NON-NLS-1$
			.collect(Collectors.toList());

		bundleContext = getBundleContext();
		final List<Bundle> bundles = new ArrayList<>();

		for (String bundleName : namesOfbundlesToInstall) {

			try (InputStream fileStream = getBundleResourceInputStream(bundleName)) {
				Bundle bundle = bundleContext.installBundle("file://" + bundleName, fileStream); //$NON-NLS-1$
				bundles.add(bundle);
			}

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

		// log.debug(Messages.BundleStarter_equinoxStopped);

		String exitMessage = bundleContext.getProperty("eu.jsparrow.standalone.exit.message"); //$NON-NLS-1$
		if (exitMessage != null && !exitMessage.isEmpty()) {
			// throw new MojoExecutionException(exitMessage);
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
				// log.debug(e.getMessage(), e);
				// log.error(e.getMessage());
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
