package at.splendit.simonykees.maven.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import at.splendit.simonykees.maven.JsparrowMojo;

/**
 * 
 * @author Andreja Sambolec, Matthias Webhofer
 * @since 2.3.0
 *
 */
@SuppressWarnings("nls")
public class MavenUtil {

	private static final String USER_DIR = "user.dir";
	private static final String JAVA_TMP = "java.io.tmpdir";
	private static final String STANDALONE_BUNDLE_NAME = "eu.jsparrow.standalone";
	private static final String INSTANCE_DATA_LOCATION_CONSTANT = "osgi.instance.area.default";
	private static final String FRAMEWORK_STORAGE_VALUE = "target/bundlecache";
	private static final String PROJECT_PATH_CONSTANT = "PROJECT.PATH";
	private static final String PROJECT_NAME_CONSTANT = "PROJECT.NAME";
	private static final String JSPARROW_TEMP_FOLDER = "temp_jSparrow";
	private static final String JSPARROW_MANIFEST = "manifest.standalone";
	private static final String OUTPUT_DIRECTORY_CONSTANT = "outputDirectory";
	private static final String DEPENDENCIES_FOLDER_CONSTANT = "deps";

	private MavenUtil() {

	}

	/**
	 * Creates Equinox framework, collects all bundles from src/main/resources,
	 * specified in manifest.standalone file and starts the framework. When
	 * done, stops framework and cleans created temp_jSparrow folder.
	 * 
	 * @param project
	 *            maven project
	 * @param mavenHome
	 *            path to maven home
	 * @param log
	 *            maven logger instance
	 * @throws BundleException
	 * @throws InterruptedException
	 */
	public static void startOSGI(MavenProject project, String mavenHome, Log log)
			throws BundleException, InterruptedException {
		startOSGI(project, mavenHome, log, null);
	}

	/**
	 * Creates Equinox framework, collects all bundles from src/main/resources,
	 * specified in manifest.standalone file and starts the framework. When
	 * done, stops framework and cleans created temp_jSparrow folder.
	 * 
	 * @param project
	 *            maven project
	 * @param mavenHome
	 *            path to maven home
	 * @param log
	 *            maven logger instance
	 * @param additionalConfiguration
	 *            will be added to the standard configuration
	 * @throws BundleException
	 * @throws InterruptedException
	 */
	public static void startOSGI(MavenProject project, String mavenHome, Log log,
			Map<String, String> additionalConfiguration) throws BundleException, InterruptedException {

		final Map<String, String> configuration = new HashMap<>();
		configuration.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
		configuration.put(Constants.FRAMEWORK_STORAGE, FRAMEWORK_STORAGE_VALUE);

		configuration.put(INSTANCE_DATA_LOCATION_CONSTANT, System.getProperty(USER_DIR));
		configuration.put(PROJECT_PATH_CONSTANT, project.getBasedir()
			.getAbsolutePath());
		configuration.put(PROJECT_NAME_CONSTANT, project.getName());

		if (additionalConfiguration != null) {
			configuration.putAll(additionalConfiguration);
		}

		// Set working directory
		String file = System.getProperty(JAVA_TMP);
		File directory = new File(file + File.separator + JSPARROW_TEMP_FOLDER).getAbsoluteFile();
		if (directory.exists()) {
			throw new InterruptedException("jSparrow already running");
		} else if (directory.mkdirs()) {
			System.setProperty(USER_DIR, directory.getAbsolutePath());
			log.info("Set user.dir to " + directory.getAbsolutePath());
		} else {
			throw new InterruptedException("Could not create temp folder");
		}

		extractAndCopyDependencies(project, mavenHome, log);

		ServiceLoader<FrameworkFactory> ffs = ServiceLoader.load(FrameworkFactory.class);
		FrameworkFactory frameworkFactory = ffs.iterator()
			.next();

		final Framework framework = frameworkFactory.newFramework(configuration);

		framework.start();

		final BundleContext ctx = framework.getBundleContext();

		final List<Bundle> bundles = new ArrayList<>();

		try (InputStream is = JsparrowMojo.class.getResourceAsStream(File.separator + JSPARROW_MANIFEST);
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));) {
			String line = "";

			if (is != null) {
				while ((line = reader.readLine()) != null) {
					InputStream fileStream = JsparrowMojo.class.getResourceAsStream(File.separator + line);
					bundles.add(ctx.installBundle("file://" + line, fileStream));
				}
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}

		startBundles(bundles, log);

		// STOP AND WAIT TO STOP WHEN DONE
		framework.stop();
		framework.waitForStop(0);

		// CLEAN
		try {
			deleteChildren(new File(directory.getAbsolutePath()));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		directory.delete();
	}

	/**
	 * Recursively deletes all sub-folders from received folder.
	 * 
	 * @param parentDirectory
	 *            directory which content is to be deleted
	 * @throws IOException
	 */
	private static void deleteChildren(File parentDirectory) throws IOException {
		for (String file : Arrays.asList(parentDirectory.list())) {
			File currentFile = new File(parentDirectory.getAbsolutePath(), file);
			if (currentFile.isDirectory()) {
				deleteChildren(currentFile);
			}
			currentFile.delete();
		}
	}

	/**
	 * Starts eu.jsparrow.standalone bundle which starts all the other needed
	 * bundles.
	 * 
	 * @param bundles
	 *            list of bundles
	 */
	private static void startBundles(List<Bundle> bundles, Log log) {
		for (final Bundle bundle : bundles) {
			if (bundle.getHeaders()
				.get(Constants.FRAGMENT_HOST) == null && null != bundle.getSymbolicName() && (bundle.getSymbolicName()
					.startsWith(STANDALONE_BUNDLE_NAME))) {
				try {
					log.info("Starting BUNDLE: " + bundle.getSymbolicName() + ", resolution: " + bundle.getState());
					bundle.start();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Executes maven goal copy-dependencies on the project to copy all resolved
	 * needed dependencies to the temp folder for use from bundles.
	 */
	private static void extractAndCopyDependencies(MavenProject project, String mavenHome, Log log) {
		final InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(new File(project.getBasedir()
			.getAbsolutePath() + File.separator + "pom.xml"));
		request.setGoals(Collections.singletonList("dependency:copy-dependencies "));
		final Properties props = new Properties();
		props.setProperty(OUTPUT_DIRECTORY_CONSTANT,
				System.getProperty(USER_DIR) + File.separator + DEPENDENCIES_FOLDER_CONSTANT);
		request.setProperties(props);
		final Invoker invoker = new DefaultInvoker();
		// TODO check if maven.home is set, handle if isn't
		log.info("M2_HOME path: " + mavenHome);
		invoker.setMavenHome(new File(mavenHome));
		try {
			invoker.execute(request);
		} catch (final MavenInvocationException e) {
			log.error(e.getMessage(), e);
		}
	}
}
