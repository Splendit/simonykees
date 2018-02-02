package eu.jsparrow.maven.util;

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
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
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

import eu.jsparrow.maven.RefactorMojo;

/**
 * {@code MavenHelper} is a helper class which provides methods for handling
 * equinox, starting the jsparrow.standalone bundle and other methods, which are
 * used from more than one MOJO.
 * 
 * @author Andreja Sambolec, Matthias Webhofer
 * @since 2.3.0
 *
 */
@SuppressWarnings("nls")
public class MavenHelper {

	// CONSTANTS
	private static final String USER_DIR = "user.dir";
	private static final String JAVA_TMP = "java.io.tmpdir";
	protected static final String STANDALONE_BUNDLE_NAME = "eu.jsparrow.standalone";
	private static final String INSTANCE_DATA_LOCATION_CONSTANT = "osgi.instance.area.default";
	private static final String FRAMEWORK_STORAGE_VALUE = "target/bundlecache";
	private static final String PROJECT_PATH_CONSTANT = "PROJECT.PATH";
	private static final String PROJECT_NAME_CONSTANT = "PROJECT.NAME";
	private static final String JSPARROW_TEMP_FOLDER = "temp_jSparrow";
	private static final String JSPARROW_MANIFEST = "manifest.standalone";
	protected static final String OUTPUT_DIRECTORY_CONSTANT = "outputDirectory";
	private static final String DEPENDENCIES_FOLDER_CONSTANT = "deps";
	private static final String OSGI_INSTANCE_AREA_CONSTANT = "osgi.instance.area";

	private boolean standaloneStarted = false;
	private long standaloneBundleID = 0;
	private Framework framework = null;
	private BundleContext bundleContext = null;
	private String mavenHomeUnzipped = "";
	private File directory;
	private static final int BUFFER_SIZE = 4096;

	private MavenProject project;
	private String mavenHome;
	private MavenSession mavenSession;
	private BuildPluginManager pluginManager;
	private Log log;

	public MavenHelper(MavenProject project, String mavenHome, Log log) {
		this.project = project;
		this.mavenHome = mavenHome;
		this.log = log;
	}

	public MavenHelper(MavenProject project, String mavenHome, MavenSession mavenSession,
			BuildPluginManager pluginManager, Log log) {
		this(project, mavenHome, log);
		this.mavenSession = mavenSession;
		this.pluginManager = pluginManager;

	}

	/**
	 * Creates Equinox framework, collects all bundles from src/main/resources,
	 * specified in manifest.standalone file and starts the framework. When
	 * done, stops framework and cleans created temp_jSparrow folder.
	 * 
	 * @throws BundleException
	 * @throws InterruptedException
	 * @throws MojoExecutionException
	 */
	public void startOSGI() throws BundleException, InterruptedException, MojoExecutionException {
		startOSGI(null);
	}

	/**
	 * Creates Equinox framework, collects all bundles from src/main/resources,
	 * specified in manifest.standalone file and starts the framework. When
	 * done, stops framework and cleans created temp_jSparrow folder.
	 * 
	 * @param additionalConfiguration
	 *            will be added to the standard configuration
	 * @throws BundleException
	 * @throws InterruptedException
	 * @throws MojoExecutionException
	 */
	public void startOSGI(Map<String, String> additionalConfiguration)
			throws BundleException, InterruptedException, MojoExecutionException {

		final Map<String, String> configuration = prepareConfiguration(additionalConfiguration);

		prepareWorkingDirectory(configuration);

		// TODO improve with this approach
		// copyDepsWithMavenExecutor();

		String newMavenHome = prepareMaven();

		if (newMavenHome != null) {
			extractAndCopyDependencies(newMavenHome);

			startEquinoxFramework(configuration);

			List<Bundle> bundles = loadBundles();
			startBundles(bundles);

			stopEquinoxFramework();
		}
	}

	/**
	 * walks through all files and subdirectories and removes them.
	 * 
	 * @param directory
	 *            directory which has to be deleted
	 * @throws IOException
	 */
	public void deleteChildren(File directory) throws IOException {

		String[] children = directory.list();

		if (children != null) {
			for (String file : Arrays.asList(children)) {
				File currentFile = new File(directory.getAbsolutePath(), file);
				if (currentFile.isDirectory()) {
					deleteChildren(currentFile);
				}
				Files.delete(currentFile.toPath());
			}
		}
	}

	/**
	 * creates a new shutdown hook for stopping equinox and cleaning the temp
	 * directory
	 * 
	 * @return
	 */
	public Thread createShutdownHook() {
		return new Thread() {
			@Override
			public void run() {
				super.run();
				shutdownFramework();
				cleanUp();
			}
		};
	}

