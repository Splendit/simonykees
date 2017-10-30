package eu.jsparrow.maven;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
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
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.twdata.maven.mojoexecutor.MojoExecutor;

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

	@Parameter(defaultValue = "${session}")
	private MavenSession mavenSession;

	@Component
	private BuildPluginManager pluginManager;

	/**
	 * Maven project on which plugin goal is executed
	 */
	@Parameter(defaultValue = "${project}", required = true)
	MavenProject project;

	/**
	 * Value of maven home environment variable
	 */
	@Parameter(defaultValue = "${maven.home}")
	String mavenHome;

	@Parameter(defaultValue = "jsparrow.yml", property = "configFile")
	protected File configFile;

	@Parameter(defaultValue = "", property = "profile")
	protected String profile;

	private boolean standaloneStarted = false;
	private Framework framework = null;

	private String mavenHomeUnzipped = "";

	private File directory;

	private long standaloneBundleID = 0;

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
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					super.run();
					if (null != framework && null != framework.getBundleContext()) {
						try {
							// stop jSparrow.logging
							Bundle standaloneBundle = framework.getBundleContext().getBundle(standaloneBundleID);
							if (standaloneBundle.getState() == Bundle.ACTIVE) {
								standaloneBundle.stop();
							}
							framework.stop();
						} catch (BundleException e) {
							getLog().error(e.getMessage(), e);
						}
					}
					// CLEAN
					if (!standaloneStarted && null != directory) {
						try {
							deleteChildren(new File(directory.getAbsolutePath()));
						} catch (IOException e) {
							getLog().error(e.getMessage(), e);
						}
						directory.delete();
					}
					
				}
			});
			startOSGI();
		} catch (BundleException e) {
			getLog().error(e.getMessage(), e);
		} catch (InterruptedException e) {
			getLog().error(e.getMessage(), e);
		} finally {

			// CLEAN
			if (null != directory) {
				try {
					deleteChildren(new File(directory.getAbsolutePath()));
				} catch (IOException e) {
					getLog().error(e.getMessage(), e);
				}
				directory.delete();
			}
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
		final Map<String, String> configuration = new HashMap<String, String>();
		configuration.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
		configuration.put(Constants.FRAMEWORK_STORAGE, FRAMEWORK_STORAGE_VALUE);
		configuration.put(CONFIG_FILE_PATH,
				(configFile.exists() && !configFile.isDirectory()) ? configFile.getAbsolutePath() : "");
		configuration.put(SELECTED_PROFILE, (profile == null) ? "" : profile);

		// Set working directory
		String file = System.getProperty(JAVA_TMP);
		directory = new File(file + File.separator + JSPARROW_TEMP_FOLDER).getAbsoluteFile();
		if (directory.exists()) {
			if (Arrays.asList(directory.list()).size() == 1) {
				System.setProperty(USER_DIR, directory.getAbsolutePath());
				getLog().info("Set user.dir to " + directory.getAbsolutePath());
			} else {
				throw new InterruptedException("jSparrow already running");
			}
		} else if (directory.mkdirs()) {
			System.setProperty(USER_DIR, directory.getAbsolutePath());
			getLog().info("Set user.dir to " + directory.getAbsolutePath());
		} else {
			throw new InterruptedException("Could not create temp folder");
		}

		if (directory.getFreeSpace() < 165 * 1000000) {
			// potential OutOfSpace
		}

		configuration.put(INSTANCE_DATA_LOCATION_CONSTANT, System.getProperty(USER_DIR));
		configuration.put(PROJECT_PATH_CONSTANT, project.getBasedir().getAbsolutePath());
		configuration.put(PROJECT_NAME_CONSTANT, project.getName());

		// TODO improve with this approach
		// copyDepsWithMavenExecutor();

		/*
		 * if maven home from parameter is usable, use it, otherwise extract
		 * maven from resources to temp folder, set execute rights and use its
		 * maven home location
		 */
		if (null != mavenHome && !mavenHome.isEmpty() && !mavenHome.endsWith("EMBEDDED")) {
			extractAndCopyDependencies(mavenHome);
		} else {
			String tempZipPath = directory.getAbsolutePath() + File.separator + "maven";
			InputStream mavenZipInputStream = null;
			try {
				mavenZipInputStream = JsparrowMojo.class
						.getResourceAsStream(File.separator + "apache-maven-3.5.2-bin.zip");
				mavenHomeUnzipped += tempZipPath;
				unzip(mavenZipInputStream, tempZipPath);
				extractAndCopyDependencies(mavenHomeUnzipped);

			} catch (IOException e) {
				getLog().error(e.getMessage(), e);
			} finally {
				if (null != mavenZipInputStream) {
					try {
						mavenZipInputStream.close();
					} catch (IOException e) {
						getLog().error(e.getMessage(), e);
					}
				}
			}
		}

		ServiceLoader<FrameworkFactory> ffs = ServiceLoader.load(FrameworkFactory.class);
		FrameworkFactory frameworkFactory = ffs.iterator().next();

		framework = frameworkFactory.newFramework(configuration);

		framework.start();

		final BundleContext ctx = framework.getBundleContext();

		final List<Bundle> bundles = new ArrayList<Bundle>();

		// load jars from manifest and install bundles
		InputStream is = null;
		BufferedReader reader = null;
		try {
			is = JsparrowMojo.class.getResourceAsStream(File.separator + JSPARROW_MANIFEST);
			reader = new BufferedReader(new InputStreamReader(is));
			String line = "";

			if (is != null) {
				while ((line = reader.readLine()) != null) {
					InputStream fileStream = JsparrowMojo.class.getResourceAsStream(File.separator + line);
					bundles.add(ctx.installBundle("file://" + line, fileStream));
				}
			}
		} catch (IOException e) {
			getLog().error(e.getMessage(), e);
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
					getLog().error(e.getMessage(), e);
				}
			}
			if (null != reader) {
				try {
					reader.close();
				} catch (IOException e) {
					getLog().error(e.getMessage(), e);
				}
			}
		}

		startBundles(bundles);

		// STOP AND WAIT TO STOP WHEN DONE
		framework.stop();
		framework.waitForStop(0);
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
			if (bundle.getHeaders().get(Constants.FRAGMENT_HOST) == null && null != bundle.getSymbolicName()
					&& (bundle.getSymbolicName().startsWith(STANDALONE_BUNDLE_NAME))) {
				try {
					getLog().info(
							"Starting BUNDLE: " + bundle.getSymbolicName() + ", resolution: " + bundle.getState());
					bundle.start();
					standaloneBundleID = bundle.getBundleId();
					standaloneStarted = true;
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
	private void extractAndCopyDependencies(String mavenHome) {
		final InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(new File(project.getBasedir().getAbsolutePath() + File.separator + "pom.xml"));
		request.setGoals(Collections.singletonList("dependency:copy-dependencies "));

		final Properties props = new Properties();
		props.setProperty(OUTPUT_DIRECTORY_CONSTANT,
				System.getProperty(USER_DIR) + File.separator + DEPENDENCIES_FOLDER_CONSTANT);
		request.setProperties(props);

		final Invoker invoker = new DefaultInvoker();
		invoker.setMavenHome(new File(mavenHome));

		try {
			invoker.execute(request);
		} catch (final MavenInvocationException e) {
			getLog().error(e.getMessage(), e);
		}
	}

	private void copyDepsWithMavenExecutor() {
		getLog().info("Session: " + mavenSession);

		// TODO fix output directory and scope to test to include junit
		Plugin execPlugin = createMavenDepsPlugin();
		Xpp3Dom configuration = MojoExecutor.configuration();
		configureExecPlugin(configuration);
		try {
			executeMojo(execPlugin, "copy-dependencies", configuration,
					executionEnvironment(project, mavenSession, pluginManager));
		} catch (MojoExecutionException e) {
			getLog().error(e.getMessage(), e);
		}
	}

	private Plugin createMavenDepsPlugin() {
		Plugin dependenciesPlugin = new Plugin();
		dependenciesPlugin.setGroupId("org.apache.maven.plugins");
		dependenciesPlugin.setArtifactId("maven-dependency-plugin");
		dependenciesPlugin.setVersion("3.0.2");

		return dependenciesPlugin;
	}

	private void configureExecPlugin(Xpp3Dom configuration) {
		configuration.setAttribute(OUTPUT_DIRECTORY_CONSTANT,
				System.getProperty(USER_DIR) + File.separator + DEPENDENCIES_FOLDER_CONSTANT);
	}

	private static final int BUFFER_SIZE = 4096;

	/**
	 * Extracts a zip file from zipInputStream to a directory specified by
	 * destDirectory which is created if does not exists
	 * 
	 * @param zipInputStream
	 * @param destDirectory
	 * @throws IOException
	 */
	public void unzip(InputStream zipInputStream, String destDirectory) throws IOException {
		File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		ZipInputStream zipIn = new ZipInputStream(zipInputStream);
		ZipEntry entry = zipIn.getNextEntry();
		mavenHomeUnzipped += File.separator + entry.getName();
		// iterates over entries in the zip file
		while (entry != null) {
			String filePath = destDirectory + File.separator + entry.getName();
			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				extractFile(zipIn, filePath);
			} else {
				// if the entry is a directory, make the directory
				File dir = new File(filePath);
				dir.mkdir();
				getLog().info("create dir : " + dir.getAbsoluteFile());
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

	/**
	 * Extracts a zip entry (file entry)
	 * 
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		getLog().info("file unzip : " + filePath);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
		File file = new File(filePath);

		Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
		perms.add(PosixFilePermission.OWNER_READ);
		perms.add(PosixFilePermission.OWNER_WRITE);
		perms.add(PosixFilePermission.OWNER_EXECUTE);

		perms.add(PosixFilePermission.OTHERS_READ);
		perms.add(PosixFilePermission.OTHERS_WRITE);
		perms.add(PosixFilePermission.OWNER_EXECUTE);

		perms.add(PosixFilePermission.GROUP_READ);
		perms.add(PosixFilePermission.GROUP_WRITE);
		perms.add(PosixFilePermission.GROUP_EXECUTE);

		Files.setPosixFilePermissions(file.toPath(), perms);

	}

}
