package eu.jsparrow.maven.adapter;

import java.io.File;
import java.util.Optional;

import org.apache.maven.execution.MavenSession;

/**
 * A class for wrapping the parameters injected in the maven plugin.
 * 
 * @author Ardit Ymeri
 * @since 2.5.0
 *
 */
public class MavenParameters {

	private File defaultYamlFile;
	private MavenSession mavenSession;
	private String mavenHome;
	private String profile;
	private String mode;
	private boolean useDefaultConfig;
	private String ruleId;
	private String license;
	private String url;
	private boolean devMode;

	public MavenParameters(File defaultYamlFile, MavenSession mavenSession, String mode, String license, String url,
			String mavenHome, String profile, boolean useDefault, boolean devMode) {
		this(defaultYamlFile, mavenSession, mode, license, url);
		setMavenHome(mavenHome);
		setProfile(profile);
		setUseDefaultConfig(useDefault);
		setDevMode(devMode);
	}

	public MavenParameters(File defaultYamlFile, MavenSession mavenSession, String mode, String license, String url) {
		this(mode);
		this.defaultYamlFile = defaultYamlFile;
		this.mavenSession = mavenSession;
		this.license = license;
		this.url = url;
	}

	public MavenParameters(String mode) {
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

	public String getUrl() {
		return url;
	}

	public boolean isDevMode() {
		return devMode;
	}

	public void setDevMode(boolean devMode) {
		this.devMode = devMode;
	}

}
