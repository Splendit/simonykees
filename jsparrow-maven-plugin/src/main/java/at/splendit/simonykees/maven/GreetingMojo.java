package at.splendit.simonykees.maven;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * Says "Hi" to the user.
 *
 */
@Mojo(name = "refactor", defaultPhase = LifecyclePhase.INSTALL, requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true)
public class GreetingMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", required = true)
	MavenProject project;

	/**
	 * The directory where files are located.
	 */
	@Parameter(defaultValue = "${basedir}/src/main/java")
	protected File sourceDirectory;
	
	@Parameter(defaultValue = "jsparrow.yml", property = "configFile")
	protected File configFile;
	
	@Parameter(defaultValue = "", property = "profile")
	protected String profile;

	List<String> path = new ArrayList<>();

	public static final String USER_DIR = "user.dir"; //$NON-NLS-1$
	public static final String STANDALONE_BUNDLE_NAME = "eu.jsparrow.standalone"; //$NON-NLS-1$
	public static final String INSTANCE_DATA_LOCATION_CONSTANT = "osgi.instance.area.default"; //$NON-NLS-1$
	public static final String PROJECT_PATH_CONSTANT = "PROJECT.PATH"; //$NON-NLS-1$
	public static final String CONFIG_FILE_PATH = "CONFIG.FILE.PATH";  //$NON-NLS-1$
	public static final String SELECTED_PROFILE = "PROFILE.SELECTED"; //$NON-NLS-1$
	
	public void execute() throws MojoExecutionException {
		getLog().info("Hello, world.");

		try {
			startOSGI();
		} catch (BundleException | InterruptedException e) {
			getLog().error(e.getMessage(), e);
		}

	}

	private void startOSGI() throws BundleException, InterruptedException {
		final Map<String, String> configuration = new HashMap<>();
		configuration.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
		configuration.put(Constants.FRAMEWORK_STORAGE, "target/bundlecache");
		configuration.put(CONFIG_FILE_PATH, configFile.getAbsolutePath());
		configuration.put(SELECTED_PROFILE, (profile == null) ? "" : profile);

		// Set working directory
		String file = System.getProperty("java.io.tmpdir");
		File directory = new File(file + "/temp_jSparrow").getAbsoluteFile();
		if (directory.exists() || directory.mkdirs()) {
			System.setProperty(USER_DIR, directory.getAbsolutePath());
			getLog().info("Set user.dir to " + directory.getAbsolutePath());
		}

		configuration.put(INSTANCE_DATA_LOCATION_CONSTANT, System.getProperty(USER_DIR));
		configuration.put(PROJECT_PATH_CONSTANT, project.getBasedir().getAbsolutePath());

		ServiceLoader<FrameworkFactory> ffs = ServiceLoader.load(FrameworkFactory.class);
		FrameworkFactory frameworkFactory = ffs.iterator().next();
		
		final Framework framework = frameworkFactory.newFramework(configuration);

		framework.start();

		final BundleContext ctx = framework.getBundleContext();

		final List<Bundle> bundles = new ArrayList<>();

		// for (File f : new
		// File("/home/andreja/workspaces/rcp-neon/jSparrow.maven/resources/plugins").listFiles())
		// {
		//
		// try (FileInputStream fileInputStream = new FileInputStream(f)) {
		// bundles.add(ctx.installBundle("reference:" + f.toURI()));
		// fileInputStream.close();
		// } catch (IOException e) {
		// getLog().error(e.getMessage(), e);
		// }
		// }

		// InputStream is =
		// GreetingMojo.class.getResourceAsStream(File.separator +
		// "manifest.standalone");

		try (InputStream is = GreetingMojo.class.getResourceAsStream(File.separator + "manifest.standalone");
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));) {
			String line = "";

			if (is != null) {
				while ((line = reader.readLine()) != null) {
					InputStream fileStream = GreetingMojo.class.getResourceAsStream(File.separator + line);
					bundles.add(ctx.installBundle("file://" + line, fileStream));
				}
			}
		} catch (IOException e) {
			getLog().error(e.getMessage(), e);
		}

		startAllBundles(bundles);

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

	private void deleteChildren(File parentDirectory) throws IOException {
		for (String file : Arrays.asList(parentDirectory.list())) {
			File currentFile = new File(parentDirectory.getAbsolutePath(), file);
			if (currentFile.isDirectory()) {
				deleteChildren(currentFile);
			}
			currentFile.delete();
		}
	}

	private void startAllBundles(List<Bundle> bundles) {
		List<Bundle> bundlesCopy = new ArrayList<>();
		bundlesCopy.addAll(bundles);

		boolean standaloneStarted = false;
		while (!bundlesCopy.isEmpty()) {
			if (standaloneStarted) {
				break;
			}
			List<Bundle> bundlesTemp = new ArrayList<>();
			bundlesTemp.addAll(bundlesCopy);
			for (final Bundle bundle : bundlesTemp) {
				if (bundle.getHeaders().get(Constants.FRAGMENT_HOST) == null && null != bundle.getSymbolicName()
						&& (bundle.getSymbolicName().startsWith(STANDALONE_BUNDLE_NAME))) {
					try {
						getLog().info(
								"Starting BUNDLE: " + bundle.getSymbolicName() + ", resolution: " + bundle.getState());
						if (null != bundle && null != bundle.getSymbolicName()
								&& bundle.getSymbolicName().equals(STANDALONE_BUNDLE_NAME)) {
							standaloneStarted = true;
						}
						bundle.start();
						bundlesCopy.remove(bundle);
					} catch (Exception e) {
						getLog().error(e.getMessage(), e);
						if (null != bundle && null != bundle.getSymbolicName()
								&& bundle.getSymbolicName().equals(STANDALONE_BUNDLE_NAME)) {
							standaloneStarted = false;
						}
						continue;
					}
				}
			}
		}
	}
}
