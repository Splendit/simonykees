package eu.jsparrow.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import eu.jsparrow.maven.util.MavenUtil;

/**
 * Starts Equinox framework and headless version of jSparrow Eclipse plugin.
 * 
 * @author Andreja Sambolec, Matthias Webhofer
 * @since 2.2.1
 *
 */
@SuppressWarnings("nls")
@Mojo(name = "refactor", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, requiresDependencyResolution = ResolutionScope.NONE, requiresProject = true)
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

	@Parameter(property = "defaultConfiguration")
	protected boolean useDefaultConfig;

	// CONSTANTS
	public static final String CONFIG_FILE_PATH = "CONFIG.FILE.PATH";
	public static final String SELECTED_PROFILE = "PROFILE.SELECTED";
	public static final String USE_DEFAULT_CONFIGURATION = "DEFAULT.CONFIG";

	public void execute() throws MojoExecutionException {
		if (!"pom".equalsIgnoreCase(project.getPackaging())) {
			try {
				Runtime.getRuntime()
					.addShutdownHook(createShutdownHook());
				final Map<String, String> configuration = new HashMap<>();
				configuration.put(CONFIG_FILE_PATH,
						(configFile.exists() && !configFile.isDirectory()) ? configFile.getAbsolutePath() : "");
				configuration.put(SELECTED_PROFILE, (profile == null) ? "" : profile);
				configuration.put(USE_DEFAULT_CONFIGURATION, Boolean.toString(useDefaultConfig));

				MavenUtil.startOSGI(project, mavenHome, getLog(), configuration);
			} catch (BundleException | InterruptedException e) {
				getLog().error(e.getMessage(), e);
			} finally {

				// CLEAN
				if (null != MavenUtil.getDirectory()) {
					try {
						deleteChildren(new File(MavenUtil.getDirectory()
							.getAbsolutePath()));
						Files.delete(MavenUtil.getDirectory()
							.toPath());
					} catch (IOException e) {
						getLog().error(e.getMessage(), e);
					}
				}
			}
		}
	}

	/**
	 * Recursively deletes all sub-folders from received folder.
	 * 
	 * @param parentDirectory
	 *            directory which content is to be deleted
	 * @throws IOException
	 */
	private void deleteChildren(File parentDirectory) throws IOException {
		String[] children = parentDirectory.list();
		if (children != null) {
			for (String file : Arrays.asList(children)) {
				File currentFile = new File(parentDirectory.getAbsolutePath(), file);
				if (currentFile.isDirectory()) {
					deleteChildren(currentFile);
				}
				Files.delete(currentFile.toPath());
			}
		}
	}

	private Thread createShutdownHook() {
		return new Thread() {
			@Override
			public void run() {
				super.run();
				if (null != MavenUtil.getFramework() && null != MavenUtil.getFramework()
					.getBundleContext()) {
					try {
						// stop jSparrow.logging
						Bundle standaloneBundle = MavenUtil.getFramework()
							.getBundleContext()
							.getBundle(MavenUtil.getStandaloneBundleID());
						if (standaloneBundle.getState() == Bundle.ACTIVE) {
							standaloneBundle.stop();
						}
						MavenUtil.getFramework()
							.stop();
					} catch (BundleException e) {
						getLog().error(e.getMessage(), e);
					}
				}
				// CLEAN
				if (!MavenUtil.isStandaloneStarted() && null != MavenUtil.getDirectory()) {
					try {
						deleteChildren(new File(MavenUtil.getDirectory()
							.getAbsolutePath()));
					} catch (IOException e) {
						getLog().error(e.getMessage(), e);
					}

					try {
						Files.delete(MavenUtil.getDirectory()
							.toPath());
					} catch (IOException e) {
						getLog().error(e.getMessage(), e);
					}
				}
			}
		};
	}
}
