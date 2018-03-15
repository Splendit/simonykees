package eu.jsparrow.adapter;

import java.io.File;
import java.util.Optional;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public class MavenArguments {
	
	private Log log;
	private MavenProject project;
	private File defaultYamlFile;
	private MavenSession mavenSession;
	private String mavenHome;
	private String profile;
	private String mode;
	private boolean useDefaultConfig;
	
	public MavenArguments(MavenProject project, Log log, File defaultYamlFile, MavenSession mavenSession, String mode) {
		this.project = project;
		this.log = log;
		this.defaultYamlFile = defaultYamlFile;
		this.mavenSession = mavenSession;
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

	public Log getLog() {
		return log;
	}

	public MavenProject getProject() {
		return project;
	}

	public File getDefaultYamlFile() {
		return defaultYamlFile;
	}

	public MavenSession getMavenSession() {
		return mavenSession;
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
}
