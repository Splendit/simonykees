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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.maven.model.Dependency;
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
 * Says "Hi" to the user.
 *
 */
@Mojo(name = "refactor", defaultPhase = LifecyclePhase.INSTALL, requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true)
public class GreetingMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", required = true)
	MavenProject project;
	
	@Parameter(defaultValue = "${maven.home}", required = true)
	String mavenHome;
	
	/**
	 * The directory where files are located.
	 */
	@Parameter(defaultValue = "${basedir}/src/main/java")
	protected File sourceDirectory;

	List<String> path = new ArrayList<>();

	public static final String USER_DIR = "user.dir"; //$NON-NLS-1$
	public static final String STANDALONE_BUNDLE_NAME = "eu.jsparrow.standalone"; //$NON-NLS-1$
	public static final String INSTANCE_DATA_LOCATION_CONSTANT = "osgi.instance.area.default"; //$NON-NLS-1$
	public static final String PROJECT_PATH_CONSTANT = "PROJECT.PATH"; //$NON-NLS-1$
	public static final String PROJECT_NAME_CONSTANT = "PROJECT.NAME"; //$NON-NLS-1$
	public static final String PROJECT_DEPENDENCIES = "PROJECT.DEPENDENCIES"; //$NON-NLS-1$

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

		// Set working directory
		String file = System.getProperty("java.io.tmpdir");
		File directory = new File(file + "/temp_jSparrow").getAbsoluteFile();
		if (directory.exists() || directory.mkdirs()) {
			System.setProperty(USER_DIR, directory.getAbsolutePath());
			getLog().info("Set user.dir to " + directory.getAbsolutePath());
		}

		configuration.put(INSTANCE_DATA_LOCATION_CONSTANT, System.getProperty(USER_DIR));
		configuration.put(PROJECT_PATH_CONSTANT, project.getBasedir().getAbsolutePath());
		configuration.put(PROJECT_NAME_CONSTANT, project.getName());

		extractDependencies();
		// TODO add System.getProperty("user.dir") + File.separator + "deps" to classpath
		
//		Set<Dependency> dependencies = new HashSet<>(project.getDependencies());
////		List<Dependency> dependencies = loadCopiedDependencies();
//
//		if (null != project.getParent()) {
//			getLog().info("Number of parent dependencies: " + project.getParent().getDependencies().size());
//			dependencies.addAll(project.getParent().getDependencies());
//		}
//
//		String dependenciesString = "";
//		for (Dependency dependency : dependencies) {
//			getLog().info("DEPENDENCY: " + dependency.getGroupId() + File.separator + dependency.getArtifactId()
//					+ File.separator + dependency.getVersion());
//			dependenciesString += dependency.getGroupId() + File.separator + dependency.getArtifactId() + File.separator
//					+ dependency.getVersion() + ";";
//		}
//		configuration.put(PROJECT_DEPENDENCIES, dependenciesString);

		ServiceLoader<FrameworkFactory> ffs = ServiceLoader.load(FrameworkFactory.class);
		FrameworkFactory frameworkFactory = ffs.iterator().next();

		final Framework framework = frameworkFactory.newFramework(configuration);

		framework.start();

		final BundleContext ctx = framework.getBundleContext();

		final List<Bundle> bundles = new ArrayList<>();

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

	private void extractDependencies() {
		final InvocationRequest request = new DefaultInvocationRequest();
		getLog().info("POM path: " + project.getBasedir().getAbsolutePath() + File.separator + "pom.xml");
		request.setPomFile(new File(project.getBasedir().getAbsolutePath() + File.separator + "pom.xml"));
		request.setGoals(Collections.singletonList("dependency:copy-dependencies "));
		final Properties props = new Properties();
		props.setProperty("outputDirectory", System.getProperty("user.dir") + File.separator + "deps");
		request.setProperties(props);
		final Invoker invoker = new DefaultInvoker();
		getLog().info("M2_HOME path: " + mavenHome);
		invoker.setMavenHome(new File(mavenHome));
		try {
			invoker.execute(request);
		} catch (final MavenInvocationException e) {
			e.printStackTrace();
		}
	}
}
