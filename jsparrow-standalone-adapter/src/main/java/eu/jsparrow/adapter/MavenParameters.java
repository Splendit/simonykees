package eu.jsparrow.adapter;

import java.io.File;
import java.util.Optional;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * A class for wrapping the parameters injected in the maven plugin.
 * 
 * @author Ardit Ymeri
 * @since 2.5.0
 *
 */
public class MavenParameters {

	private Log log;
	private MavenProject project;
	private File defaultYamlFile;
	private MavenSession mavenSession;
	private String mavenHome;
	private String profile;
	private String mode;
	private boolean useDefaultConfig;
	private String ruleId;
	private String license;
	private boolean devMode;

	public MavenParameters(MavenProject project, Log log, File defaultYamlFile, MavenSession mavenSession, String mode,
			String license) {
		this(project, log, mode);
		this.defaultYamlFile = defaultYamlFile;
		this.mavenSession = mavenSession;
		this.license = license;
	}

	public MavenParameters(MavenProject project, Log log, String mode) {
		this.project = project;
		this.log = log;
		this.mode = mode;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public void setUseDefaultConfig(boolean useDefaultConfig) {
		this.useDefaultConfig = useDefaultConfig;
	}

	public void setMavenHome(String mavenHome) {
		this.mavenHome = mavenHome;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public Log getLog() {
		return log;
	}

	public MavenProject getProject() {
		return project;
	}

	public Optional<File> getDefaultYamlFile() {
		return Optional.ofNullable(defaultYamlFile);
	}

	public Optional<MavenSession> getMavenSession() {
		return Optional.ofNullable(mavenSession);
	}

	public Optional<String> getMavenHome() {
		return Optional.ofNullable(mavenHome);
	}

	public Optional<String> getProfile() {
		return Optional.ofNullable(profile);
	}

	public String getMode() {
		return mode;
	}

	public Optional<Boolean> getUseDefaultConfig() {
		return Optional.ofNullable(useDefaultConfig);
	}

	public Optional<String> getRuleId() {
		return Optional.ofNullable(ruleId)
			.filter(s -> !s.isEmpty());
	}

	public String getLicense() {
		return license;
	}

	public boolean isDevMode() {
		return devMode;
	}

	public void setDevMode(boolean devMode) {
		this.devMode = devMode;
	}

}