	/**
	 * shuts down the standalone bundle and equinox
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
				this.getFramework()
					.stop();
			} catch (BundleException e) {
				log.debug(e.getMessage(), e);
				log.error(e.getMessage());
			}
		}
	}

	/**
	 * removes the temp directory
	 */
	public void cleanUp() {
		if (!this.isStandaloneStarted() && null != this.getDirectory()) {
			try {
				deleteChildren(new File(this.getDirectory()
					.getAbsolutePath()));
				Files.delete(this.getDirectory()
					.toPath());
			} catch (IOException e) {
				log.debug(e.getMessage(), e);
				log.error(e.getMessage());
			}
		}
	}

	/*** HELPER METHODS ***/

	protected Map<String, String> prepareConfiguration(Map<String, String> additionalConfiguration) {

		if (additionalConfiguration == null) {
			additionalConfiguration = new HashMap<>();
		}

		additionalConfiguration.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
		additionalConfiguration.put(Constants.FRAMEWORK_STORAGE, FRAMEWORK_STORAGE_VALUE);
		additionalConfiguration.put(INSTANCE_DATA_LOCATION_CONSTANT, System.getProperty(USER_DIR));
		additionalConfiguration.put(PROJECT_PATH_CONSTANT, getProjectPath());
		additionalConfiguration.put(PROJECT_NAME_CONSTANT, getProjectName());

		return additionalConfiguration;
	}

	/**
	 * creates and prepares the temporary working directory and sets its path in
	 * system properties and equinox configuration
	 * 
	 * @param configuration
	 * @throws InterruptedException
	 */
	protected void prepareWorkingDirectory(Map<String, String> configuration) throws InterruptedException {
		setWorkingDirectory();

		if (directory.exists()) {
			if (Arrays.asList(directory.list())
				.size() == 1) {
				System.setProperty(USER_DIR, directory.getAbsolutePath());
				configuration.put(OSGI_INSTANCE_AREA_CONSTANT, directory.getAbsolutePath());
				log.info("Set user.dir to " + directory.getAbsolutePath());
			} else {
				throw new InterruptedException("jSparrow already running");
			}
		} else if (directory.mkdirs()) {
			System.setProperty(USER_DIR, directory.getAbsolutePath());
			configuration.put(OSGI_INSTANCE_AREA_CONSTANT, directory.getAbsolutePath());
			log.info("Set user.dir to " + directory.getAbsolutePath());
		} else {
			throw new InterruptedException("Could not create temp folder");
		}
	}

	protected void setWorkingDirectory() {
		String file = System.getProperty(JAVA_TMP);
		directory = new File(file + File.separator + JSPARROW_TEMP_FOLDER).getAbsoluteFile();
	}

	/**
	 * if maven home from parameter is usable, use it, otherwise extract maven
	 * from resources to temp folder, set execute rights and use its maven home
	 * location
	 */
	private String prepareMaven() {
		String newMavenHome = null;

		if (null != mavenHome && !mavenHome.isEmpty() && !mavenHome.endsWith("EMBEDDED")) {
			newMavenHome = mavenHome;
		} else {
			String tempZipPath = directory.getAbsolutePath() + File.separator + "maven";

			try (InputStream mavenZipInputStream = RefactorMojo.class
				.getResourceAsStream("/apache-maven-3.5.2-bin.zip")) {
				mavenHomeUnzipped += tempZipPath;
				unzip(mavenZipInputStream, tempZipPath);
				newMavenHome = mavenHomeUnzipped;
			} catch (IOException e) {
				log.debug(e.getMessage(), e);
				log.error(e.getMessage());
			}
		}

		return newMavenHome;
	}

	/**
	 * starts the equinox framework with the given configuration
	 * 
	 * @param configuration
	 * @throws BundleException
	 */
	private void startEquinoxFramework(Map<String, String> configuration) throws BundleException {
		ServiceLoader<FrameworkFactory> ffs = ServiceLoader.load(FrameworkFactory.class);
		FrameworkFactory frameworkFactory = ffs.iterator()
			.next();

		framework = frameworkFactory.newFramework(configuration);

		framework.start();
	}

	/**
	 * stops the equinox framework
	 * 
	 * @throws InterruptedException
	 * @throws BundleException
	 * @throws MojoExecutionException
	 */
	private void stopEquinoxFramework() throws InterruptedException, BundleException, MojoExecutionException {
		framework.stop();
		framework.waitForStop(0);
		standaloneStarted = false;

		String exitMessage = bundleContext.getProperty("eu.jsparrow.standalone.exit.message");
		if (exitMessage != null && !exitMessage.isEmpty()) {
			throw new MojoExecutionException(exitMessage);
		}
	}

