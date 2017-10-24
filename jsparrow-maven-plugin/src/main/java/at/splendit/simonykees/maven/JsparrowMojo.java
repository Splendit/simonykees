package at.splendit.simonykees.maven;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
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

/**
 * Starts Equinox framework and headless version of jSparrow Eclipse plugin.
 * 
 * @author Andreja Sambolec
 * @since 2.2.1
 *
 */
@SuppressWarnings("nls")
@Mojo(name = "refactor", defaultPhase = LifecyclePhase.INSTALL, requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true)
public class JsparrowMojo extends AbstractMojo {

	/**
	 * Maven project on which plugin goal is executed
	 */
	@Parameter(defaultValue = "${project}", required = true)
	MavenProject project;

	/**
	 * Value of maven home environment variable
	 */
	@Parameter(defaultValue = "${maven.home}", required = true)
	String mavenHome;

	@Parameter(defaultValue = "jsparrow.yml", property = "configFile")
	protected File configFile;

	@Parameter(defaultValue = "", property = "profile")
	protected String profile;

	// CONSTANTS
	public static final String USER_DIR = "user.dir";
	public static final String JAVA_TMP = "java.io.tmpdir";
	public static final String STANDALONE_BUNDLE_NAME = "eu.jsparrow.standalone";
	public static final String INSTANCE_DATA_LOCATION_CONSTANT = "osgi.instance.area.default";
	public static final String FRAMEWORK_STORAGE_VALUE = "target/bundlecache";
	public static final String PROJECT_PATH_CONSTANT = "PROJECT.PATH";
	public static final String PROJECT_NAME_CONSTANT = "PROJECT.NAME";
	public static final String CONFIG_FILE_PATH = "CONFIG.FILE.PATH";
	public static final String SELECTED_PROFILE = "PROFILE.SELECTED";
	public static final String JSPARROW_TEMP_FOLDER = "temp_jSparrow";
	public static final String JSPARROW_MANIFEST = "manifest.standalone";
	public static final String OUTPUT_DIRECTORY_CONSTANT = "outputDirectory";
	public static final String DEPENDENCIES_FOLDER_CONSTANT = "deps";

	public void execute() throws MojoExecutionException {
		try {
			startOSGI();
		} catch (BundleException | InterruptedException e) {
			getLog().error(e.getMessage(), e);
		}

	}

	/**
	 * Creates Equinox framework, collects all bundles from src/main/resources,
	 * specified in manifest.standalone file and starts the framework. When
	 * done, stops framework and cleans created temp_jSparrow folder.
	 * 
	 * @throws BundleException
	 * @throws InterruptedException
	 */
	private void startOSGI() throws BundleException, InterruptedException {
		final Map<String, String> configuration = new HashMap<>();
		configuration.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
		configuration.put(Constants.FRAMEWORK_STORAGE, FRAMEWORK_STORAGE_VALUE);
		configuration.put(CONFIG_FILE_PATH,
				(configFile.exists() && !configFile.isDirectory()) ? configFile.getAbsolutePath() : "");
		configuration.put(SELECTED_PROFILE, (profile == null) ? "" : profile);

		// Set working directory
		String file = System.getProperty(JAVA_TMP);
		File directory = new File(file + File.separator + JSPARROW_TEMP_FOLDER).getAbsoluteFile();
		if (directory.exists()) {
			throw new InterruptedException("jSparrow already running");
		} else if (directory.mkdirs()) {
			System.setProperty(USER_DIR, directory.getAbsolutePath());
			getLog().info("Set user.dir to " + directory.getAbsolutePath());
		} else {
			throw new InterruptedException("Could not create temp folder");
		}

		configuration.put(INSTANCE_DATA_LOCATION_CONSTANT, System.getProperty(USER_DIR));
		configuration.put(PROJECT_PATH_CONSTANT, project.getBasedir()
			.getAbsolutePath());
		configuration.put(PROJECT_NAME_CONSTANT, project.getName());

		extractAndCopyDependencies();

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
			getLog().error(e.getMessage(), e);
		}

		startBundles(bundles);

		// STOP AND WAIT TO STOP WHEN DONE
		framework.stop();
		framework.waitForStop(0);

		// CLEAN
		try {
			deleteChildren(new File(directory.getAbsolutePath()));
		} catch (IOException e) {
			getLog().error(e.getMessage(), e);
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
	private void deleteChildren(File parentDirectory) throws IOException {
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
	private void startBundles(List<Bundle> bundles) {
		for (final Bundle bundle : bundles) {
			if (bundle.getHeaders()
				.get(Constants.FRAGMENT_HOST) == null && null != bundle.getSymbolicName() && (bundle.getSymbolicName()
					.startsWith(STANDALONE_BUNDLE_NAME))) {
				try {
					getLog()
						.info("Starting BUNDLE: " + bundle.getSymbolicName() + ", resolution: " + bundle.getState());
					bundle.start();
				} catch (Exception e) {
					getLog().error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Executes maven goal copy-dependencies on the project to copy all resolved
	 * needed dependencies to the temp folder for use from bundles.
	 */
	private void extractAndCopyDependencies() {
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
		getLog().info("M2_HOME path: " + mavenHome);
		invoker.setMavenHome(new File(mavenHome));
		try {
			invoker.execute(request);
		} catch (final MavenInvocationException e) {
			getLog().error(e.getMessage(), e);
		}
	}
}
