package eu.jsparrow.maven.adapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import eu.jsparrow.maven.i18n.Messages;

/**
 * A class containing functionalities for starting/stopping the equinox
 * framework, and loading/starting OSGi bundles.
 * 
 * @author Andreja Sambolec, Matthias Webhofer, Ardit Ymeri
 * @since 2.5.0
 *
 */
public class BundleStarter {

	private static final String JSPARROW_BUNDLE_PREFIX = "eu.jsparrow."; //$NON-NLS-1$
	protected static final String STANDALONE_BUNDLE_NAME = "eu.jsparrow.standalone"; //$NON-NLS-1$
	private static final String JSPARROW_MANIFEST = "manifest.standalone"; //$NON-NLS-1$

	private Framework framework;
	private BundleContext bundleContext = null;

	private Log log;
	private boolean standaloneStarted = false;
	private long standaloneBundleID;
	private WorkingDirectory workingDirectory;

	public BundleStarter(WorkingDirectory workingDirectory, Log log) {
		this.log = log;
		this.workingDirectory = workingDirectory;
		standaloneBundleID = 0;
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
			throws BundleException, MojoExecutionException, InterruptedException {

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
		log.debug(Messages.BundleStarter_startEquinox);

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
		bundles.stream()
			.filter(bundle -> bundle.getHeaders()
				.get(Constants.FRAGMENT_HOST) == null)
			.filter(bundle -> bundle.getSymbolicName() != null)
			.filter(bundle -> bundle.getSymbolicName()
				.startsWith(JSPARROW_BUNDLE_PREFIX))
			.forEach(bundle -> {
				try {
					String loggerInfo = NLS.bind(Messages.BundleStarter_startingBundle, bundle.getSymbolicName(),
							bundle.getState());
					log.debug(loggerInfo);

					bundle.start();
					if (bundle.getSymbolicName()
						.startsWith(STANDALONE_BUNDLE_NAME)) {
						standaloneBundleID = bundle.getBundleId();
						standaloneStarted = true;
					}
				} catch (Exception e) {
					log.debug(e.getMessage(), e);
					log.error(e.getMessage());
				}
			});
	}

	/**
	 * Loads the manifest.standalone file, reads the names of the needed bundles
	 * and installs them in the framework's bundle context
	 * 
	 * @return a list of the installed bundles
	 * @throws BundleException
	 */
	protected List<Bundle> loadBundles() throws BundleException, MojoExecutionException {
		log.debug(Messages.BundleStarter_loadOsgiBundles);

		bundleContext = getBundleContext();
		final List<Bundle> bundles = new ArrayList<>();

		try (InputStream is = getManifestInputStream()) {
			if (is != null) {
				try (BufferedReader reader = getBufferedReaderFromInputStream(is)) {

					String line = ""; //$NON-NLS-1$
					while ((line = reader.readLine()) != null) {
						InputStream fileStream = getBundleResourceInputStream(line);
						Bundle bundle = bundleContext.installBundle("file://" + line, fileStream); //$NON-NLS-1$
						bundles.add(bundle);
					}
				}
			} else {
				throw new MojoExecutionException(
						"The standalone manifest file could not be found. Please read the readme-file."); //$NON-NLS-1$
			}
		} catch (IOException e) {
			log.debug(e.getMessage(), e);
			log.error(e.getMessage());
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
	private void stopEquinoxFramework() throws InterruptedException, BundleException, MojoExecutionException {
		framework.stop();
		framework.waitForStop(0);
		standaloneStarted = false;

		log.debug(Messages.BundleStarter_equinoxStopped);

		String exitMessage = bundleContext.getProperty("eu.jsparrow.standalone.exit.message"); //$NON-NLS-1$
		if (exitMessage != null && !exitMessage.isEmpty()) {
			throw new MojoExecutionException(exitMessage);
		}
	}

	/**
	 * Shuts down the standalone bundle and equinox
	 */
	public void shutdownFramework() {
		if (null != this.getFramework() && null != this.getFramework()
			.getBundleContext()) {
			try {
				Bundle standaloneBundle = this.getFramework()
					.getBundleContext()
					.getBundle(this.getStandaloneBundleID());
				if (standaloneBundle.getState() == Bundle.ACTIVE) {
					standaloneBundle.stop();
				}

				this.stopEquinoxFramework();
			} catch (BundleException | InterruptedException | MojoExecutionException e) {
				log.debug(e.getMessage(), e);
				log.error(e.getMessage());
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

	/**
	 * shuts down the equinox framework and cleans all generated files
	 * 
	 * @param mavenAdapter
	 */
	public void shutdown(MavenAdapter mavenAdapter) {
		shutdownFramework();
		if (!mavenAdapter.isJsparrowRunningFlag()) {
			workingDirectory.cleanUp();
		}
	}

	public boolean isStandaloneStarted() {
		return this.standaloneStarted;
	}

	public WorkingDirectory getWorkingDirectoryWatcher() {
		return this.workingDirectory;
	}
}