	/**
	 * loads the manifest.standalone file, reads the names of the needed bundles
	 * and installs them in the framework's bundle context
	 * 
	 * @return a list of the installed bundles
	 * @throws BundleException
	 */
	protected List<Bundle> loadBundles() throws BundleException, MojoExecutionException {
		bundleContext = getBundleContext();
		final List<Bundle> bundles = new ArrayList<>();

		try (InputStream is = getManifestInputStream()) {
			if (is != null) {
				try (BufferedReader reader = getBufferedReaderFromInputStream(is)) {

					String line = "";
					while ((line = reader.readLine()) != null) {
						InputStream fileStream = getBundleResourceInputStream(line);
						Bundle bundle = bundleContext.installBundle("file://" + line, fileStream);
						bundles.add(bundle);
					}
				}
			} else {
				throw new MojoExecutionException(
						"The standalone manifest file could not be found. Please read the readme-file.");
			}
		} catch (IOException e) {
			log.debug(e.getMessage(), e);
			log.error(e.getMessage());
		}

		return bundles;
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
				.startsWith(STANDALONE_BUNDLE_NAME))
			.forEach(bundle -> {
				try {
					log.info("Starting BUNDLE: " + bundle.getSymbolicName() + ", resolution: " + bundle.getState());
					bundle.start();
					standaloneBundleID = bundle.getBundleId();
					standaloneStarted = true;
				} catch (Exception e) {
					log.debug(e.getMessage(), e);
					log.error(e.getMessage());
				}
			});
	}

	/**
	 * Executes maven goal copy-dependencies on the project to copy all resolved
	 * needed dependencies to the temp folder for use from bundles.
	 */
	private void extractAndCopyDependencies(String preparedMavenHome) {
		final InvocationRequest request = new DefaultInvocationRequest();
		final Properties props = new Properties();

		prepareDefaultRequest(request, props);

		final Invoker invoker = new DefaultInvoker();

		invokeMaven(invoker, request, preparedMavenHome);
	}

	protected void prepareDefaultRequest(InvocationRequest request, Properties props) {
		request.setPomFile(new File(getProjectPath() + File.separator + "pom.xml"));
		request.setGoals(Collections.singletonList("dependency:copy-dependencies "));

		props.setProperty(OUTPUT_DIRECTORY_CONSTANT,
				System.getProperty(USER_DIR) + File.separator + DEPENDENCIES_FOLDER_CONSTANT);
		request.setProperties(props);
	}

	protected void invokeMaven(Invoker invoker, InvocationRequest request, String preparedMavenHome) {
		invoker.setMavenHome(new File(preparedMavenHome));

		try {
			invoker.execute(request);
		} catch (final MavenInvocationException e) {
			log.debug(e.getMessage(), e);
			log.error(e.getMessage());
		}
	}

	private void copyDepsWithMavenExecutor() {
		log.info("Session: " + mavenSession);

		// TODO fix output directory and scope to test to include junit
		Plugin execPlugin = createMavenDepsPlugin();
		Xpp3Dom configuration = MojoExecutor.configuration();
		configureExecPlugin(configuration);
		try {
			executeMojo(execPlugin, "copy-dependencies", configuration,
					executionEnvironment(project, mavenSession, pluginManager));
		} catch (MojoExecutionException e) {
			log.debug(e.getMessage(), e);
			log.error(e.getMessage());
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
				log.info("create dir : " + dir.getAbsoluteFile());
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
		log.info("file unzip : " + filePath);
		try (FileOutputStream fos = new FileOutputStream(filePath);
				BufferedOutputStream bos = new BufferedOutputStream(fos)) {
			byte[] bytesIn = new byte[BUFFER_SIZE];
			int read = 0;
			while ((read = zipIn.read(bytesIn)) != -1) {
				bos.write(bytesIn, 0, read);
			}
		}

		File file = new File(filePath);

		Set<PosixFilePermission> perms = new HashSet<>();
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

	public boolean isStandaloneStarted() {
		return standaloneStarted;
	}

	public long getStandaloneBundleID() {
		return standaloneBundleID;
	}

	public Framework getFramework() {
		return framework;
	}

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	protected InputStream getBundleResourceInputStream(String resouceName) {
		return getClass().getResourceAsStream("/" + resouceName);
	}

	protected BufferedReader getBufferedReaderFromInputStream(InputStream is) {
		return new BufferedReader(new InputStreamReader(is));
	}

	protected InputStream getManifestInputStream() {
		return getClass().getResourceAsStream("/" + JSPARROW_MANIFEST);
	}

	protected BundleContext getBundleContext() {
		return framework.getBundleContext();
	}

	protected String getProjectName() {
		return project.getName();
	}

	protected String getProjectPath() {
		return project.getBasedir()
			.getAbsolutePath();
	}
}
