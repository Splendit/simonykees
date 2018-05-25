package eu.jsparrow.maven.adapter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.osgi.util.NLS;

import eu.jsparrow.maven.i18n.Messages;

/**
 * Contains functionalities for running {@code mvn dependency:copy-dependencies}
 * plugin.
 * 
 * @author Andreja Sambolec, Matthias Webhofer, Ardit Ymeri
 * @since 2.5.0
 *
 */
public class DependencyManager {

	protected static final String OUTPUT_DIRECTORY_OPTION_KEY = "outputDirectory"; //$NON-NLS-1$
	private static final int BUFFER_SIZE = 4096;

	/**
	 * The output directory name must match with the one expected by
	 * {@link eu.jsparrow.standalone.StandaloneConfig}. The full path used in
	 * {@link #prepareDefaultRequest(MavenProject, InvocationRequest, Properties)}
	 * must also match with the one used in
	 * {@code StandaloneConfig::getMavenDependencyFolder}.
	 */
	private static final String OUTPUT_DIRECTORY_PREFIX = "deps"; //$NON-NLS-1$
	private static final String DEPENDENCY_PLUGIN_ID = "dependency"; //$NON-NLS-1$
	private static final String COPY_DEPENDENCIES_GOAL = "copy-dependencies"; //$NON-NLS-1$
	private static final String POM_FILE_NAME = "pom.xml"; //$NON-NLS-1$

	private Log log;
	private String mavenHomeUnzipped = ""; //$NON-NLS-1$
	private String defaultMavenHome;

	public DependencyManager(Log log, String defaultMavenHome) {
		this.log = log;
		this.defaultMavenHome = defaultMavenHome;
	}

	/**
	 * Executes maven goal copy-dependencies on the project to copy all resolved
	 * needed dependencies to the temp folder for use from bundles.
	 */
	public void extractAndCopyDependencies(MavenProject project) {
		log.debug(Messages.DependencyManager_extractAndCopyDependencies);
		final InvocationRequest request = new DefaultInvocationRequest();
		final Properties props = new Properties();
		prepareDefaultRequest(project, request, props);
		final Invoker invoker = new DefaultInvoker();
		String mavenHome = prepareMavenHome();
		invokeMaven(invoker, request, mavenHome);
	}

	/**
	 * Sets the pom file, goals and properties to the provided
	 * {@link InvocationRequest} for running:
	 * {@code mvn clean package depedency:copy-dependencies outputDirectory=[user.dir]/deps/[artifactId] -DskipTests}.
	 * 
	 * @see <a href=
	 *      "https://maven.apache.org/plugins/maven-dependency-plugin/copy-dependencies-mojo.html">
	 *      Apache Maven Dependency Plugin </a>.
	 * 
	 *      The output directory must match with the one used in
	 *      {@link eu.jsparrow.standalone.StandaloneConfig#getMavenDependencyFolder}.
	 * 
	 * @param project
	 *            the project to take the pom file from.
	 * @param request
	 *            the {@link InvocationRequest} to be updated.
	 * @param props
	 *            properties to be added to the invocation request.
	 */
	protected void prepareDefaultRequest(MavenProject project, InvocationRequest request, Properties props) {
		File projectBaseDir = project.getBasedir();
		String projectPath = projectBaseDir.getAbsolutePath();
		request.setPomFile(new File(projectPath + File.separator + POM_FILE_NAME));
		String cleanPackageCopyDependencies = String.format("clean package %s:%s ", DEPENDENCY_PLUGIN_ID, //$NON-NLS-1$
				COPY_DEPENDENCIES_GOAL);
		List<String> goals = Collections.singletonList(cleanPackageCopyDependencies);
		request.setGoals(goals);
		String outputDirectoryPath = System.getProperty(MavenAdapter.USER_DIR) + File.separator
				+ OUTPUT_DIRECTORY_PREFIX + File.separator + "\\${project.artifactId}"; //$NON-NLS-1$
		props.setProperty(OUTPUT_DIRECTORY_OPTION_KEY, outputDirectoryPath);
		props.setProperty("skipTests", "true"); //$NON-NLS-1$ , //$NON-NLS-2$
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

	/**
	 * If maven home from parameter is usable, use it, otherwise extract maven
	 * from resources to temp folder, set execute rights and use its maven home
	 * location.
	 */
	public String prepareMavenHome() {
		String jsarrowTempPath = WorkingDirectory.calculateJsparrowTempFolderPath();

		if (null != defaultMavenHome && !defaultMavenHome.isEmpty() && !defaultMavenHome.endsWith("EMBEDDED")) { //$NON-NLS-1$
			return defaultMavenHome;
		}

		log.debug(Messages.Adapter_embededMavenVersionDetected);
		String tempZipPath = jsarrowTempPath + File.separator + "maven"; //$NON-NLS-1$
		try (InputStream mavenZipInputStream = getMavenZipInputStream()) {
			mavenHomeUnzipped += tempZipPath;
			unzip(mavenZipInputStream, tempZipPath);
			return mavenHomeUnzipped;
		} catch (IOException e) {
			log.debug(e.getMessage(), e);
			log.error(e.getMessage());
		}

		return defaultMavenHome;
	}

	protected InputStream getMavenZipInputStream() {
		return getClass().getResourceAsStream("/apache-maven-3.5.2-bin.zip"); //$NON-NLS-1$
	}

	/**
	 * Extracts a zip entry (file entry)
	 * 
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		String loggerInfo = NLS.bind(Messages.EmbeddedMaven_fileUnzip, filePath);
		log.debug(loggerInfo);

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
		perms.add(PosixFilePermission.OTHERS_EXECUTE);

		perms.add(PosixFilePermission.GROUP_READ);
		perms.add(PosixFilePermission.GROUP_WRITE);
		perms.add(PosixFilePermission.GROUP_EXECUTE);

		Path path = file.toPath();
		Files.setPosixFilePermissions(path, perms);
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

		String loggerInfo = NLS.bind(Messages.EmbeddedMaven_unzipTemporaryMavenInstallation, destDir.toString());
		log.debug(loggerInfo);

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
				log.debug(NLS.bind(Messages.EmbeddedMaven_createDir, dir.getAbsoluteFile()));
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}
}
